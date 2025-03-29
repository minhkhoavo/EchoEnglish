package com.echo_english.service;

import com.echo_english.entity.Flashcard;
import com.echo_english.repository.FlashcardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;

    public FlashcardService(FlashcardRepository flashcardRepository) {
        this.flashcardRepository = flashcardRepository;
    }

    // Lấy tất cả flashcards
    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    // Lấy flashcards theo userId
    public List<Flashcard> getFlashcardsByUserId(Long userId) {
        return flashcardRepository.findByUserId(userId);
    }

    // Lấy Flashcard theo ID
    public Flashcard getFlashcardById(Long id) {
        return flashcardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));
    }
}
