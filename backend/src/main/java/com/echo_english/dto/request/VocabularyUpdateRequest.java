package com.echo_english.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VocabularyUpdateRequest {

    @NotBlank(message = "Word cannot be blank")
    private String word;

    @NotBlank(message = "Definition cannot be blank")
    private String definition;

    private String phonetic; // Optional
    private String example;  // Optional
    private String type;     // Optional
    private String imageUrl; // Optional (cho phép null/rỗng để xóa/không đổi)

    // Không cần flashcardId vì ID từ vựng đã xác định flashcard cha
}