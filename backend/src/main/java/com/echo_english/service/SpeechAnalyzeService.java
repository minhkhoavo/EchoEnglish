package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.entity.SentenceAnalysisResult;
import com.echo_english.dto.response.SentenceAnalyzeTaskIdResponse;
import com.echo_english.repository.SentenceAnalysisResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class SpeechAnalyzeService {
    private final WebClient webClient;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private SentenceAnalysisResultRepository resultRepository;

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


    public Mono<SentenceAnalysisResult> analyzeSentence(String targetWord, MultipartFile audioFile) {
        if(targetWord.equals("test audio")) {
            return createResponseFromJson(); // Ensure this mock method returns Mono<SentenceAnalysisResult>
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
                    try {
                        return audioFile.getSize();
                    } catch (IllegalStateException e) {
                        log.warn("Could not determine content length for multipart file", e);
                        return -1;
                    }
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
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), // Add error handling here too
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    return Mono.error(new RuntimeException("Lỗi khi gọi API tạo task phân tích câu: " + clientResponse.statusCode()));
                                })
                )
                .bodyToMono(SentenceAnalyzeTaskIdResponse.class)
                .flatMap(transcribeResponse -> {
                    if (transcribeResponse == null || transcribeResponse.getTaskId() == null) {
                        return Mono.error(new RuntimeException("No task ID returned from server."));
                    }
                    String taskId = transcribeResponse.getTaskId();
                    return pollResult(taskId, 25, Duration.ofSeconds(2));
                });
    }

    private Mono<SentenceAnalysisResult> pollResult(String taskId, int timeoutSeconds, Duration interval) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        String resultUrl = "/api/result/{taskId}";

        Mono<JsonNode> pollOperation = Mono.defer(() -> webClient.get()
                .uri(resultUrl, taskId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("Polling API failed: " + clientResponse.statusCode()))
                )
                .bodyToMono(JsonNode.class));

        return pollOperation
                .expand(currentNode -> {
                    String status = currentNode.path("status").asText("").toLowerCase();
                    boolean stillProcessing = "processing".equals(status);
                    boolean withinDeadline = System.currentTimeMillis() < deadline;

                    return stillProcessing && withinDeadline
                            ? Mono.delay(interval).then(pollOperation)
                            : Mono.empty();
                })
                .filter(node -> !"processing".equalsIgnoreCase(node.path("status").asText("")))
                .next()
                .flatMap(finalNode -> {
                    String finalStatus = finalNode.path("status").asText("").toLowerCase();
                    switch (finalStatus) {
                        case "completed":
                            try {
                                JsonNode resultData = finalNode.path("result");
                                SentenceAnalysisResult result = mapper.treeToValue(resultData, SentenceAnalysisResult.class);
                                return Mono.fromCallable(() -> resultRepository.save(result))
                                        .subscribeOn(Schedulers.boundedElastic());
                            } catch (JsonProcessingException e) {
                                return Mono.error(new RuntimeException("Result parsing failed", e));
                            }
                        case "error":
                            String errorMsg = finalNode.path("error").asText("Unknown server error");
                            return Mono.error(new RuntimeException("Task failed on server: " + errorMsg));
                        default:
                            return Mono.error(new RuntimeException("Task ended with unexpected status: " + finalStatus));
                    }
                })
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new RuntimeException("Polling timeout for task " + taskId))
                ))
                .onErrorMap(DataAccessException.class,
                        e -> new RuntimeException("Database save failed", e)
                );
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

