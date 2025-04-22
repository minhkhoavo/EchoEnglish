package com.echo_english.repository;

import com.echo_english.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    // Tìm TẤT CẢ flashcards thuộc một category cụ thể (dùng cho user-defined category 1)
    List<Flashcard> findByCategoryId(Long categoryId);

    // Tìm TẤT CẢ flashcards KHÔNG thuộc một category cụ thể (dùng cho public)
    List<Flashcard> findByCategoryIdNot(Long categoryId);

    // Tìm flashcard public cụ thể theo ID (bất kỳ ai cũng có thể xem)
    // Optional<Flashcard> findByIdAndCategoryIdNot(Long id, Long categoryId); // Có thể giữ lại nếu cần check cụ thể là public

    // --- Các phương thức này ít liên quan hơn nhưng không hại nếu giữ lại ---
    // Optional<Flashcard> findByIdAndCategoryIdAndCreatorId(Long id, Long categoryId, Long creatorId);
    // List<Flashcard> findByCategoryIdAndCreatorId(Long categoryId, Long creatorId);
    // boolean existsByIdAndCreatorId(Long id, Long creatorId);

    // Optional<Flashcard> findById(Long id); // Có sẵn từ JpaRepository

    // Đếm số vocab trong flashcard (ví dụ nếu cần)
    @Query("SELECT COUNT(v) FROM Vocabulary v WHERE v.flashcard.id = :flashcardId")
    long countVocabulariesByFlashcardId(@Param("flashcardId") Long flashcardId);

    // Tìm flashcards theo category ID VÀ category ID đó phải khác 1
    List<Flashcard> findByCategoryIdAndCategoryIdNot(Long categoryId, Long excludedCategoryId);
}