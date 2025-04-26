package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_history_detail")
public class TestHistoryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_history_id", nullable = false)
    @JsonBackReference
    private TestHistory testHistory;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;

    @ManyToOne
    @JoinColumn(name = "choice_id")
    private TestChoice choice;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}