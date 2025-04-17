package com.echo_english.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flashcard_learning_history")
public class FlashcardLearningHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- ĐÃ LOẠI BỎ ---
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "flashcard_id", nullable = false)
    // private Flashcard flashcard;

    // --- THAY THẾ BẰNG ---
    // Liên kết chính tới từ vựng đã học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false) // Foreign key đến bảng vocabulary
    private Vocabulary vocabulary;

    @Column(name = "learned_at")
    private LocalDateTime learnedAt; // Thời điểm học/ghi nhận cuối cùng

    // --- THAY THẾ is_remember ---
    // Đếm số lần học/nhớ
    @Column(name = "remember_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int rememberCount; // 0 = chưa bao giờ ghi nhận, 1 = ghi nhận lần 1, ...
}