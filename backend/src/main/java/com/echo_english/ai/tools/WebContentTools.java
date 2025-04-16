package com.echo_english.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebContentTools {
    @Autowired
    private UrlContentFetcher urlContentFetcher;

    @Tool(description = "Fetch and extract the main textual content from a specific web URL. It removes unnecessary HTML tags and returns only the essential text.")
    public String extractMainContentFromUrl(
            @ToolParam(description = "The full and valid URL of the web page from which to extract the main text content.")
            String url)
    {
        log.info("AI requested extraction of content from URL: {}", url);

        try {
            String rawHtml = urlContentFetcher.fetchHtmlContent(url).block();

            if (rawHtml == null || rawHtml.startsWith("Error") || rawHtml.startsWith("Unable")) {
                return "Unable to retrieve content from the provided URL or an error occurred while accessing it: " + rawHtml;
            }

            Document doc = Jsoup.parse(rawHtml);

            String extractedText = "";
            if (!doc.select("article").isEmpty()) {
                extractedText = doc.select("article").first().text();
            } else if (!doc.select("main").isEmpty()) {
                extractedText = doc.select("main").first().text();
            } else if (!doc.select(".main-content").isEmpty()) {
                extractedText = doc.select(".main-content").first().text();
            } else if (!doc.select("#content").isEmpty()) {
                extractedText = doc.select("#content").first().text();
            } else if (!doc.select(".post-body").isEmpty()) {
                extractedText = doc.select(".post-body").first().text();
            }
            else {
                extractedText = doc.body().text();
                log.warn("No specific main content selector found; using text from <body>.");
            }

            String processedText = extractedText.replaceAll("\\s{2,}", " ").replaceAll("\n+", "\n").trim(); // Normalize whitespace and line breaks
            int maxLength = 8000;
            if (processedText.length() > maxLength) {
                processedText = processedText.substring(0, maxLength) + "... (Content has been truncated)";
                log.info("Content from URL {} truncated to {} characters.", url, maxLength);
            }

            log.info("Successfully extracted {} characters of text from URL: {}", processedText.length(), url);
            return processedText;

        } catch (Exception e) {
            log.error("Error while extracting content from URL '{}': {}", url, e.getMessage(), e);
            return "An error occurred while trying to extract content from the specified URL. Error: " + e.getMessage();
        }
    }
}
