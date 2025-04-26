package com.echo_english.controller;

import com.echo_english.entity.Test;
import com.echo_english.entity.TestPart; // Import TestPart
import com.echo_english.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<List<Test>> getAllTests() {
        return ResponseEntity.status(HttpStatus.OK).body(testService.getAllTests());
    }


    // Existing endpoint to get the full test
    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Integer id) {
        // Consider if you still need the full test data fetched this way
        // or if it should also use more specific fetching if parts become large.
        return ResponseEntity.ok(testService.getTestById(id));
    }

    // New endpoint to get a specific part of a test
    @GetMapping("/{testId}/parts/{partId}")
    public ResponseEntity<TestPart> getTestPartById(
            @PathVariable Integer testId,
            @PathVariable Integer partId) {
        TestPart testPart = testService.getTestPartById(testId, partId);
        return ResponseEntity.ok(testPart);
    }

    /* // Optional: Add specific exception handling for better HTTP status codes
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        // You could return a more structured error response object instead of just a String
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    */
}