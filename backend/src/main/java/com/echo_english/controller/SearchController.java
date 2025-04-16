package com.echo_english.controller;

import com.echo_english.dto.response.GoogleSearchResponse;
import com.echo_english.service.GoogleSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController {

    private final GoogleSearchService googleSearchService;

    public SearchController(GoogleSearchService googleSearchService) {
        this.googleSearchService = googleSearchService;
    }

    @GetMapping("/search")
    public Mono<List<String>> performSearch(@RequestParam String query) {
        return googleSearchService.search(query, 5)
                .map(response -> {
                    if (response.getItems() == null) {
                        return List.of("No result found!");
                    }
                    return response.getItems().stream()
                            .map(item -> item.getTitle() + " - " + item.getLink())
                            .collect(Collectors.toList());
                });
    }

    @GetMapping("/search/details")
    public Mono<GoogleSearchResponse> performSearchWithDetails(@RequestParam String query) {
        return googleSearchService.search(query, 5);
    }
}