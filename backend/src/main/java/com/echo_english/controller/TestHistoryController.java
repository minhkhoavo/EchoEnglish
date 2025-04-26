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
        // No change needed here as it uses the updated DTO
        try {
            StartTestResponse response = testHistoryService.startTest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            log.warn("Start test failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error starting test for user {}", request.getUserId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error starting test", e);
        }
    }

    @PostMapping("/detail/submit")
    public ResponseEntity<Void> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        // No change needed here
        try {
            testHistoryService.submitAnswer(request);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.warn("Submit answer failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Submit answer failed due to invalid state or argument: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error submitting answer for history {}", request.getTestHistoryId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error submitting answer", e);
        }
    }

    @PutMapping("/{historyId}/complete")
    public ResponseEntity<TestHistory> completeTest(@PathVariable Long historyId) {
        // No change needed here
        try {
            TestHistory completedHistory = testHistoryService.completeTest(historyId);
            return ResponseEntity.ok(completedHistory);
        } catch (EntityNotFoundException e) {
            log.warn("Complete test failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error completing test for history {}", historyId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error completing test", e);
        }
    }

    @GetMapping("/user/{userId}")
    // Change PathVariable and method parameter type from String to Long
    public ResponseEntity<List<TestHistory>> getUserHistory(@PathVariable Long userId) {
        try {
            // Pass the Long userId to the service method
            List<TestHistory> historyList = testHistoryService.getUserTestHistory(userId);
            return ResponseEntity.ok(historyList);
        } catch (EntityNotFoundException e) {
            log.warn("Get user history failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            // Log using the Long userId
            log.error("Error fetching history for user id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user history", e);
        }
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<TestHistory> getHistoryDetails(@PathVariable Long historyId) {
        // No change needed here
        try {
            TestHistory historyDetails = testHistoryService.getTestHistoryDetails(historyId);
            return ResponseEntity.ok(historyDetails);
        } catch (EntityNotFoundException e) {
            log.warn("Get history details failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching details for history {}", historyId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching history details", e);
        }
    }
}