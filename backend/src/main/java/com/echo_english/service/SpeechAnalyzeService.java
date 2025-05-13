package com.echo_english.service;
import com.echo_english.dto.response.FeedbackResponse;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.dto.response.SentenceAnalysisMetadata;
import com.echo_english.entity.SentenceAnalysisResult;
import com.echo_english.entity.Word;
import com.echo_english.repository.SentenceAnalysisResultRepository;
import com.echo_english.utils.AuthUtil;
import com.echo_english.utils.JSONUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpeechAnalyzeService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private WordService wordService;

    @Autowired
    private SentenceAnalysisResultRepository resultRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public SpeechAnalyzeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<SentenceAnalysisResult> getSentenceResultsByCurrentUser() {
        Query query = new Query();
        query.addCriteria(Criteria.where("metadata.userId").is(AuthUtil.getUserId()));
        query.with(Sort.by(Sort.Order.desc("metadata.createdAt")));
        return mongoTemplate.find(query, SentenceAnalysisResult.class);
    }

    public List<PhonemeComparisonDTO> analyzeSpeech(String targetWord, MultipartFile audioFile) throws JsonProcessingException {
        // ================================================
        // Only for mock API, update later
        if (targetWord.equals("mock")) {
            try {
                String fileName = String.format("mock/word_%s.json", targetWord);
                ClassPathResource resource = new ClassPathResource(fileName);
                PronunciationDTO dto = objectMapper.readValue(resource.getInputStream(), PronunciationDTO.class);
                return dto.getMapping();
            } catch (IOException e) {
                throw new RuntimeException("Cannot read mock file", e);
            }
        }
        // ================================================

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("target_word", targetWord);
        formData.add("audio_type", "single");

        try {
            Resource audioResource = new InputStreamResource(audioFile.getInputStream()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }

                @Override
                public long contentLength() throws IOException {
                    return audioFile.getSize();
                }
            };
            formData.add("audio_file", audioResource);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read InputStream from MultipartFile", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://speech.mkhoavo.space/api/transcribe",
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error: " + response.getStatusCode());
        }

        PronunciationDTO dto = objectMapper.readValue(response.getBody(), PronunciationDTO.class);
        return dto.getMapping();
    }

    public String analyzeSentence(String targetWord, MultipartFile audioFile) throws Exception {
        if (targetWord.equals("test audio")) {
            createResponseFromJson();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("target_word", targetWord);
        body.add("audio_type", "sentence");
        try {
            Resource audioResource = new InputStreamResource(audioFile.getInputStream()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }

                @Override
                public long contentLength() throws IOException {
                    return audioFile.getSize();
                }
            };
            body.add("audio_file", audioResource);
        } catch (IOException e) {
            log.error("Cannot read InputStream from MultipartFile: " + e.getMessage());
            throw new RuntimeException("Error when processing audio.", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://speech.mkhoavo.space/api/transcribe", requestEntity, String.class);

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String taskId = jsonNode.get("task_id").asText();

        SentenceAnalysisMetadata metadata = SentenceAnalysisMetadata.builder()
                .userId(AuthUtil.getUserId())
                .taskId(taskId)
                .targetWord(targetWord)
                .createdAt(LocalDateTime.now())
                .audioName(audioFile.getOriginalFilename())
                .audioSize(audioFile.getSize())
                .audioContentType(audioFile.getContentType())
                .build();

        SentenceAnalysisResult result = SentenceAnalysisResult.builder()
                .metadata(metadata)
                .status("processing")
                .build();

        resultRepository.save(result);
        pollForResultAsync(taskId, result.getId());
        return result.getId();
    }

    @Async
    public void pollForResultAsync(String taskId, String resultId) throws IOException {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "https://speech.mkhoavo.space/api/result/" + taskId, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String status = jsonNode.get("status").asText();

            SentenceAnalysisResult result = resultRepository.findById(resultId).orElse(null);
            if (result != null) {
                if ("completed".equals(status)) {
                    JsonNode resultNode = jsonNode.get("result");
                    objectMapper.readerForUpdating(result).readValue(resultNode.toString());
                    result.setPhonemeStatistics(result.calculatePhonemeStatistics());
                    result.setWordLevelCount(wordService.countWordsByCEFRLevel(result.getText()));
                    result.setFeedback(getFeedback(result.getText()));
                    result.setStatus("completed");
                }
                else if ("error".equals(status)) {
                    result.setStatus("error");
                }
                resultRepository.save(result);
            }
        } catch (Exception e) {
            SentenceAnalysisResult result = resultRepository.findById(resultId).orElse(null);
            if (result != null) {
                result.setStatus("error");
                resultRepository.save(result);
            }
            log.error("Poll result error: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 10000)  // 10s
    public void scheduledPollForResults() throws IOException {
        List<SentenceAnalysisResult> incompleteResults = resultRepository.findByStatus("processing");

        for (SentenceAnalysisResult result : incompleteResults) {
            String taskId = result.getMetadata().getTaskId();
            String resultId = result.getId();
            pollForResultAsync(taskId, resultId);
        }
    }

    public FeedbackResponse getFeedback(String inputText) {
        String jsonFormatInstruction = """
               {
                 "overview": "string (concise overall feedback, link content ideas if possible)",
                 "errors": [
                   {
                     "type": "string (e.g., Tense Error, Sentence Structure)",
                     "originalText": "string (the original incorrect text snippet)",
                     "correctionText": "string (the corrected text snippet)",
                     "explanation": "string (concise explanation of the error, max 1-2 sentences)",
                     "severityColor": "string (must be 'high', 'medium', or 'low')"
                   }
                 ],
                 "suggestion": "string (concise overall suggestion for improvement, max 2-3 sentences)"
               }
               """;

        String systemPromptContent = String.format("""
            You are an AI assistant specialized in English grammar analysis and correction.
            Your task is to analyze the provided text and return your findings as a JSON object.
            The JSON response MUST strictly follow this structure and use the specified field names:
            %s
            Ensure 'overview', 'explanation' for each error, and 'suggestion' are very concise.
            For 'explanation' and 'suggestion', keep them to 1-2 sentences at most.
            'severityColor' must be one of 'high', 'medium', or 'low'.
            Do NOT include any text outside of the JSON object itself.
            """, jsonFormatInstruction);

        String userPromptContent = "Please analyze the following English text and provide feedback in the specified JSON format:\n\n" +
                "Text to analyze:\n\"" + inputText + "\"";

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPromptContent),
                new UserMessage(userPromptContent)
        ));

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String aiJsonResponse = response.getResult().getOutput().getText();

        try {
            return objectMapper.readValue(JSONUtils.extractPureJson(aiJsonResponse), FeedbackResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI response into FeedbackResponse object.", e);
        }
    }
    private SentenceAnalysisResult createResponseFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mock/mock_response.json")) {
            if (is == null) {
                throw new RuntimeException("mock_response.json not found!");
            }
            return mapper.readValue(is, SentenceAnalysisResult.class);
        } catch (IOException e) {
            throw new RuntimeException("Error when reading JSON", e);
        }
    }
}

