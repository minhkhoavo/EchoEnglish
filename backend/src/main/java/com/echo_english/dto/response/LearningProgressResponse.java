package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressResponse {
    private long flashcardId;
    private long userId;
    private int totalVocabularies;    // Tổng số từ trong bộ thẻ
    private int learnedVocabularies; // Số từ đã học (có trong history)
    private double completionPercentage; // Tỷ lệ hoàn thành (0.0 -> 100.0)
}