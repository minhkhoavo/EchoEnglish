package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WordDetailDTO {
    private String text;
    private double start_time;
    private double end_time;
    private AnalysisDetail analysis;
    private String error;
    private PronunciationDTO pronunciation;
}
