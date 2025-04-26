package com.echo_english.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StartTestRequest {
    private Long userId;
    private Integer testId;
    private Integer partId;
    private Integer totalQuestions;
}