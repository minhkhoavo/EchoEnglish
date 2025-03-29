package com.echo_english.service;

import com.echo_english.dto.request.VocabularyCreationRequest;
import com.echo_english.entity.Category;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.Vocabulary;
import com.echo_english.mapper.VocabularyMapper;
import com.echo_english.repository.CategoryRepository;
import com.echo_english.repository.VocabularyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class VocabularyService {
    @Autowired
    private VocabularyRepository vocabularyRepository;

    public List<Vocabulary> getAllVocabularies() {
        return vocabularyRepository.findAll();
    }

    public List<Vocabulary> getVocabulariesByFlashcardId(Long flashcardId) {
        return vocabularyRepository.findByFlashcardId(flashcardId);
    }

    public Vocabulary getVocabularyById(Long id) {
        return vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found"));
    }


//    // Create new vocabulary
//    public Vocabulary createVocabulary(Vocabulary vocabulary) {
//        return vocabularyRepository.save(vocabulary);
//    }
//
//    public Vocabulary createVocabulary(VocabularyCreationRequest request) {
//        Vocabulary vocabulary = vocabularyMapper.toVocabulary(request);
//        Vocabulary createdVocabulary = vocabularyRepository.save(vocabulary);
//
//        if (request.getCategory_id() != null) {
//            Long categoryId = Long.parseLong(request.getCategory_id());
//            Category category = categoryRepository.findById(categoryId)
//                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
//            vocabularyCategoryService.createVocabulary(
//                    VocabularyCategory.builder()
//                            .vocabulary(createdVocabulary)
//                            .category(category)
//                            .build());
//            }
//
//        return createdVocabulary;
//    }
//
//    // Get vocabulary by ID
//    public Vocabulary getVocabularyById(Long id) {
//        return vocabularyRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Vocabulary not found with id: " + id));
//    }
//
//    // Get all vocabularies
//    public List<Vocabulary> getAllVocabularies() {
//        return vocabularyRepository.findAll();
//    }
//
//    // Update vocabulary
//    public Vocabulary updateVocabulary(Long id, Vocabulary updatedVocabulary) {
//        Optional<Vocabulary> existingVocabulary = vocabularyRepository.findById(id);
//        if (existingVocabulary.isPresent()) {
//            Vocabulary vocabulary = existingVocabulary.get();
//            vocabulary.setWord(updatedVocabulary.getWord());
//            vocabulary.setDefinition(updatedVocabulary.getDefinition());
//            vocabulary.setPronunciation(updatedVocabulary.getPronunciation());
//            vocabulary.setImage(updatedVocabulary.getImage());
//            vocabulary.setExample(updatedVocabulary.getExample());
//            vocabulary.setStatus(updatedVocabulary.getStatus());
//            return vocabularyRepository.save(vocabulary);
//        }
//        throw new RuntimeException("Vocabulary with ID " + id + " not found"); // Ném ngoại lệ RuntimeException
//    }
//
//    // Delete vocabulary by ID
//    public void deleteVocabulary(Long id) {
//        if (!vocabularyRepository.existsById(id)) {
//            throw new RuntimeException("Vocabulary not found with id: " + id);
//        }
//        vocabularyRepository.deleteById(id);
//    }
}
