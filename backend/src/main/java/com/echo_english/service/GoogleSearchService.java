package com.echo_english.service;

import com.echo_english.dto.response.GoogleSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class GoogleSearchService {

    private final WebClient webClient;
    private final String apiKey;
    private final String searchEngineId;

    // Google Custom Search API endpoint URL
    private static final String GOOGLE_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";

    public GoogleSearchService(WebClient.Builder webClientBuilder,
                               @Value("${google.api.key}") String apiKey,
                               @Value("${google.cse.id}") String searchEngineId) {
        this.webClient = webClientBuilder.baseUrl(GOOGLE_SEARCH_API_URL).build();
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
    }

    public Mono<GoogleSearchResponse> search(String query, int numResults) {
        if (numResults <= 0 || numResults > 10) {
            numResults = 10;
        }
        final int finalNumResults = numResults;

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)         // API Key
                        .queryParam("cx", searchEngineId)    // Search Engine ID
                        .queryParam("q", query)              // Search query
                        .queryParam("num", finalNumResults)  // Number of results
                        // .queryParam("start", 1)           // Starting index (for pagination)
                        // .queryParam("fields", "items(title,link,snippet)") // Only retrieve necessary fields
                        .build())
                .retrieve()
                .bodyToMono(GoogleSearchResponse.class) // Map the JSON response to GoogleSearchResponse class
                .doOnError(error -> System.err.println("Error calling Google Search API: " + error.getMessage()))
                .onErrorResume(error -> {
                    System.err.println("Error:::::" + error.getMessage());
                    GoogleSearchResponse errorResponse = new GoogleSearchResponse();
                    errorResponse.setItems(Collections.emptyList()); // Set an empty list
                    return Mono.just(errorResponse);
                });
    }

    public Mono<GoogleSearchResponse> search(String query) {
        return search(query, 5);
    }
}







