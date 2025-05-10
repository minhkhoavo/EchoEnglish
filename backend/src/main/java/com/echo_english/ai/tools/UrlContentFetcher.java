package com.echo_english.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UrlContentFetcher {
    private final RestTemplate restTemplate;

    public UrlContentFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchHtmlContent(String url) {
        log.info("Starting to fetch content from URL: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully fetched content from URL: {}", url);
                return response.getBody();
            } else {
                log.warn("HTTP request to {} was not successful or body is null. Status: {}", url, response.getStatusCode());
                return "HTTP error " + response.getStatusCodeValue() + " when accessing the URL.";
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error {} when accessing URL {}: {}", e.getStatusCode(), url, e.getResponseBodyAsString(), e);
            return "HTTP error " + e.getStatusCode() + " when accessing the URL.";
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException (e.g., timeout, connection refused) when accessing URL {}: {}", url, e.getMessage(), e);
            return "Unable to connect or fetch content from URL. Error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        } catch (Exception e) {
            log.error("Other error when accessing URL {}: {}", url, e.getMessage(), e);
            String errorMessage = "Unable to connect or fetch content from URL. Error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMessage += " - " + e.getMessage();
            }
            return errorMessage;
        }
    }
}