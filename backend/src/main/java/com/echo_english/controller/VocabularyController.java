package com.echo_english.controller;

import com.echo_english.dto.request.VocabularyUpdateRequest;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.service.FlashcardService;
import com.echo_english.service.VocabularyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vocabularies")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;

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


//    @PostMapping
//    public ResponseEntity<ApiResponse<Vocabulary>> createVocabulary(@RequestBody VocabularyCreationRequest request) {
//        Vocabulary createdVocabulary = vocabularyService.createVocabulary(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdVocabulary));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<Vocabulary>> getVocabularyById(@PathVariable Long id) {
//        Vocabulary vocabulary = vocabularyService.getVocabularyById(id);
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(vocabulary));
//    }
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<Vocabulary>>> getAllVocabularies() {
//        List<Vocabulary> vocabularies = vocabularyService.getAllVocabularies();
//        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(vocabularies));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<Vocabulary>> updateVocabulary(@PathVariable Long id, @RequestBody Vocabulary updatedVocabulary) {
//        try {
//            Vocabulary updated = vocabularyService.updateVocabulary(id, updatedVocabulary);
//            return new ResponseEntity<>(ApiResponse.success(updated), HttpStatus.OK);
//        } catch (RuntimeException ex) {
//            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
//        }
//    }


}
