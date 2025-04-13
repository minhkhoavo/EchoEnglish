package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "test_question_content")
public class TestQuestionContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer contentId;

    private String contentType;
    private String contentData;
    private Integer contentIndex;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonBackReference
    private TestQuestionGroup group;
}