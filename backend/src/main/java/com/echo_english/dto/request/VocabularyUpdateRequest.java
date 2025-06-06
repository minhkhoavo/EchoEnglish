package com.echo_english.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String imageUrl; // Optional (cho ph�p null/r?ng �? x�a/kh�ng �?i)

    // Kh�ng c?n flashcardId v? ID t? v?ng �? ��?c truy?n v�o tr�n url

    // *** THÊM TRƯỜNG NÀY ***
    @NotNull(message = "Target Flashcard ID cannot be null") // Bắt buộc phải có ID flashcard mới/cũ
    private Long flashcardId; // ID của bộ thẻ mà từ vựng sẽ thuộc về SAU KHI cập nhật
}