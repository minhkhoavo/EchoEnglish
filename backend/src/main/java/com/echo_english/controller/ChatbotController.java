package com.echo_english.controller;

import com.echo_english.service.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestBody String message) {

        String response = geminiService.callGeminiApi(message);

        // Xử lý JSON để lấy phần text
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode contentNode = candidatesNode.get(0).path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && partsNode.size() > 0) {
                    String text = partsNode.get(0).path("text").asText();
                    // Trả về phần text dưới dạng định dạng JSON nguyên gốc
                    return "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "{\"text\":\"Không thể lấy thông tin từ phản hồi.\"}";
    }
}