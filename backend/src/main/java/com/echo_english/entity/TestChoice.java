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
@Table(name = "test_choice")
public class TestChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer choiceId;

    private String choiceLabel;
    private String choiceText;
    private String choiceExplanation;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference
    private TestQuestion question;
}
