package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyReviewResponse {
    // Vocabulary Details (similar to VocabularyResponse)
    private Long id;
    private String word;
    private String definition;
    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;

    // Learning History Details (relevant for review)
    private Long learningHistoryId; // The ID of the history record itself
    private int rememberCount;     // How many times remembered
    private LocalDateTime learnedAt;   // When it was last learned/forgotten
}