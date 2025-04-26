package com.echo_english.controller;

import com.echo_english.ai.tools.WebContentTools;
import com.echo_english.dto.request.ConverseRequest;
import com.echo_english.dto.request.StartConversationRequest;
import com.echo_english.dto.response.ConversationResponse;
import com.echo_english.service.ChatbotService;
import com.echo_english.service.GeminiService;
import com.echo_english.service.MailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private GeminiService geminiService;
    @Autowired
    private WebContentTools webContentTools;
    @Autowired
    private ChatbotService chatbotService;

    @GetMapping("/test")
    public String testChatWithTool() {
        String response = chatClient
                .prompt("Send mail with a random number between 1 and 100, Please choose a random number and send it by yourself")
                .tools(new MailService())
                .call()
                .content();

        System.out.println(response);
        return response;
    }
    @PostMapping("/ask")
    public String chat(@RequestBody String userInput) {
        return chatClient.prompt(userInput).call().content();
    }

    @PostMapping("/start")
    public ConversationResponse startChat(@RequestBody StartConversationRequest request) {
        return chatbotService.startConversation(request);
    }

    @PostMapping("/converse")
    public ConversationResponse continueChat(@RequestBody ConverseRequest request) {
        return chatbotService.continueConversation(request);
    }


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
                    return "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "{\"text\":\"Không thể lấy thông tin từ phản hồi.\"}";
    }
    @GetMapping("/ai/summarize-url")
    public String summarizeUrl(@RequestParam String url) {
        url = "https://edition.cnn.com/2025/04/12/tech/trump-electronics-china-tariffs/index.html";
        String userMessage = "Hãy truy cập vào URL sau và tóm tắt nội dung chính của nó: " + url;

        return chatClient.prompt()
                .user(userMessage)
                .tools(webContentTools)
                .call()
                .content();
    }
}