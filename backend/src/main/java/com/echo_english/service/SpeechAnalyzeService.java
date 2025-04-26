package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.dto.response.SentenceAnalysisResultDTO;
import com.echo_english.dto.response.SentenceAnalyzeTaskIdResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class SpeechAnalyzeService {
    private final WebClient webClient;
    @Autowired
    private ObjectMapper mapper;

    public SpeechAnalyzeService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://speech.mkhoavo.space/")
                .build();
        this.mapper = new ObjectMapper();
    }

    public Mono<List<PhonemeComparisonDTO>> analyzeSpeech(String targetWord, MultipartFile audioFile) throws IOException {
        // ================================================
        // Only for mock API, update later
        if(targetWord.equals("mock")) {
            String fileName = String.format("mock/word_%s.json", targetWord);
            ClassPathResource resource = new ClassPathResource(fileName);
            PronunciationDTO dto = mapper.readValue(resource.getInputStream(), PronunciationDTO.class);
            return Mono.just(dto.getMapping());
        }
        // ================================================

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("target_word", targetWord);
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
            log.error("Không thể đọc InputStream từ MultipartFile: " + e.getMessage());
            return Mono.error(new RuntimeException("Lỗi xử lý file âm thanh đầu vào.", e));
        }
        formData.add("audio_type", "single");

        return webClient.post()
                .uri("https://speech.mkhoavo.space/api/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.printf("Lỗi từ API ngoài: Status=%s, Body=%s%n", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("Lỗi khi gọi API phân tích: " + clientResponse.statusCode()));
                                })
                )
                .bodyToMono(PronunciationDTO.class)
                .map(PronunciationDTO::getMapping);
    }


    public Mono<SentenceAnalysisResultDTO> analyzeSentence(String targetWord, MultipartFile audioFile) {
        if(targetWord.equals("test audio")) {
            return createResponseFromJson();
        }

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("target_word", targetWord);

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
            log.error("Cannot read InputStream from MultipartFile: " + e.getMessage());
            return Mono.error(new RuntimeException("Error processing input audio file.", e));
        }
        formData.add("audio_type", "sentence");

        return webClient.post()
                .uri("https://speech.mkhoavo.space/api/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(SentenceAnalyzeTaskIdResponse.class)
                .flatMap(transcribeResponse -> {
                    if (transcribeResponse == null || transcribeResponse.getTaskId() == null) {
                        return Mono.error(new RuntimeException("No task ID returned from server."));
                    }
                    String taskId = transcribeResponse.getTaskId();
                    return pollResult(taskId, 25, Duration.ofSeconds(2)); // Step 2: poll /api/result/{taskId}
                });

    }

    private Mono<SentenceAnalysisResultDTO> pollResult(String taskId, int timeoutSeconds, Duration interval) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        return Mono.defer(() -> webClient.get()
                        .uri("https://speech.mkhoavo.space/api/result/{taskId}", taskId)
                        .retrieve()
                        .bodyToMono(JsonNode.class))
                .expand(jsonNode -> {
                    String status = jsonNode.path("status").asText();
                    if ("processing".equalsIgnoreCase(status) && System.currentTimeMillis() < deadline) {
                        return Mono.delay(interval).then(Mono.defer(() -> webClient.get()
                                .uri("https://speech.mkhoavo.space/api/result/{taskId}", taskId)
                                .retrieve()
                                .bodyToMono(JsonNode.class)));
                    } else {
                        return Mono.empty();
                    }
                })
                .filter(jsonNode -> !"processing".equalsIgnoreCase(jsonNode.path("status").asText()))
                .next()
                .flatMap(jsonNode -> {
                    String status = jsonNode.path("status").asText();
                    if ("completed".equalsIgnoreCase(status)) {
                        JsonNode resultNode = jsonNode.path("result");
                        try {
                            SentenceAnalysisResultDTO result = new ObjectMapper().treeToValue(resultNode, SentenceAnalysisResultDTO.class);
                            return Mono.just(result);
                        } catch (JsonProcessingException e) {
                            return Mono.error(new RuntimeException("Failed to parse result.", e));
                        }
                    } else if ("error".equalsIgnoreCase(status)) {
                        String errorMessage = jsonNode.path("error").asText("Unknown error");
                        return Mono.error(new RuntimeException("Error during processing: " + errorMessage));
                    } else {
                        return Mono.error(new RuntimeException("Unknown status: " + status));
                    }
                });
    }

    private Mono<SentenceAnalysisResultDTO> createResponseFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mock/mock_response.json")) {
            if (is == null) {
                throw new RuntimeException("mock_response.json not found!");
            }
            return Mono.just(mapper.readValue(is, SentenceAnalysisResultDTO.class));
        } catch (IOException e) {
            throw new RuntimeException("Error when reading JSON", e);
        }
    }
}

