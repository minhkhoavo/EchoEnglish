package com.echo_english.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder@JsonIgnoreProperties(ignoreUnknown = true)
public class SentenceAnalysisResultDTO {
    private String text;
    private List<WordDetailDTO> chunks;
    private SentenceSummaryDTO summary;
//    private List<> wordTranscriptions;
}
