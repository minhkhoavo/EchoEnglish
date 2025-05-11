package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ErrorDetail {
    private String type;
    private String originalText;
    private String correctionText;
    private String explanation;
    private String severityColor; // "high", "medium", "low"
}