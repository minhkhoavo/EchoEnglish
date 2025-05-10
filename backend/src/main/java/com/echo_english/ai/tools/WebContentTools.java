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
    private int maxContentLength = 8000;
    @Tool(description = "Fetch and extract the main textual content from a specific web URL. It removes unnecessary HTML tags and returns only the essential text.")
    public String extractMainContentFromUrl(String url) {
        log.info("Extraction of content requested for URL: {}", url);

        try {
            String rawHtml = urlContentFetcher.fetchHtmlContent(url);

            if (rawHtml == null || rawHtml.startsWith("Error") || rawHtml.startsWith("Unable") || rawHtml.startsWith("HTTP error")) {
                log.warn("Unable to retrieve content from URL {}: {}", url, rawHtml);
                return "Unable to retrieve content from the provided URL or an error occurred while accessing it: " + rawHtml;
            }

            Document doc = Jsoup.parse(rawHtml);

            String extractedText;
            if (!doc.select("article.story").isEmpty()) {
                extractedText = doc.select("article.story").first().text();
            } else if (!doc.select("main#main-content article").isEmpty()) {
                extractedText = doc.select("main#main-content article").first().text();
            } else if (!doc.select("article").isEmpty()) {
                extractedText = doc.select("article").first().text();
            } else if (!doc.select("main").isEmpty()) {
                extractedText = doc.select("main").first().text();
            } else if (!doc.select(".main-content, .content, #main, #content").isEmpty()) {
                extractedText = doc.select(".main-content, .content, #main, #content").first().text();
            } else if (!doc.select(".post-body, .entry-content").isEmpty()) {
                extractedText = doc.select(".post-body, .entry-content").first().text();
            } else {
                extractedText = doc.body().text();
                log.warn("No specific main content selector found for {}; using text from <body>.", url);
            }

            String processedText = extractedText.replaceAll("\\s{2,}", " ").replaceAll("\n+", "\n").trim();
            if (processedText.length() > maxContentLength) {
                processedText = processedText.substring(0, maxContentLength) + "... (Content has been truncated)";
                log.info("Content from URL {} truncated to {} characters.", url, maxContentLength);
            }

            log.info("Successfully extracted {} characters of text from URL: {}", processedText.length(), url);
            return processedText;

        } catch (Exception e) {
            log.error("Error while extracting content from URL '{}': {}", url, e.getMessage(), e);
            return "An error occurred while trying to extract content from the specified URL. Error: " + e.getMessage();
        }
    }
}
