package com.echo_english.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlashcardUpdateRequest {
    @NotBlank(message = "Flashcard name cannot be blank")
    private String name;
    private String imageUrl;
}