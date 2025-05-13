package com.echo_english.repository;

import com.echo_english.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    // Tìm TẤT CẢ flashcards thuộc một category cụ thể (dùng cho user-defined category 1)
    List<Flashcard> findByCategoryId(Long categoryId);
    // Tìm TẤT CẢ flashcards KHÔNG thuộc một category cụ thể (dùng cho public)
    List<Flashcard> findByCategoryIdNot(Long categoryId);

    List<Flashcard> findByCreatorId(Long creatorId);


    // Đếm số vocab trong flashcard (ví dụ nếu cần)
    @Query("SELECT COUNT(v) FROM Vocabulary v WHERE v.flashcard.id = :flashcardId")
    long countVocabulariesByFlashcardId(@Param("flashcardId") Long flashcardId);

    // Tìm flashcards theo category ID VÀ category ID đó phải khác 1
    List<Flashcard> findByCategoryIdAndCategoryIdNot(Long categoryId, Long excludedCategoryId);
}