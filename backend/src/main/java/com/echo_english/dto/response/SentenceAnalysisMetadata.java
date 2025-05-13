package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SentenceAnalysisMetadata {
    private Long userId;
    private String taskId;
    private String targetWord;
    private LocalDateTime createdAt;

    private String audioName;
    private Long audioSize;
    private String audioContentType;
    private Double audioDuration;
}

