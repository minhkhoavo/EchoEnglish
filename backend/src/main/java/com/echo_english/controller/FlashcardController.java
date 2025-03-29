package com.echo_english.controller;

import com.echo_english.dto.response.ApiResponse;
import com.echo_english.entity.Flashcard;
import com.echo_english.service.FlashcardService;
import com.echo_english.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashcards")
public class FlashcardController {
    @Autowired
    private FlashcardService flashcardService;

    // API lấy tất cả flashcards
    @GetMapping
    public ResponseEntity<List<Flashcard>> getAllFlashcards() {
        List<Flashcard> flashcards = flashcardService.getAllFlashcards();
        return ResponseEntity.status(HttpStatus.OK).body(flashcards);
    }

    // API lấy flashcards theo userId
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Flashcard>> getFlashcardsByUserId(@PathVariable Long userId) {
        List<Flashcard> flashcards = flashcardService.getFlashcardsByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(flashcards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getFlashcardById(@PathVariable Long id) {
        Flashcard flashcard = flashcardService.getFlashcardById(id);
        return ResponseEntity.status(HttpStatus.OK).body(flashcard);
    }
}
