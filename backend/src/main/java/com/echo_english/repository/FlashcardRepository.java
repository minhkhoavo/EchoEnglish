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

    // --- Truy vấn cho Flashcards "User-Defined" (Category ID = 1) ---

    // Tìm flashcard user-defined cụ thể theo ID và người tạo
    Optional<Flashcard> findByIdAndCategoryIdAndCreatorId(Long id, Long categoryId, Long creatorId);

    // Tìm TẤT CẢ flashcards user-defined của MỘT người dùng
    List<Flashcard> findByCategoryIdAndCreatorId(Long categoryId, Long creatorId);

    // --- Truy vấn cho Flashcards "Public" (Category ID != 1) ---

    // Tìm flashcard public cụ thể theo ID (bất kỳ ai cũng có thể xem)
    Optional<Flashcard> findByIdAndCategoryIdNot(Long id, Long categoryId);

    // Tìm TẤT CẢ flashcards public
    List<Flashcard> findByCategoryIdNot(Long categoryId);

    // --- Truy vấn Chung (Sử dụng trong Service để kiểm tra quyền) ---
    // Optional<Flashcard> findById(Long id); // Đã có sẵn từ JpaRepository

    // Đếm số vocab trong flashcard (ví dụ nếu cần)
    @Query("SELECT COUNT(v) FROM Vocabulary v WHERE v.flashcard.id = :flashcardId")
    long countVocabulariesByFlashcardId(@Param("flashcardId") Long flashcardId);

    // Kiểm tra flashcard có thuộc user không (hữu ích cho check quyền)
    boolean existsByIdAndCreatorId(Long id, Long creatorId);
}