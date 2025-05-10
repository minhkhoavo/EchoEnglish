package com.echo_english.service;

import com.echo_english.dto.response.GoogleSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
@Slf4j
public class GoogleSearchService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String searchEngineId;

    private static final String GOOGLE_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";

    public GoogleSearchService(RestTemplate restTemplate,
                               @Value("${google.api.key}") String apiKey,
                               @Value("${google.cse.id}") String searchEngineId) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
    }

    public GoogleSearchResponse search(String query, int numResults, String sort, String dateRestrict) {
        if (numResults <= 0 || numResults > 10) {
            numResults = 10; // Google API giới hạn num tối đa là 10
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_SEARCH_API_URL)
                .queryParam("key", apiKey)
                .queryParam("cx", searchEngineId)
                .queryParam("q", query)
                .queryParam("num", numResults)
                .queryParam("sort", "date");

        if (sort != null && !sort.isEmpty()) {
            builder.queryParam("sort", sort); // e.g., "date"
        }
        if (dateRestrict != null && !dateRestrict.isEmpty()) {
            builder.queryParam("dateRestrict", dateRestrict); // e.g., "d1" for last day
        }
        // Bạn có thể thêm queryParam("fields", "items(title,link,snippet,pagemap)") để giới hạn trường trả về

        String url = builder.toUriString();
        log.error("Calling Google Search API: {}", url);

        try {
            ResponseEntity<GoogleSearchResponse> responseEntity = restTemplate.getForEntity(url, GoogleSearchResponse.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody();
            } else {
                log.warn("Google Search API call was not successful or body is null. Status: {}, URL: {}", responseEntity.getStatusCode(), url);
                return createEmptyResponse();
            }
        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException calling Google Search API (URL: {}): Status Code {}, Response Body: {}", url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return createEmptyResponse();
        } catch (Exception e) {
            log.error("Unexpected error calling Google Search API (URL: {}): {}", url, e.getMessage(), e);
            return createEmptyResponse();
        }
    }

    public GoogleSearchResponse search(String query, int numResults) {
        return search(query, numResults, null, null);
    }

    public GoogleSearchResponse search(String query) {
        return search(query, 5, null, null); // Default 5 results
    }

    private GoogleSearchResponse createEmptyResponse() {
        GoogleSearchResponse errorResponse = new GoogleSearchResponse();
        errorResponse.setItems(Collections.emptyList());
        return errorResponse;
    }
}