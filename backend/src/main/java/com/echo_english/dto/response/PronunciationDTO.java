package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PronunciationDTO {
    private String target_word;
    private String target_ipa;
    private List<String> target_ipa_no_stress;
    private String transcription_ipa;
    private List<String> transcription_no_stress;
    private double similarity;
    private List<PhonemeComparisonDTO> mapping;
}
