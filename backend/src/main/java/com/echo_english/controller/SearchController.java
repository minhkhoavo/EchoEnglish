package com.echo_english.controller;

import com.echo_english.dto.response.GoogleSearchResponse;
import com.echo_english.service.GoogleSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final GoogleSearchService googleSearchService;

    public SearchController(GoogleSearchService googleSearchService) {
        this.googleSearchService = googleSearchService;
    }

    @GetMapping
    public ResponseEntity<List<String>> performSearch(@RequestParam String query) {
        GoogleSearchResponse response = googleSearchService.search(query, 5);
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return ResponseEntity.ok(List.of("No result found!"));
        }
        List<String> results = response.getItems().stream()
                .map(item -> item.getTitle() + " - " + item.getLink())
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/details")
    public ResponseEntity<GoogleSearchResponse> performSearchWithDetails(@RequestParam String query) {
        GoogleSearchResponse response = googleSearchService.search(query, 5);
        if (response.getItems() == null) {
            GoogleSearchResponse emptyResponse = new GoogleSearchResponse();
            emptyResponse.setItems(Collections.emptyList());
            return ResponseEntity.ok(emptyResponse);
        }
        return ResponseEntity.ok(response);
    }
}