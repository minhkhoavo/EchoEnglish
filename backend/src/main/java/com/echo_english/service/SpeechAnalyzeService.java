package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.dto.response.SentenceAnalysisResultDTO;
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
        if(!targetWord.equals("test audio")) {
            throw new RuntimeException("The speech service currently not availble");
        }
        return createResponseFromJson();
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

