package com.echo_english.controller;

import com.echo_english.service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vocabularies")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;


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
