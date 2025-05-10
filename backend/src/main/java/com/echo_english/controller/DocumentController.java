package com.echo_english.controller;

import com.echo_english.service.DocumentService;
import io.github.thoroldvix.api.TranscriptContent;
import io.github.thoroldvix.api.TranscriptRetrievalException;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.internal.TranscriptApiFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @GetMapping("/youtube/{videoId}")
    public ResponseEntity<TranscriptContent> getTranscript(@PathVariable String videoId) throws TranscriptRetrievalException {
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
}
