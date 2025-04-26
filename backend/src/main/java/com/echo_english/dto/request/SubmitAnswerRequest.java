package com.echo_english.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmitAnswerRequest {
    private Long testHistoryId; // ID of the ongoing test attempt
    private Integer questionId; // ID of the question being answered
    private Integer choiceId;   // ID of the choice selected by the user
}