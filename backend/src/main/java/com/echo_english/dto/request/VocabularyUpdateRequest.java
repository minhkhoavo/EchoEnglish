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
    private String imageUrl; // Optional (cho phép null/r?ng ð? xóa/không ð?i)

    // Không c?n flashcardId v? ID t? v?ng ð? ðý?c truy?n vào trên url
}