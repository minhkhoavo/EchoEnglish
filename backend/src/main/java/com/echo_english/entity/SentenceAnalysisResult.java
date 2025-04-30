package com.echo_english.entity;

import com.echo_english.dto.response.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "sentence_analysis_results")
public class SentenceAnalysisResult {
    @Id
    private String id;
    private SentenceAnalysisMetadata metadata;

    private String text;
    private String status;
    private List<WordDetailDTO> chunks;
    private SentenceSummaryDTO summary;
    @JsonProperty("phoneme_statistics")
    @Field("phoneme_statistics")
    private List<PhonemeStatsDTO> phonemeStatistics;

    //    private List<> wordTranscriptions;
    public List<PhonemeStatsDTO> calculatePhonemeStatistics() {
        Map<String, int[]> statsMap = new HashMap<>(); // <phoneme, [totalCount, correctCount]>

        if (chunks != null) {
            for (WordDetailDTO word : chunks) {
                if (word.getPronunciation() != null && word.getPronunciation().getMapping() != null) {
                    for (PhonemeComparisonDTO mapping : word.getPronunciation().getMapping()) {
                        String phoneme = mapping.getCorrectPhoneme();
                        statsMap.putIfAbsent(phoneme, new int[]{0, 0});
                        statsMap.get(phoneme)[0]++;

                        if ("correct".equals(mapping.getResult())) {
                            statsMap.get(phoneme)[1]++;
                        }
                    }
                }
            }
        }

        List<PhonemeStatsDTO> statsList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : statsMap.entrySet()) {
            String phoneme = entry.getKey();
            int totalCount = entry.getValue()[0];
            int correctCount = entry.getValue()[1];
            statsList.add(new PhonemeStatsDTO(phoneme, totalCount, correctCount));
        }
        return statsList;
    }
}
