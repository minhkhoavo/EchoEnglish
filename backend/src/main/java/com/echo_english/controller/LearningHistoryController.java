package com.echo_english.controller;

import com.echo_english.dto.request.LearningRecordRequest;
import com.echo_english.dto.response.DueReviewCountResponse;
import com.echo_english.dto.response.LearningProgressResponse;
import com.echo_english.dto.response.MemoryLevelsResponse;
import com.echo_english.dto.response.VocabularyReviewResponse; // Import the new DTO
import com.echo_english.service.LearningHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learnings")
@RequiredArgsConstructor
public class LearningHistoryController {

    private final LearningHistoryService learningHistoryService;

    @PostMapping
    public ResponseEntity<Void> recordLearning(
            @RequestBody @Valid LearningRecordRequest recordRequest) {
        learningHistoryService.recordLearning(recordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/review/user/{userId}")
    // Change return type to List of the new VocabularyReviewResponse
    public ResponseEntity<List<VocabularyReviewResponse>> getDueVocabulariesForReview(
            @PathVariable Long userId) {
        List<VocabularyReviewResponse> dueVocabularies = learningHistoryService.getDueVocabulariesForReview(userId);
        return ResponseEntity.ok(dueVocabularies);
    }

    // Lấy tiến trình lúc chọn flashcard
    @GetMapping("/progress/user/{userId}/flashcard/{flashcardId}")
    public ResponseEntity<LearningProgressResponse> getLearningProgress(
            @PathVariable Long userId,
            @PathVariable Long flashcardId) {
        LearningProgressResponse progress = learningHistoryService.getLearningProgress(userId, flashcardId);
        return ResponseEntity.ok(progress);
    }

    // Lấy số lượng từ vựng theo từng cấp độ ghi nhớ (TOÀN BỘ từ vựng của user) **
    @GetMapping("/memory-levels/user/{userId}")
    public ResponseEntity<MemoryLevelsResponse> getMemoryLevelsForUser(
            @PathVariable Long userId
    ) {
        MemoryLevelsResponse memoryLevels = learningHistoryService.getMemoryLevels(userId);
        return ResponseEntity.ok(memoryLevels);
    }

    //  lấy số lượng từ vựng cần review
    @GetMapping("/review/user/{userId}/count")
    public ResponseEntity<DueReviewCountResponse> getDueReviewCountForUser(
            @PathVariable Long userId
    ) {
        DueReviewCountResponse reviewCount = learningHistoryService.getDueReviewCount(userId);
        return ResponseEntity.ok(reviewCount);
    }
}