package com.echo_english.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCreateRequest {
    @NotBlank(message = "Word cannot be blank")
    private String word;

    @NotBlank(message = "Definition cannot be blank")
    private String definition;

    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
}
