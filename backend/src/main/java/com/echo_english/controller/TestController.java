package com.echo_english.controller;

import com.echo_english.dto.response.TestResponse;
import com.echo_english.entity.Test;
import com.echo_english.entity.TestPart; // Import TestPart
import com.echo_english.service.TestService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
// Consider adding exception handling for EntityNotFoundException
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.server.ResponseStatusException;
// import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping
    public ResponseEntity<List<TestResponse>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTestSummaries());
    }

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    // Existing endpoint to get the full test
    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Integer id) {
        // Consider if you still need the full test data fetched this way
        // or if it should also use more specific fetching if parts become large.
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @GetMapping("/{testId}/part-number/{partNumber}")
    public ResponseEntity<TestPart> getDetailedTestPartByNumber(
            @PathVariable Integer testId,
            @PathVariable Integer partNumber) {
        try {
            log.info("Received request for detailed part number {} of test {}", partNumber, testId);
            // Gọi phương thức service mới
            TestPart testPart = testService.getTestPartByNumber(testId, partNumber);
            return ResponseEntity.ok(testPart);
        } catch (EntityNotFoundException e) {
            log.warn("Get detailed test part by number failed: testId={}, partNumber={}. Error: {}", testId, partNumber, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error getting detailed test part by number: testId={}, partNumber={}", testId, partNumber, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving detailed test part", e);
        }
    }
}