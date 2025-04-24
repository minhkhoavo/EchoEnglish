package com.echo_english.controller;

import com.echo_english.dto.request.VocabularyUpdateRequest;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.service.FlashcardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vocabularies")
public class VocabularyController {

    @Autowired
    private FlashcardService flashcardService;

    @PutMapping("/{id}") // Endpoint độc lập với flashcard
    public ResponseEntity<VocabularyResponse> updateVocabulary(
            @PathVariable("id") Long vocabularyId,
            @Valid @RequestBody VocabularyUpdateRequest updateRequest) {
        // Gọi service tương ứng (ví dụ trong FlashcardService)
        VocabularyResponse updatedVocabulary = flashcardService.updateVocabulary(vocabularyId, updateRequest);
        // Hoặc vocabularyService.updateVocabulary(...)
        return ResponseEntity.ok(updatedVocabulary);
    }
}
