package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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