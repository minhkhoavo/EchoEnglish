package com.echo_english.controller;

import com.echo_english.dto.request.LearningRecordRequest;
import com.echo_english.dto.response.LearningHistoryResponse;
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
            @Valid @RequestBody LearningRecordRequest recordRequest) {
        learningHistoryService.recordLearning(recordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<LearningHistoryResponse>> getLearningHistoryForUser(
            @PathVariable Long userId) {
        List<LearningHistoryResponse> history = learningHistoryService.getLearningHistoryForUser(userId);
        return ResponseEntity.ok(history);
    }


}