# Technical Documentation: Article Review Module

## Overview

This module is responsible for automatically fetching, processing, and reviewing news articles from various RSS feeds to curate content suitable for English language learners. The review process is primarily handled by an AI model that assesses the suitability of the article content based on predefined criteria.

## RSS Feed Processing

The system periodically fetches articles from a predefined list of RSS feed URLs (`targetRssFeeds`). The `scanAndProcessDocumentsViaRss` method, scheduled to run at regular intervals, iterates through these feeds. For each feed, it retrieves the latest entries. To avoid reprocessing, it checks if an article with the same URL already exists in the database.

The `fetchArticlesFromRss` method handles the actual fetching and parsing of the RSS feed XML.

## Content Extraction

For each new article found in the RSS feed, the system extracts the main content from the article's URL using the `WebContentTools.extractMainContentFromUrl` method. This extracted content is then used for the AI assessment. A maximum content length is enforced for the text sent to the AI to manage token limits.

## AI Assessment

The core of the article review process is the AI assessment. The extracted article content is sent to a configured `ChatClient` (interacting with a language model). A detailed prompt is constructed to guide the AI's analysis. The prompt instructs the AI to evaluate the text for suitability for intermediate English learners, specifically looking for content that is overly distressing, inappropriate (extreme violence, graphic details, hate speech, sexually explicit), excessively biased, inflammatory, or gratuitously shocking/complex.

The AI is instructed to respond in a specific JSON format:
```json
{
  "suitable": boolean,
  "assessment": "string"
}
```
- `"suitable"`: A boolean indicating whether the article is deemed suitable for learners (`true`) or not (`false`).
- `"assessment"`: A string containing either a concise summary of the article (if suitable and summarization is enabled) or a brief explanation of why the article is unsuitable.

The system parses this JSON response to determine the suitability and the assessment notes.

## Data Storage

The processed article data, including the original URL, title, source, snippet, the AI's suitability assessment (`suitableForLearners`), and the AI's assessment notes (`moderationNotes`), is stored in the `WebArticle` entity in the database. If the article is deemed suitable and summarization is enabled, the AI's summary is stored in the `processedContent` field. Otherwise, a note about the suitability is stored.

## Accessing Suitable Articles

Articles that have been processed and marked as `suitableForLearners = true` are available for retrieval. The `getNews` method in `DocumentService` provides a paginated list of these suitable articles, ordered by their published date in descending order. This allows the application to display a curated list of news articles appropriate for English learners.
