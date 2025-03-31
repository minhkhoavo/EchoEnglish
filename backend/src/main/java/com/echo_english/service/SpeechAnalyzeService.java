package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.PronunciationDTO;
import com.echo_english.dto.response.SentenceAnalysisResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class SpeechAnalyzeService {
    private final WebClient webClient;
    @Autowired
    private ObjectMapper mapper;

    public SpeechAnalyzeService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://d2a33b4f-f0f9-4b87-a012-c07684d3916e.mock.pstmn.io")
                .build();
        this.mapper = new ObjectMapper();
    }

    public Mono<List<PhonemeComparisonDTO>> analyzeSpeech(String targetWord, MultipartFile audioFile) {
        // ================================================
        // Only for mock API, update later
        try {
            String fileName = String.format("mock/word_%s.json", targetWord);
            ClassPathResource resource = new ClassPathResource(fileName);
            PronunciationDTO dto = mapper.readValue(resource.getInputStream(), PronunciationDTO.class);
            System.out.println("Passs:::::::");
            return Mono.just(dto.getMapping());
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Mock data file not found for word: " + targetWord, e));
        }
        // ================================================

//        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
//        formData.add("target_word", targetWord);
//        formData.add("audio_file", audioFile);
//        formData.add("audio_type", "single");
//
//        return webClient.post()
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(BodyInserters.fromMultipartData(formData))
//                .retrieve()
//                .bodyToMono(PronunciationDTO.class)
//                .map(PronunciationDTO::getMapping);
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

