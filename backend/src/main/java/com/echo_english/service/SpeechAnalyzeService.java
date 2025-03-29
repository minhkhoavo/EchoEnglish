package com.echo_english.service;
import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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

    public Mono<List<PhonemeComparisonDTO>> analyzeSpeech() {
        // ================================================
        // Only for mock api, update later
        FileSystemResource audioFile = new FileSystemResource("C:/Users/ADMIN/Downloads/test/elsa speak clone/words/about.mp3");

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("target_word", "about");
        formData.add("audio_file", audioFile);
        formData.add("audio_type", "single");
        // ================================================

        return webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> parseAndCompare(response));  // Chuyển đổi JSON thành List<PhonemeComparisonDTO>
    }

    private Mono<List<PhonemeComparisonDTO>> parseAndCompare(String response) {
        try {
            JsonNode root = mapper.readTree(response);

            List<String> targetPhonemes = mapper.convertValue(
                    root.get("target_ipa_no_stress"),
                    new TypeReference<List<String>>() {});

            List<String> actualPhonemes = mapper.convertValue(
                    root.get("transcription_no_stress"),
                    new TypeReference<List<String>>() {});

            String targetWord = root.get("target_word").asText();

            List<List<String>> grapheneIpaMapping = mapper.convertValue(
                    root.get("graphene_ipa_mapping"),
                    new TypeReference<List<List<String>>>() {});

            return Mono.just(comparePhonemes(targetPhonemes, actualPhonemes, targetWord, grapheneIpaMapping));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public List<PhonemeComparisonDTO> comparePhonemes(List<String> targetPhonemes,
                                                      List<String> actualPhonemes,
                                                      String targetWord,
                                                      List<List<String>> grapheneIpaMapping) {
        // Tạo danh sách chứa các cặp (start_index, end_index) ứng với từng âm vị trong targetPhonemes
        List<int[]> mappingIndices = new ArrayList<>();
        int mappingIdx = 0;
        for (String phon : targetPhonemes) {
            if (mappingIdx >= grapheneIpaMapping.size()) {
                mappingIndices.add(new int[]{-1, -1});
                continue;
            }
            int start = mappingIdx;
            // Nhóm các entry liên tiếp trong grapheneIpaMapping có âm vị bằng phon
            while (mappingIdx < grapheneIpaMapping.size() &&
                    grapheneIpaMapping.get(mappingIdx).get(1).equals(phon)) {
                mappingIdx++;
            }
            int end = (mappingIdx > start) ? mappingIdx - 1 : start;
            mappingIndices.add(new int[]{start, end});
        }

        List<PhonemeComparisonDTO> resultList = new ArrayList<>();
        int targetIdx = 0;  // chỉ số cho targetPhonemes
        int actualIdx = 0;  // chỉ số cho actualPhonemes

        // Duyệt qua targetPhonemes để so sánh
        while (targetIdx < targetPhonemes.size()) {
            int startIdx = -1;
            int endIdx = -1;
            if (targetIdx < mappingIndices.size()) {
                int[] indices = mappingIndices.get(targetIdx);
                startIdx = indices[0];
                endIdx = indices[1];
            }
            if (actualIdx < actualPhonemes.size()) {
                // Trường hợp 1: Khớp trực tiếp
                if (targetPhonemes.get(targetIdx).equals(actualPhonemes.get(actualIdx))) {
                    PhonemeComparisonDTO dto = new PhonemeComparisonDTO(
                            "correct",
                            startIdx,
                            actualPhonemes.get(actualIdx),
                            endIdx,
                            targetPhonemes.get(targetIdx)
                    );
                    resultList.add(dto);
                    targetIdx++;
                    actualIdx++;
                }
                // Trường hợp 2: Âm vị đa ký tự trong target (ví dụ: "ju" khớp với ["j", "u"])
                else if (targetPhonemes.get(targetIdx).length() > 1 &&
                        actualIdx + targetPhonemes.get(targetIdx).length() <= actualPhonemes.size() &&
                        compareSublist(actualPhonemes, actualIdx, targetPhonemes.get(targetIdx))) {
                    StringBuilder actualCombined = new StringBuilder();
                    int len = targetPhonemes.get(targetIdx).length();
                    for (int j = 0; j < len; j++) {
                        actualCombined.append(actualPhonemes.get(actualIdx + j));
                    }
                    PhonemeComparisonDTO dto = new PhonemeComparisonDTO(
                            "correct",
                            startIdx,
                            actualCombined.toString(),
                            endIdx,
                            targetPhonemes.get(targetIdx)
                    );
                    resultList.add(dto);
                    targetIdx++;
                    actualIdx += len;
                }
                // Trường hợp 3: Không khớp
                else {
                    PhonemeComparisonDTO dto = new PhonemeComparisonDTO(
                            "incorrect",
                            startIdx,
                            actualPhonemes.get(actualIdx),
                            endIdx,
                            targetPhonemes.get(targetIdx)
                    );
                    resultList.add(dto);
                    targetIdx++;
                    actualIdx++;
                }
            } else {
                // Nếu actualPhonemes đã hết nhưng targetPhonemes chưa, đánh dấu "N/A"
                PhonemeComparisonDTO dto = new PhonemeComparisonDTO(
                        "incorrect",
                        startIdx,
                        "N/A",
                        endIdx,
                        targetPhonemes.get(targetIdx)
                );
                resultList.add(dto);
                targetIdx++;
            }
        }
        return resultList;
    }

    /**
     * So sánh một đoạn của actualPhonemes với các ký tự của correctString.
     */
    private static boolean compareSublist(List<String> actualPhonemes, int startIndex, String correctString) {
        for (int i = 0; i < correctString.length(); i++) {
            if (!actualPhonemes.get(startIndex + i).equals(String.valueOf(correctString.charAt(i)))) {
                return false;
            }
        }
        return true;
    }
}

