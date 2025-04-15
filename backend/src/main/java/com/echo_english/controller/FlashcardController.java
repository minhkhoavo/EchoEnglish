package com.echo_english.controller;

import com.echo_english.config.security.UserPrincipal;
import com.echo_english.dto.request.FlashcardCreateRequest;
import com.echo_english.dto.request.VocabularyCreateRequest;
import com.echo_english.dto.response.FlashcardBasicResponse;
import com.echo_english.dto.response.FlashcardDetailResponse;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    // Tạo flashcard (chỉ user-defined qua endpoint này)
    @PostMapping("/my-defined") // Đổi tên endpoint cho rõ ràng
    public ResponseEntity<FlashcardDetailResponse> createUserDefinedFlashcard(
            @Valid @RequestBody FlashcardCreateRequest createRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) { // Lấy user hiện tại
        // Kiểm tra currentUser có null không (nếu endpoint yêu cầu xác thực)
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Hoặc để Spring Security xử lý
        }
        FlashcardDetailResponse createdFlashcard = flashcardService.createUserDefinedFlashcard(createRequest, currentUser.getId());
        return new ResponseEntity<>(createdFlashcard, HttpStatus.CREATED);
    }

    // Thêm vocabulary vào flashcard (kiểm tra quyền trong service)
    @PostMapping("/{flashcardId}/vocabularies")
    public ResponseEntity<VocabularyResponse> addVocabulary(
            @PathVariable Long flashcardId,
            @Valid @RequestBody VocabularyCreateRequest createRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        VocabularyResponse addedVocabulary = flashcardService.addVocabularyToFlashcard(flashcardId, createRequest, currentUser.getId());
        return new ResponseEntity<>(addedVocabulary, HttpStatus.CREATED);
    }

    // Lấy danh sách flashcards CỦA TÔI (user-defined)
    @GetMapping("/my-defined")
    public ResponseEntity<List<FlashcardBasicResponse>> getMyUserDefinedFlashcards(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<FlashcardBasicResponse> flashcards = flashcardService.getMyUserDefinedFlashcards(currentUser.getId());
        return ResponseEntity.ok(flashcards);
    }

    // Lấy danh sách flashcards PUBLIC
    @GetMapping("/public")
    public ResponseEntity<List<FlashcardBasicResponse>> getPublicFlashcards(
            /*@AuthenticationPrincipal UserPrincipal currentUser*/ // Có thể yêu cầu login hoặc không
    ) {
        // if (currentUser == null && requireLoginForPublic) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<FlashcardBasicResponse> flashcards = flashcardService.getPublicFlashcards();
        return ResponseEntity.ok(flashcards);
    }


    // Lấy chi tiết một flashcard (kiểm tra quyền trong service)
    @GetMapping("/{flashcardId}")
    public ResponseEntity<FlashcardDetailResponse> getFlashcardDetails(
            @PathVariable Long flashcardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Cần login để xem chi tiết
        FlashcardDetailResponse flashcard = flashcardService.getFlashcardDetails(flashcardId, currentUser.getId());
        return ResponseEntity.ok(flashcard);
    }

    // Lấy danh sách từ vựng của một flashcard (kiểm tra quyền trong service)
    @GetMapping("/{flashcardId}/vocabularies")
    public ResponseEntity<List<VocabularyResponse>> getVocabulariesByFlashcard(
            @PathVariable Long flashcardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<VocabularyResponse> vocabularies = flashcardService.getVocabulariesByFlashcard(flashcardId, currentUser.getId());
        return ResponseEntity.ok(vocabularies);
    }

    // Xóa một flashcard (kiểm tra quyền trong service)
    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<Void> deleteFlashcard(
            @PathVariable Long flashcardId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        flashcardService.deleteFlashcard(flashcardId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // Xóa một từ vựng (kiểm tra quyền trong service)
    @DeleteMapping("/vocabularies/{vocabularyId}")
    public ResponseEntity<Void> deleteVocabulary(
            @PathVariable Long vocabularyId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        flashcardService.deleteVocabulary(vocabularyId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}