package com.echo_english.controller;

import com.echo_english.entity.WebArticle;
import com.echo_english.service.DocumentService;
import io.github.thoroldvix.api.TranscriptContent;
import io.github.thoroldvix.api.TranscriptRetrievalException;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.internal.TranscriptApiFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/document")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/youtube/{videoId}")
    @Cacheable("transcripts")
    public ResponseEntity<TranscriptContent> getTranscript(@PathVariable String videoId) throws TranscriptRetrievalException {
        System.out.println("Đang gọi API để lấy transcript cho videoId: " + videoId);
        YoutubeTranscriptApi youtubeTranscriptApi = TranscriptApiFactory.createDefault();
        TranscriptContent transcript = youtubeTranscriptApi.getTranscript(videoId, "en");
        return ResponseEntity.ok(transcript);
    }

    @GetMapping("/scan-and-process")
    public ResponseEntity<Map<String, String>> triggerScanAndProcessDocuments() {
        documentService.scanAndProcessDocumentsViaRss();
        return ResponseEntity.ok(Map.of("message",
                "Document scan and processing task triggered successfully. " +
                        "Check logs for details."));
    }
    @GetMapping("/news")
    public ResponseEntity<Page<WebArticle>> getNews(Pageable pageable) {
        Page<WebArticle> newsPage = documentService.getNews(pageable);
        return ResponseEntity.ok(newsPage);
    }
}
