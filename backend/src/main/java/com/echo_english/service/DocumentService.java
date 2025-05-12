package com.echo_english.service;

import com.echo_english.ai.tools.WebContentTools;
import com.echo_english.entity.WebArticle;
import com.echo_english.repository.ArticleRepository;
import com.echo_english.utils.JSONUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class DocumentService {
    private final ArticleRepository articleRepository;
    private final ChatClient chatClient;
    private final WebContentTools webContentTools;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.news.summarize-content:true}")
    private boolean summarizeContent;

    @Value("${app.news.max-content-length-for-ai:10000}")
    private int maxContentLengthForAI;
//    private final Map<String, String> targetRssFeeds = Map.ofEntries(
//            Map.entry("https://feeds.bbci.co.uk/news/world/rss.xml", "BBC News World"),
//            Map.entry("http://rss.cnn.com/rss/cnn_world.rss", "CNN World"),
//            Map.entry("https://rss.nytimes.com/services/xml/rss/nyt/World.xml", "The New York Times World"),
//            Map.entry("http://feeds.reuters.com/Reuters/worldNews", "Reuters World"),
//            Map.entry("https://www.aljazeera.com/xml/rss/all.xml", "Al Jazeera All News"),
//            Map.entry("https://www.theguardian.com/world/rss", "The Guardian World"),
//            Map.entry("https://e.vnexpress.net/rss/news.rss", "VnExpress English")
//    );

    private final Map<String, String> targetRssFeeds = Map.ofEntries(
            Map.entry("https://www.aljazeera.com/xml/rss/all.xml", "Al Jazeera All News"),
            Map.entry("https://www.theguardian.com/world/rss", "The Guardian World"),
            Map.entry("https://e.vnexpress.net/rss/news.rss", "VnExpress English")
    );


    public DocumentService(ArticleRepository articleRepository,
                           ChatClient.Builder chatClientBuilder,
                           @Qualifier("webContentTools") WebContentTools webContentTools) {
        this.articleRepository = articleRepository;
        this.chatClient = chatClientBuilder.build();
        this.webContentTools = webContentTools;
    }

    public Page<WebArticle> getNews(Pageable pageable) {
         boolean suitable = true;
         return articleRepository.findBySuitableForLearnersOrderByPublishedDateDesc(suitable, pageable);
    }

    @Scheduled(cron = "${app.news.scan.cron:0 0 */4 * * ?}")
    @Transactional
    public void scanAndProcessDocumentsViaRss() {
        targetRssFeeds.forEach((feedUrl, siteName) -> {
            log.info("Fetching articles from RSS feed: {} (Source: {})", feedUrl, siteName);
            List<SyndEntry> rssEntries = fetchArticlesFromRss(feedUrl);

            if (rssEntries.isEmpty()) {
                log.info("No new entries found in RSS feed for {}", siteName);
                return;
            }

            List<SyndEntry> topEntries = rssEntries.stream()
                    .limit(3)
                    .toList();

            for (SyndEntry entry : topEntries) {
                String articleUrl = entry.getLink();
                if (articleUrl == null || articleUrl.isBlank()) {
                    log.warn("Skipping RSS entry with no link from feed: {}", feedUrl);
                    continue;
                }

                if (!articleRepository.existsByUrl(articleUrl)) {
                    processSingleRssEntry(entry, siteName);
                } else {
                    log.debug("Article already processed (from RSS): {}", articleUrl);
                }
            }
        });

        log.info("Scheduled document scan and processing via RSS feeds finished.");
    }

    public List<SyndEntry> fetchArticlesFromRss(String feedUrlString) {
        try {
            URL feedUrl = new URL(feedUrlString);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            return feed.getEntries();
        } catch (Exception e) {
            log.error("Error parsing RSS feed {}: {}", feedUrlString, e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private void processSingleRssEntry(SyndEntry rssEntry, String siteName) {
        String url = rssEntry.getLink();
        String title = rssEntry.getTitle();
        if (title == null || title.isBlank()) {
            title = "Title not available from RSS";
        }

        log.info("Processing new document from RSS ({}): \"{}\" - {}", siteName, title, url);

        String originalContent = webContentTools.extractMainContentFromUrl(url);
        String contentForAI = originalContent.length() > maxContentLengthForAI
                ? originalContent.substring(0, maxContentLengthForAI) + "..."
                : originalContent;

        String task;
        String commonInstruction = "Analyze the provided text for suitability for English learners. " +
                "The goal is to provide engaging and informative content that is not OVERLY distressing or inappropriate. " +
                "Consider if the text contains EXTREME violence, graphic details of death or suffering, hate speech, or content that is sexually explicit. " +
                "News reports on political events, international relations, or social issues are generally acceptable " +
                "UNLESS they are excessively biased, inflammatory, or present information in a way " +
                "that is gratuitously shocking or complex for an intermediate learner. " +
                "Respond in JSON format: {\"suitable\": boolean, \"assessment\": \"string\"}. ";

        if (summarizeContent) {
            task = commonInstruction +
                    "If suitable, 'assessment' should be a concise summary (1-2 paragraphs, simple English, focusing on main events and factual information). " +
                    "If unsuitable, 'assessment' should be a BRIEF explanation (1-2 sentences) of the primary reason, focusing on why it's OVERLY distressing or inappropriate.";
        } else {
            task = commonInstruction +
                    "If suitable, 'assessment' can be a short confirmation (e.g., 'Content appears suitable for general news reading.'). " +
                    "If unsuitable, 'assessment' should be a BRIEF explanation (1-2 sentences) of the primary reason, focusing on why it's OVERLY distressing or inappropriate.";
        }

        PromptTemplate promptTemplate = new PromptTemplate(
                "You are an AI assistant helping to curate news articles for English language learners (intermediate level and above). " +
                        "Your primary goal is to filter out content that is GRATUITOUSLY graphic, hateful, " +
                        "or sexually explicit, or so emotionally overwhelming that it hinders learning. " +
                        "Standard news reporting on current events, even if they involve conflict or politics, is often acceptable if presented factually. " +
                        "{task_definition}\n\nText to analyze:\n\"\"\"\n{textContent}\n\"\"\""
        );

        Map<String, Object> model = Map.of("task_definition", task, "textContent", contentForAI);
        Prompt aiPrompt = promptTemplate.create(model);

        log.debug("Sending prompt to AI for URL (from RSS): {}", url);
        ChatResponse aiResponse = chatClient.prompt(aiPrompt).call().chatResponse();

        String rawAiResult = null;
        if (aiResponse.getResult() != null && aiResponse.getResult().getOutput() != null) {
            rawAiResult = JSONUtils.extractPureJson(aiResponse.getResult().getOutput().getText());
        }

        String aiResult = null;
        if (rawAiResult != null && !rawAiResult.isBlank()) {
            aiResult = JSONUtils.extractPureJson(rawAiResult);
        }


        if (aiResult == null || aiResult.isBlank()) {
            log.error("AI returned empty, null, or unextractable JSON response for URL (from RSS): {}. Raw AI output: {}", url, rawAiResult);
            return;
        }
        WebArticle article = new WebArticle();
        article.setUrl(url);
        article.setTitle(title);
        article.setSource(siteName);

        if (rssEntry.getDescription() != null && rssEntry.getDescription().getValue() != null) {
            String rawSnippet = rssEntry.getDescription().getValue();
            String cleanSnippet = Jsoup.parse(rawSnippet).text();
            article.setSnippet(cleanSnippet.substring(0, Math.min(cleanSnippet.length(), 1000)));
        } else {
            article.setSnippet("Snippet not available from RSS.");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(aiResult);
            boolean suitable = rootNode.path("suitable").asBoolean(false);
            String assessment = rootNode.path("assessment").asText("AI assessment not available or format error.");

            int maxModerationNoteLength = 65000;
            if (assessment.length() > maxModerationNoteLength) {
                assessment = assessment.substring(0, maxModerationNoteLength);
            }

            article.setSuitableForLearners(suitable);
            article.setModerationNotes(assessment);

            if (suitable && summarizeContent) {
                article.setProcessedContent(assessment);
            } else if (suitable) {
                article.setProcessedContent("Content deemed suitable by AI. Original content available.");
            } else {
                article.setProcessedContent("Content deemed unsuitable by AI. Reason: " + assessment);
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing AI JSON response for URL {}: {}. AI Response: {}", url, e.getMessage(), aiResult, e);
        }

        Date rssPublishedDate = rssEntry.getPublishedDate();
        if (rssPublishedDate != null) {
            article.setPublishedDate(rssPublishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else {
            Date rssUpdatedDate = rssEntry.getUpdatedDate();
            if (rssUpdatedDate != null) {
                article.setPublishedDate(rssUpdatedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else {
                log.warn("No published or updated date found in RSS entry for {}", url);
            }
        }
        try {
            articleRepository.save(article);
            log.info("Successfully processed and saved document from RSS: {}", article.getUrl());
        } catch (Exception e) {
            log.error("Failed to save document {} (from RSS): {}", url, e.getMessage(), e);
        }
    }
}