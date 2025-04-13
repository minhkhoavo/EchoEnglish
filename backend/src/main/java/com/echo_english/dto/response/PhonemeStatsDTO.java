package com.echo_english.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhonemeStatsDTO {
    private String phoneme;
    private int totalCount;
    private int correctCount;
}