package com.echo_english.repository;

import com.echo_english.entity.FlashcardLearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardLearningHistoryRepository extends JpaRepository<FlashcardLearningHistory, Long> {
    List<FlashcardLearningHistory> findByUserId(Long userId);
    Optional<FlashcardLearningHistory> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);


    // Lấy tất cả bản ghi lịch sử của người dùng (để lọc trong Service cho các từ đến hạn)
    // Sử dụng JOIN FETCH để lấy Vocabulary và Flashcard tránh N+1 khi mapping VocabularyReviewResponse
    @Query("SELECT h FROM FlashcardLearningHistory h JOIN FETCH h.vocabulary v LEFT JOIN FETCH v.flashcard f WHERE h.user.id = :userId")
    List<FlashcardLearningHistory> findByUserIdWithVocabularyAndFlashcard(@Param("userId") Long userId);

    // Phương thức đếm theo rememberCount cho một người dùng (TOÀN BỘ từ vựng)
    @Query("SELECT COUNT(h) FROM FlashcardLearningHistory h WHERE h.user.id = :userId AND h.rememberCount = :rememberCount")
    long countByUserIdAndRememberCount(@Param("userId") Long userId, @Param("rememberCount") int rememberCount);

    @Query("SELECT COUNT(h) FROM FlashcardLearningHistory h WHERE h.user.id = :userId AND h.rememberCount >= :rememberCount")
    long countByUserIdAndRememberCountGreaterThanEqual(@Param("userId") Long userId, @Param("rememberCount") int rememberCount);

    // Các phương thức đếm theo flashcardId (có thể giữ lại nếu cần ở chỗ khác, nhưng không dùng cho SRS toàn cầu)
    @Query("SELECT COUNT(DISTINCT h.vocabulary.id) FROM FlashcardLearningHistory h WHERE h.user.id = :userId AND h.vocabulary.flashcard.id = :flashcardId AND h.rememberCount >= 1")
    long countLearnedVocabulariesByUserIdAndFlashcardId(@Param("userId") Long userId, @Param("flashcardId") Long flashcardId);

    @Query("SELECT COUNT(h) FROM FlashcardLearningHistory h WHERE h.user.id = :userId AND h.vocabulary.flashcard.id = :flashcardId AND h.rememberCount = :rememberCount")
    long countByUserIdAndFlashcardIdAndRememberCount(@Param("userId") Long userId, @Param("flashcardId") Long flashcardId, @Param("rememberCount") int rememberCount);

    @Query("SELECT COUNT(h) FROM FlashcardLearningHistory h WHERE h.user.id = :userId AND h.vocabulary.flashcard.id = :flashcardId AND h.rememberCount >= :rememberCount")
    long countByUserIdAndFlashcardIdAndRememberCountGreaterThanEqual(@Param("userId") Long userId, @Param("flashcardId") Long flashcardId, @Param("rememberCount") int rememberCount);

}
