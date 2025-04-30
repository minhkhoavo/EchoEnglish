package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.dto.response.SentenceAnalysisMetadata;
import com.echo_english.entity.SentenceAnalysisResult;
import com.echo_english.repository.SentenceAnalysisResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
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

@Service
@Slf4j
public class SpeechAnalyzeService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private SentenceAnalysisResultRepository resultRepository;

    public SpeechAnalyzeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
                throw new RuntimeException("Không thể đọc file mock", e);
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
            throw new RuntimeException("Không thể đọc InputStream từ MultipartFile", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://speech.mkhoavo.space/api/transcribe",
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Lỗi khi gọi API phân tích: " + response.getStatusCode());
        }

        PronunciationDTO dto = objectMapper.readValue(response.getBody(), PronunciationDTO.class);
        return dto.getMapping();
    }

    public String analyzeSentence(String targetWord, MultipartFile audioFile) throws Exception {
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
            log.error("Không thể đọc InputStream từ MultipartFile: " + e.getMessage());
            throw new RuntimeException("Lỗi xử lý file âm thanh đầu vào.", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://speech.mkhoavo.space/api/transcribe", requestEntity, String.class);

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String taskId = jsonNode.get("task_id").asText();

        SentenceAnalysisMetadata metadata = SentenceAnalysisMetadata.builder()
                .userId(getCurrentUserId())
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
//        try {
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
                    result.setStatus("completed");
                }
                else if ("error".equals(status)) {
                    result.setStatus("error");
                }
                resultRepository.save(result);
            }
//        } catch (Exception e) {
//            SentenceAnalysisResult result = resultRepository.findById(resultId).orElse(null);
//            if (result != null) {
//                result.setStatus("error");
//                resultRepository.save(result);
//            }
//        }
    }

    // Stub methods
    private String getCurrentUserId() {
        return "27";
    }

    private Mono<SentenceAnalysisResult> createResponseFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mock/mock_response.json")) {
            if (is == null) {
                throw new RuntimeException("mock_response.json not found!");
            }
            return Mono.just(mapper.readValue(is, SentenceAnalysisResult.class));
        } catch (IOException e) {
            throw new RuntimeException("Error when reading JSON", e);
        }
    }
}

