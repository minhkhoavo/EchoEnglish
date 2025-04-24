package com.echo_english.service;

import com.echo_english.dto.request.FlashcardCreateRequest;
import com.echo_english.dto.request.FlashcardUpdateRequest;
import com.echo_english.dto.request.VocabularyCreateRequest;
import com.echo_english.dto.request.VocabularyUpdateRequest;
import com.echo_english.dto.response.FlashcardBasicResponse;
import com.echo_english.dto.response.FlashcardDetailResponse;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.entity.Category;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.User;
import com.echo_english.entity.Vocabulary;
import com.echo_english.exception.ResourceNotFoundException;
import com.echo_english.repository.CategoryRepository;
import com.echo_english.repository.FlashcardRepository;
import com.echo_english.repository.UserRepository;
import com.echo_english.repository.VocabularyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final CategoryRepository categoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    private static final Long USER_DEFINED_CATEGORY_ID = 1L;
    private static final Long DEFAULT_CREATOR_ID = 27L;

    private User defaultCreator;

    @PostConstruct
    private void checkDefaultUserExists() {
        this.defaultCreator = userRepository.findById(DEFAULT_CREATOR_ID)
                .orElseThrow(() -> new IllegalStateException("Default User with ID " + DEFAULT_CREATOR_ID + " not found!"));

        categoryRepository.findById(USER_DEFINED_CATEGORY_ID)
                .orElseThrow(() -> new IllegalStateException("Default Category with ID " + USER_DEFINED_CATEGORY_ID + " not found!"));
    }

    @Transactional
    public FlashcardDetailResponse createUserDefinedFlashcard(FlashcardCreateRequest createRequest) {
        Category defaultCategory = categoryRepository.findById(USER_DEFINED_CATEGORY_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", USER_DEFINED_CATEGORY_ID));

        Flashcard flashcard = Flashcard.builder()
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .category(defaultCategory)
                .creator(this.defaultCreator)
                .build();
        Flashcard savedFlashcard = flashcardRepository.save(flashcard);
        return mapToFlashcardDetailResponse(savedFlashcard);
    }

    @Transactional
    public VocabularyResponse addVocabularyToFlashcard(Long flashcardId, VocabularyCreateRequest createRequest) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));

        Vocabulary vocabulary = Vocabulary.builder()
                .word(createRequest.getWord())
                .definition(createRequest.getDefinition())
                .phonetic(createRequest.getPhonetic())
                .example(createRequest.getExample())
                .type(createRequest.getType())
                .imageUrl(createRequest.getImageUrl())
                .flashcard(flashcard)
                .build();
        flashcard.getVocabularies().add(vocabulary);
        Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(savedVocabulary);
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Long vocabularyId, VocabularyUpdateRequest updateRequest) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", vocabularyId));

        Flashcard currentParentFlashcard = vocabulary.getFlashcard();
        if (currentParentFlashcard == null) {
            throw new IllegalStateException("Vocabulary " + vocabularyId + " has no parent flashcard.");
        }
        if (currentParentFlashcard.getCreator() == null || !DEFAULT_CREATOR_ID.equals(currentParentFlashcard.getCreator().getId()) ||
                currentParentFlashcard.getCategory() == null || !USER_DEFINED_CATEGORY_ID.equals(currentParentFlashcard.getCategory().getId())) {
            throw new IllegalArgumentException("Permission denied to modify this vocabulary.");
        }

        Flashcard newParentFlashcard = flashcardRepository.findById(updateRequest.getFlashcardId())
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", updateRequest.getFlashcardId()));

        if (newParentFlashcard.getCreator() == null || !DEFAULT_CREATOR_ID.equals(newParentFlashcard.getCreator().getId()) ||
                newParentFlashcard.getCategory() == null || !USER_DEFINED_CATEGORY_ID.equals(newParentFlashcard.getCategory().getId())) {
            throw new IllegalArgumentException("Invalid target flashcard. Cannot move vocabulary to a non-user-defined or other user's flashcard.");
        }

        vocabulary.setWord(updateRequest.getWord());
        vocabulary.setDefinition(updateRequest.getDefinition());
        vocabulary.setPhonetic(updateRequest.getPhonetic());
        vocabulary.setType(updateRequest.getType());
        vocabulary.setExample(updateRequest.getExample());
        vocabulary.setImageUrl(updateRequest.getImageUrl());

        vocabulary.setFlashcard(newParentFlashcard);

        Vocabulary updatedVocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(updatedVocabulary);
    }

    @Transactional(readOnly = true)
    public FlashcardDetailResponse getFlashcardDetails(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId);
        flashcard.setVocabularies(vocabularies);
        return mapToFlashcardDetailResponse(flashcard);
    }

    @Transactional(readOnly = true)
    public List<FlashcardBasicResponse> getAllUserDefinedFlashcards() {
        List<Flashcard> flashcards = flashcardRepository.findByCategoryId(USER_DEFINED_CATEGORY_ID);
        return flashcards.stream()
                .map(this::mapToFlashcardBasicResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FlashcardBasicResponse> getPublicFlashcards() {
        List<Flashcard> flashcards = flashcardRepository.findByCategoryIdNot(USER_DEFINED_CATEGORY_ID);
        return flashcards.stream()
                .map(this::mapToFlashcardBasicResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyResponse> getVocabulariesByFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId);
        return vocabularies.stream()
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        flashcardRepository.delete(flashcard);
    }

    @Transactional
    public void deleteVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", vocabularyId));
        vocabularyRepository.delete(vocabulary);
    }

    private FlashcardDetailResponse mapToFlashcardDetailResponse(Flashcard flashcard) {
        return FlashcardDetailResponse.builder()
                .id(flashcard.getId()).name(flashcard.getName()).imageUrl(flashcard.getImageUrl())
                .categoryId(flashcard.getCategory() != null ? flashcard.getCategory().getId() : null)
                .categoryName(flashcard.getCategory() != null ? flashcard.getCategory().getName() : null)
                .creatorId(flashcard.getCreator() != null ? flashcard.getCreator().getId() : null)
                .creatorName(flashcard.getCreator() != null ? flashcard.getCreator().getName() : null)
                .vocabularies(flashcard.getVocabularies() != null ?
                        flashcard.getVocabularies().stream().map(this::mapToVocabularyResponse).collect(Collectors.toList()) : List.of())
                .build();
    }

    private FlashcardBasicResponse mapToFlashcardBasicResponse(Flashcard flashcard) {
        return FlashcardBasicResponse.builder()
                .id(flashcard.getId()).name(flashcard.getName()).imageUrl(flashcard.getImageUrl())
                .categoryId(flashcard.getCategory() != null ? flashcard.getCategory().getId() : null)
                .categoryName(flashcard.getCategory() != null ? flashcard.getCategory().getName() : null)
                .creatorId(flashcard.getCreator() != null ? flashcard.getCreator().getId() : null)
                .creatorName(flashcard.getCreator() != null ? flashcard.getCreator().getName() : null)
                .build();
    }

    private VocabularyResponse mapToVocabularyResponse(Vocabulary vocabulary) {
        return VocabularyResponse.builder()
                .id(vocabulary.getId()).word(vocabulary.getWord()).definition(vocabulary.getDefinition())
                .phonetic(vocabulary.getPhonetic()).example(vocabulary.getExample()).type(vocabulary.getType())
                .imageUrl(vocabulary.getImageUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FlashcardBasicResponse> getPublicFlashcardsByCategory(Long categoryId) {
        if (USER_DEFINED_CATEGORY_ID.equals(categoryId)) {
            return List.of();
        }

        List<Flashcard> flashcards = flashcardRepository.findByCategoryIdAndCategoryIdNot(categoryId, USER_DEFINED_CATEGORY_ID);

        return flashcards.stream()
                .map(this::mapToFlashcardBasicResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FlashcardDetailResponse updateFlashcard(Long flashcardId, FlashcardUpdateRequest updateRequest) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));

        if (flashcard.getCreator() == null || !DEFAULT_CREATOR_ID.equals(flashcard.getCreator().getId()) ||
                flashcard.getCategory() == null || !USER_DEFINED_CATEGORY_ID.equals(flashcard.getCategory().getId())) {
            throw new IllegalArgumentException("Cannot modify this flashcard. It's either not user-defined or not created by the default user.");
        }

        flashcard.setName(updateRequest.getName());
        flashcard.setImageUrl(updateRequest.getImageUrl());

        Flashcard updatedFlashcard = flashcardRepository.save(flashcard);
        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(updatedFlashcard.getId());
        updatedFlashcard.setVocabularies(vocabularies);

        return mapToFlashcardDetailResponse(updatedFlashcard);
    }

    @Transactional(readOnly = true)
    public List<FlashcardBasicResponse> getFlashcardsByCreator(Long creatorId) {
        if (!DEFAULT_CREATOR_ID.equals(creatorId)) {
            return Collections.emptyList();
        }

        List<Flashcard> flashcards = flashcardRepository.findByCreatorId(creatorId);
        return flashcards.stream()
                .map(this::mapToFlashcardBasicResponse)
                .collect(Collectors.toList());
    }

}
