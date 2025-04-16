package com.echo_english.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
@Slf4j
public class UrlContentFetcher {
    private final WebClient webClient;

    public UrlContentFetcher(WebClient.Builder webClientBuilder) {
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(10485760))
                .build();
//        log.info("Configuring WebClient with maxInMemorySize: {} bytes", 10485760);

        this.webClient = webClientBuilder
                .exchangeStrategies(strategies)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();
    }

    public Mono<String> fetchHtmlContent(String url) {
        log.info("Starting to fetch content from URL: {}", url);
        return this.webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(content -> log.info("Successfully fetched content from URL: {}", url))
                .doOnError(error -> log.error("Error fetching content from URL {}: {}", url, error.getMessage()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("HTTP error {} when accessing URL {}: {}", ex.getStatusCode(), url, ex.getResponseBodyAsString());
                    return Mono.just("HTTP error " + ex.getStatusCode() + " when accessing the URL.");
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Other error when accessing URL {}: {}", url, ex.getMessage());
                    String errorMessage = "Unable to connect or fetch content from URL. Error: " + ex.getClass().getSimpleName();
                    if (ex.getMessage() != null) {
                        errorMessage += " - " + ex.getMessage();
                    }
                    return Mono.just(errorMessage);
                });
    }
}







