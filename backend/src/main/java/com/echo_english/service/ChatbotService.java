package com.echo_english.service;

import com.echo_english.dto.request.ConverseRequest;
import com.echo_english.dto.request.StartConversationRequest;
import com.echo_english.dto.response.ChecklistItemResponse;
import com.echo_english.dto.response.ConversationResponse;
import com.echo_english.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ChatbotService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public ConversationResponse startConversation(StartConversationRequest request) {
        if (request.getContext() == null || request.getContext().trim().isEmpty()) {
            logger.warn("Received start request with empty or null context.");
            return new ConversationResponse("Please provide a context to start our conversation.", Collections.emptyList(), false);
        }
        if (request.getInitialUserInput() == null || request.getInitialUserInput().trim().isEmpty()) {
            logger.warn("Received start request with empty or null initial user input.");
            return new ConversationResponse("Please provide an initial message to start.", Collections.emptyList(), false);
        }

        List<Message> initialHistory = List.of(
                new UserMessage(request.getInitialUserInput())
        );

        Prompt initialPrompt = createInitialPrompt(request.getContext(), initialHistory);
        logger.info("Sending INITIAL prompt to AI for context: {}", request.getContext());

        try {
            ChatResponse response = chatClient.prompt(initialPrompt).call().chatResponse();
            String aiContent = getAiContentFromResponse(response);
            return parseAIResponse(aiContent, null, true);

        } catch (Exception e) {
            logger.error("Error calling AI service for initial context '{}': {}", request.getContext(), e.getMessage(), e);
            return new ConversationResponse("Sorry, I encountered an error starting the conversation.", Collections.emptyList(), false);
        }
    }

    public ConversationResponse continueConversation(ConverseRequest request) {
        if (request.getContext() == null || request.getContext().trim().isEmpty()) {
            logger.warn("Received continue request with empty or null context.");
            return new ConversationResponse("Context is missing for continuing conversation.", request.getCurrentChecklist(), false); // Return old checklist state
        }
        if (request.getCurrentChecklist() == null || request.getCurrentChecklist().isEmpty()) {
            logger.warn("Received continue request with empty or null checklist.");
            return new ConversationResponse("Conversation state error: Checklist is missing.", Collections.emptyList(), false);
        }
        if (request.getCurrentUserInput() == null || request.getCurrentUserInput().trim().isEmpty()) {
            logger.warn("Received continue request with empty or null current user input.");
            return new ConversationResponse("Please provide your response.", request.getCurrentChecklist(), false); // Return old checklist state
        }

        List<Message> chatHistory = buildChatHistoryForContinuation(request);
        Prompt subsequentPrompt = createSubsequentPrompt(request.getContext(), chatHistory, request.getCurrentChecklist());

        logger.info("Sending SUBSEQUENT prompt to AI for context: {}", request.getContext());

        try {
            ChatResponse response = chatClient.prompt(subsequentPrompt).call().chatResponse();
            String aiContent = getAiContentFromResponse(response);

            return parseAIResponse(aiContent, request.getCurrentChecklist(), false);

        } catch (Exception e) {
            logger.error("Error calling AI service for subsequent turn in context '{}': {}", request.getContext(), e.getMessage(), e);
            return new ConversationResponse("Sorry, I encountered an error processing your request.",
                    request.getCurrentChecklist(),
                    false);
        }
    }

    // Helper to build history for continuation (includes latest user input)
    private List<Message> buildChatHistoryForContinuation(ConverseRequest request) {
        List<Message> messages = new ArrayList<>();
        if (request.getHistory() != null) {
            request.getHistory().forEach(msg -> {
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            });
        }
        // Add the latest user input that triggered this turn
        messages.add(new UserMessage(request.getCurrentUserInput()));
        return messages;
    }

    private Prompt createInitialPrompt(String userContext, List<Message> initialHistory) {
        String systemMessageContent = String.format("""
                You are an advanced conversational AI assistant acting as a friendly tutor or guide...
                Conversation Context Provided by User: %s

                Your Tasks for this FIRST interaction:
                1.  **Generate Checklist:** ...
                2.  **Initial Evaluation:** ...
                3.  **Generate First Response:** ...
                4.  **Output:** Provide your response strictly in the following JSON format...
                    ```json
                    {
                      "aiResponse": "Your natural conversational reply directed at the user here...",
                      "updatedChecklist": [
                        { "id": "generated_id_1", "description": "Generated description 1 for user", "completed": boolean_value_based_on_user },
                        { "id": "generated_id_2", "description": "Generated description 2 for user", "completed": boolean_value_based_on_user },
                        ...
                      ]
                    }
                    ```
                """, userContext);

        SystemMessage systemMessage = new SystemMessage(systemMessageContent);
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(initialHistory);
        return new Prompt(allMessages);
    }


    private Prompt createSubsequentPrompt(String userContext, List<Message> chatHistory, List<ChecklistItemResponse> currentChecklist) {
        String systemMessageContent = String.format("""
                You are an advanced conversational AI assistant acting as a friendly tutor or guide.
                Your primary goal is to facilitate a natural conversation based on a specific context provided by the user, guiding the *user* to achieve specific conversational goals based on that context. You focus on evaluating the *user's* contributions against a PRE-DEFINED checklist.

                Conversation Context Provided by User: %s

                Existing Requirements Checklist (with current status):
                %s

                Your Tasks for this interaction:
                1.  **Analyze User History:** Carefully review the *entire* conversation history provided below, focusing specifically on the messages from the participant with the role 'user', *especially the latest message*.
                2.  **Evaluate Checklist Progress:** Review the 'Existing Requirements Checklist' provided above. For each item:
                    *   If its 'Current Status' is already 'Completed' (`true`), it MUST remain 'Completed' (`true`) in your output. **DO NOT change a completed item back to pending.**
                    *   If its 'Current Status' is 'Pending' (`false`), determine if the *user's contributions* in the conversation history (including the latest message) have *now sufficiently fulfilled* this requirement. Mark `completed` as `true` only if the *user* has clearly fulfilled it *in this turn or previously*.
                    *   Do *not* mark items complete based on your own contributions or questions the user asked you (unless the task was specifically for the user to ask you something).
                3.  **Generate Guiding Response:** Craft a natural, engaging, and encouraging response directed *specifically at the user*. Your primary goal is to prompt or guide the *user* towards fulfilling the *next uncompleted* checklist item (an item still marked as `false`). Ask clarifying questions or pose relevant prompts. If all items are now completed, congratulate the user. If the user asks an unrelated question, provide a brief answer and steer back to *their* goals if any remain.
                4.  **Output:** Provide your response strictly in the following JSON format, with no extra text before or after the JSON block:
                    ```json
                    {
                      "aiResponse": "Your natural conversational reply directed at the user here...",
                      "updatedChecklist": [
                        { "id": "existing_id_1", "description": "Existing description 1", "completed": updated_boolean_value },
                        { "id": "existing_id_2", "description": "Existing description 2", "completed": updated_boolean_value },
                        ...
                      ]
                    }
                    ```
                Ensure the `updatedChecklist` contains the *exact same items* (with same IDs and descriptions) as the 'Existing Requirements Checklist' provided, but with `completed` statuses updated according to rule #2 (completed items stay completed).
                """, userContext, currentChecklist);

        SystemMessage systemMessage = new SystemMessage(systemMessageContent);
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(chatHistory);
        return new Prompt(allMessages);
    }

    private ConversationResponse parseAIResponse(String aiContent, List<ChecklistItemResponse> originalChecklist, boolean isInitialRequest) {
        String jsonContent = JSONUtils.extractPureJson(aiContent);

        if (jsonContent == null) {
            logger.warn("AI response did not contain expected JSON format. Raw response: {}", aiContent);
            return new ConversationResponse(aiContent,
                    originalChecklist != null ? originalChecklist : Collections.emptyList(),
                    false);
        }

        try {
            Map<String, Object> parsedMap = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            String aiResponseText = (String) parsedMap.getOrDefault("aiResponse", "Sorry, I couldn't generate a proper response.");
            List<Map<String, Object>> checklistMaps = (List<Map<String, Object>>) parsedMap.get("updatedChecklist");
            List<ChecklistItemResponse> parsedChecklist = new ArrayList<>();

            if (checklistMaps != null) {
                for (Map<String, Object> itemMap : checklistMaps) {
                    String id = (String) itemMap.get("id");
                    String description;
                    if (isInitialRequest) {
                        description = (String) itemMap.getOrDefault("description", "No description provided");
                    } else {
                        description = findDescriptionInList(originalChecklist, id);
                    }

                    Object completedObj = itemMap.get("completed");
                    boolean completed = false;
                    if (completedObj instanceof Boolean) {
                        completed = (Boolean) completedObj;
                    } else if (completedObj instanceof String) {
                        completed = Boolean.parseBoolean((String) completedObj);
                    }

                    if (id != null) {
                        parsedChecklist.add(new ChecklistItemResponse(id, description, completed));
                    } else {
                        logger.warn("Parsed checklist item missing 'id': {}", itemMap);
                    }
                }
            } else {
                logger.warn("AI response JSON did not contain 'updatedChecklist'. Returning original/empty list.");
                parsedChecklist = originalChecklist != null ? originalChecklist : Collections.emptyList();
            }

            boolean allCompleted = !parsedChecklist.isEmpty() && parsedChecklist.stream().allMatch(ChecklistItemResponse::isCompleted);
            return new ConversationResponse(aiResponseText, parsedChecklist, allCompleted);

        } catch (Exception e) {
            logger.error("Error parsing AI JSON response: {}. Raw JSON content: {}", e.getMessage(), jsonContent, e);
            return new ConversationResponse("Sorry, I had trouble understanding the response format.",
                    originalChecklist != null ? originalChecklist : Collections.emptyList(),
                    false);
        }
    }

    private String findDescriptionInList(List<ChecklistItemResponse> checklist, String id) {
        if (checklist == null || id == null) {
            return "Unknown Requirement";
        }
        return checklist.stream()
                .filter(item -> id.equals(item.getId()))
                .map(ChecklistItemResponse::getDescription)
                .findFirst()
                .orElse("Description not found for ID: " + id);
    }
    private String getAiContentFromResponse(ChatResponse response) {
        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            if (response.getResult().getOutput().getText() != null) {
                return response.getResult().getOutput().getText();
            }
        }
        logger.warn("Could not extract content from ChatResponse: {}", response);
        return null;
    }
}