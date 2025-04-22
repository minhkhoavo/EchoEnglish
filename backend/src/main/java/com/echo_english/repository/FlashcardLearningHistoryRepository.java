package com.echo_english.repository;

import com.echo_english.entity.FlashcardLearningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardLearningHistoryRepository extends JpaRepository<FlashcardLearningHistory, Long> {
    List<FlashcardLearningHistory> findByUserId(Long userId);
    Optional<FlashcardLearningHistory> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);
 }
