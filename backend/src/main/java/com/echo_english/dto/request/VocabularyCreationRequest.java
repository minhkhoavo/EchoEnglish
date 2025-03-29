package com.echo_english.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VocabularyCreationRequest {
    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String definition;

    @Column(nullable = false)
    private String pronunciation;

    private String image;

    private String example;

    private Integer status = 0;

    private String category_id;
}
