package com.echo_english.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private String overview;
    private List<ErrorDetail> errors;
    private String suggestion;
}
