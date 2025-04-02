package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisDetail {
    private double pitch;
    private double intensity;
    private String stress_level;
    private double variation;
}
