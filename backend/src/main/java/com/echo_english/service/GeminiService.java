package com.echo_english.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    private final WebClient webClient;

    @Autowired
    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String callGeminiApi(String message) {
        // Tạo đối tượng request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", "gemini-1.5-flash");

        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");

        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", message);
        parts.add(part);

        content.put("parts", parts);
        contents.add(content);
        requestBodyMap.put("contents", contents);

        try {
            // Chuyển đổi requestBodyMap thành JSON String
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            // Gửi yêu cầu POST bằng WebClient và nhận phản hồi
            String response = webClient.post()
                    .uri("/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(responseBody -> {
                                    return Mono.error(new RuntimeException("Error: " + responseBody));
                                });
                    })
                    .bodyToMono(String.class)
                    .block(); // Chặn cho đến khi nhận được phản hồi

            // Trả về phản hồi từ API
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}