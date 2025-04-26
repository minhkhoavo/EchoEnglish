package com.echo_english.controller;

import com.echo_english.dto.request.StartTestRequest;
import com.echo_english.dto.request.SubmitAnswerRequest;
import com.echo_english.dto.response.StartTestResponse;
import com.echo_english.entity.TestHistory;
import com.echo_english.service.TestHistoryService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/test-history") // Base path for all history related endpoints
public class TestHistoryController {

    private static final Logger log = LoggerFactory.getLogger(TestHistoryController.class);

    @Autowired
    private TestHistoryService testHistoryService;

    @PostMapping("/start")
    public ResponseEntity<StartTestResponse> startTest(@RequestBody StartTestRequest request) {
        StartTestResponse response = testHistoryService.startTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/detail/submit")
    public ResponseEntity<Void> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        testHistoryService.submitAnswer(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{historyId}/complete")
    public ResponseEntity<TestHistory> completeTest(@PathVariable Long historyId) {
        TestHistory completedHistory = testHistoryService.completeTest(historyId);
        return ResponseEntity.ok(completedHistory);
    }

    @GetMapping("/user/{userId}")
    // Change PathVariable and method parameter type from String to Long
    public ResponseEntity<List<TestHistory>> getUserHistory(@PathVariable Long userId) {
        List<TestHistory> historyList = testHistoryService.getUserTestHistory(userId);
        return ResponseEntity.ok(historyList);
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<TestHistory> getHistoryDetails(@PathVariable Long historyId) {
        TestHistory historyDetails = testHistoryService.getTestHistoryDetails(historyId);
        return ResponseEntity.ok(historyDetails);
    }
}