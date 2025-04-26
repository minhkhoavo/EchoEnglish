package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
