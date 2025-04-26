package com.echo_english.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SentenceAnalyzeTaskIdResponse {
    @JsonProperty("task_id")
    private String taskId;
    private String status;
}
