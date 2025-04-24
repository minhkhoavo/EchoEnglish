package com.echo_english.controller;

import com.echo_english.dto.request.FlashcardCreateRequest;
import com.echo_english.dto.request.FlashcardUpdateRequest;
import com.echo_english.dto.request.VocabularyCreateRequest;
import com.echo_english.dto.request.VocabularyUpdateRequest;
import com.echo_english.dto.response.FlashcardBasicResponse;
import com.echo_english.dto.response.FlashcardDetailResponse;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    // Đổi tên endpoint cho rõ ràng hơn
    @PostMapping("/user-defined")
    // public ResponseEntity<FlashcardDetailResponse> createUserDefinedFlashcard(
    //         @Valid @RequestBody FlashcardCreateRequest createRequest,
    //         @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<FlashcardDetailResponse> createUserDefinedFlashcard(
            @Valid @RequestBody FlashcardCreateRequest createRequest) {
        // if (currentUser == null) { ... } // Bỏ kiểm tra null
        // FlashcardDetailResponse createdFlashcard = flashcardService.createUserDefinedFlashcard(createRequest, currentUser.getId()); // Bỏ currentUser.getId()
        FlashcardDetailResponse createdFlashcard = flashcardService.createUserDefinedFlashcard(createRequest);
        return new ResponseEntity<>(createdFlashcard, HttpStatus.CREATED);
    }

    @PostMapping("/{flashcardId}/vocabularies")
    // public ResponseEntity<VocabularyResponse> addVocabulary(
    //         @PathVariable Long flashcardId,
    //         @Valid @RequestBody VocabularyCreateRequest createRequest,
    //         @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<VocabularyResponse> addVocabulary(
            @PathVariable Long flashcardId,
            @Valid @RequestBody VocabularyCreateRequest createRequest) {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // VocabularyResponse addedVocabulary = flashcardService.addVocabularyToFlashcard(flashcardId, createRequest, currentUser.getId()); // Bỏ currentUser.getId()
        VocabularyResponse addedVocabulary = flashcardService.addVocabularyToFlashcard(flashcardId, createRequest);
        return new ResponseEntity<>(addedVocabulary, HttpStatus.CREATED);
    }



    // Đổi tên endpoint
    @GetMapping("/user-defined")
    // public ResponseEntity<List<FlashcardBasicResponse>> getMyUserDefinedFlashcards(
    //          @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<List<FlashcardBasicResponse>> getAllUserDefinedFlashcards() {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // List<FlashcardBasicResponse> flashcards = flashcardService.getMyUserDefinedFlashcards(currentUser.getId()); // Bỏ currentUser.getId()
        List<FlashcardBasicResponse> flashcards = flashcardService.getAllUserDefinedFlashcards();
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/public")
    public ResponseEntity<List<FlashcardBasicResponse>> getPublicFlashcards() {
        List<FlashcardBasicResponse> flashcards = flashcardService.getPublicFlashcards();
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/{flashcardId}")
    // public ResponseEntity<FlashcardDetailResponse> getFlashcardDetails(
    //         @PathVariable Long flashcardId,
    //         @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<FlashcardDetailResponse> getFlashcardDetails(
            @PathVariable Long flashcardId) {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // FlashcardDetailResponse flashcard = flashcardService.getFlashcardDetails(flashcardId, currentUser.getId()); // Bỏ currentUser.getId()
        FlashcardDetailResponse flashcard = flashcardService.getFlashcardDetails(flashcardId);
        return ResponseEntity.ok(flashcard);
    }

    @GetMapping("/{flashcardId}/vocabularies")
    // public ResponseEntity<List<VocabularyResponse>> getVocabulariesByFlashcard(
    //         @PathVariable Long flashcardId,
    //         @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<List<VocabularyResponse>> getVocabulariesByFlashcard(
            @PathVariable Long flashcardId) {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // List<VocabularyResponse> vocabularies = flashcardService.getVocabulariesByFlashcard(flashcardId, currentUser.getId()); // Bỏ currentUser.getId()
        List<VocabularyResponse> vocabularies = flashcardService.getVocabulariesByFlashcard(flashcardId);
        return ResponseEntity.ok(vocabularies);
    }

    @DeleteMapping("/{flashcardId}")
    // public ResponseEntity<Void> deleteFlashcard(
    //         @PathVariable Long flashcardId,
    //         @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<Void> deleteFlashcard(
            @PathVariable Long flashcardId) {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // flashcardService.deleteFlashcard(flashcardId, currentUser.getId()); // Bỏ currentUser.getId()
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/vocabularies/{vocabularyId}")
    // public ResponseEntity<Void> deleteVocabulary(
    //          @PathVariable Long vocabularyId,
    //          @AuthenticationPrincipal UserPrincipal currentUser) { // Bỏ AuthenticationPrincipal
    public ResponseEntity<Void> deleteVocabulary(
            @PathVariable Long vocabularyId) {
        // if (currentUser == null) ... // Bỏ kiểm tra null
        // flashcardService.deleteVocabulary(vocabularyId, currentUser.getId()); // Bỏ currentUser.getId()
        flashcardService.deleteVocabulary(vocabularyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}") // Lấy danh sách flashcard thuộc một category công khai cụ thể. :)????
    public ResponseEntity<List<FlashcardBasicResponse>> getPublicFlashcardsByCategory(@PathVariable Long categoryId) {
        List<FlashcardBasicResponse> flashcards = flashcardService.getPublicFlashcardsByCategory(categoryId);
        return ResponseEntity.ok(flashcards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardDetailResponse> updateFlashcard(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardUpdateRequest updateRequest) {
        FlashcardDetailResponse updatedFlashcard = flashcardService.updateFlashcard(id, updateRequest);
        return ResponseEntity.ok(updatedFlashcard);
    }

     @GetMapping("/creator/{creatorId}")
        public ResponseEntity<List<FlashcardBasicResponse>> getFlashcardsByCreator(@PathVariable Long creatorId) {
            // Hiện tại chỉ cho phép lấy của creatorId = 1
            // Trong tương lai với Security, có thể kiểm tra quyền ở đây hoặc trong Service
            if (!Long.valueOf(27L).equals(creatorId)) { // So sánh Long đúng cách
                 // return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
                 return ResponseEntity.ok(Collections.emptyList()); // Trả về rỗng thay vì lỗi
            }
            List<FlashcardBasicResponse> flashcards = flashcardService.getFlashcardsByCreator(creatorId);
            return ResponseEntity.ok(flashcards);
        }



}