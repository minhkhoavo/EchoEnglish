package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyResponse {
    private Long id;
    private String word;
    private String definition;
    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
}