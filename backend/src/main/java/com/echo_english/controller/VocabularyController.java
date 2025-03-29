package com.echo_english.controller;

import ch.qos.logback.classic.Logger;
import com.echo_english.dto.request.VocabularyCreationRequest;
import com.echo_english.dto.response.ApiResponse;
import com.echo_english.entity.Category;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.Vocabulary;
import com.echo_english.mapper.VocabularyMapper;
import com.echo_english.repository.CategoryRepository;
import com.echo_english.service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/vocabularies")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;

    // Lấy tất cả từ vựng
    @GetMapping
    public ResponseEntity<List<Vocabulary>> getAllVocabularies() {
        List<Vocabulary> vocabularies = vocabularyService.getAllVocabularies();
        return ResponseEntity.status(HttpStatus.OK).body(vocabularies);
    }

    // Lấy từ vựng theo Flashcard ID
    @GetMapping("/flashcards/{flashcardId}")
    public ResponseEntity<List<Vocabulary>> getVocabulariesByFlashcardId(@PathVariable Long flashcardId) {
        List<Vocabulary> vocabularies = vocabularyService.getVocabulariesByFlashcardId(flashcardId);
        return ResponseEntity.status(HttpStatus.OK).body(vocabularies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vocabulary> getFlashcardById(@PathVariable Long id) {
        Vocabulary vocabulary = vocabularyService.getVocabularyById(id);
        return ResponseEntity.status(HttpStatus.OK).body(vocabulary);
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
