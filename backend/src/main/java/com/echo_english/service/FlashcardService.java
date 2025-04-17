package com.echo_english.service;

import com.echo_english.dto.request.FlashcardCreateRequest;
import com.echo_english.dto.request.VocabularyCreateRequest;
import com.echo_english.dto.response.FlashcardBasicResponse;
import com.echo_english.dto.response.FlashcardDetailResponse;
import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.entity.Category;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.User;
import com.echo_english.entity.Vocabulary;
// import com.echo_english.exception.ForbiddenAccessException; // Xóa import
import com.echo_english.exception.ResourceNotFoundException;
import com.echo_english.repository.CategoryRepository;
import com.echo_english.repository.FlashcardRepository;
import com.echo_english.repository.UserRepository;
import com.echo_english.repository.VocabularyRepository;
import jakarta.annotation.PostConstruct; // Dùng để cảnh báo
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private static final Logger logger = LoggerFactory.getLogger(FlashcardService.class);

    private final FlashcardRepository flashcardRepository;
    private final CategoryRepository categoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    private static final Long USER_DEFINED_CATEGORY_ID = 1L;
    private static final Long DEFAULT_CREATOR_ID = 27L; // ID của user mặc định

    private User defaultCreator; // Cache user mặc định

    // Kiểm tra User ID 1 tồn tại khi khởi tạo service
    @PostConstruct
    private void checkDefaultUserExists() {
        this.defaultCreator = userRepository.findById(DEFAULT_CREATOR_ID)
                .orElseThrow(() -> {
                    logger.error("FATAL: Default User with ID {} not found! Please ensure this user exists in the database.", DEFAULT_CREATOR_ID);
                    return new IllegalStateException("Default User with ID " + DEFAULT_CREATOR_ID + " not found!");
                });
        logger.info("Default creator user (ID={}) loaded successfully.", DEFAULT_CREATOR_ID);

        // Kiểm tra Category ID 1
        categoryRepository.findById(USER_DEFINED_CATEGORY_ID)
                .orElseThrow(() -> {
                    logger.error("FATAL: Default Category with ID {} not found! Please ensure this category exists.", USER_DEFINED_CATEGORY_ID);
                    return new IllegalStateException("Default Category with ID " + USER_DEFINED_CATEGORY_ID + " not found!");
                });
        logger.info("Default category (ID={}) verified.", USER_DEFINED_CATEGORY_ID);
    }

    @Transactional
    // public FlashcardDetailResponse createUserDefinedFlashcard(FlashcardCreateRequest createRequest, Long creatorUserId) { // Bỏ creatorUserId
    public FlashcardDetailResponse createUserDefinedFlashcard(FlashcardCreateRequest createRequest) {
        // User creator = userRepository.findById(creatorUserId)... // Bỏ dòng này
        Category defaultCategory = categoryRepository.findById(USER_DEFINED_CATEGORY_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", USER_DEFINED_CATEGORY_ID)); // Nên dùng ResourceNotFound

        Flashcard flashcard = Flashcard.builder()
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .category(defaultCategory)
                .creator(this.defaultCreator) // Gán user mặc định (ID 1)
                .build();
        Flashcard savedFlashcard = flashcardRepository.save(flashcard);
        return mapToFlashcardDetailResponse(savedFlashcard);
    }

    @Transactional
    // public VocabularyResponse addVocabularyToFlashcard(Long flashcardId, VocabularyCreateRequest createRequest, Long currentUserId) { // Bỏ currentUserId
    public VocabularyResponse addVocabularyToFlashcard(Long flashcardId, VocabularyCreateRequest createRequest) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        // checkFlashcardModificationPermission(flashcard, currentUserId); // Bỏ kiểm tra quyền

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

    @Transactional(readOnly = true)
    // public FlashcardDetailResponse getFlashcardDetails(Long flashcardId, Long currentUserId) { // Bỏ currentUserId
    public FlashcardDetailResponse getFlashcardDetails(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        // checkFlashcardViewPermission(flashcard, currentUserId); // Bỏ kiểm tra quyền
        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId);
        flashcard.setVocabularies(vocabularies);
        return mapToFlashcardDetailResponse(flashcard);
    }

    // Lấy TẤT CẢ flashcards User-Defined (Category 1)
    @Transactional(readOnly = true)
    // public List<FlashcardBasicResponse> getMyUserDefinedFlashcards(Long currentUserId) { // Bỏ currentUserId
    public List<FlashcardBasicResponse> getAllUserDefinedFlashcards() {
        // List<Flashcard> flashcards = flashcardRepository.findByCategoryIdAndCreatorId(USER_DEFINED_CATEGORY_ID, currentUserId); // Bỏ
        List<Flashcard> flashcards = flashcardRepository.findByCategoryId(USER_DEFINED_CATEGORY_ID); // Lấy theo Category thôi
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
    // public List<VocabularyResponse> getVocabulariesByFlashcard(Long flashcardId, Long currentUserId) { // Bỏ currentUserId
    public List<VocabularyResponse> getVocabulariesByFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        // checkFlashcardViewPermission(flashcard, currentUserId); // Bỏ kiểm tra quyền
        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId);
        return vocabularies.stream()
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    // public void deleteFlashcard(Long flashcardId, Long currentUserId) { // Bỏ currentUserId
    public void deleteFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));
        // checkFlashcardModificationPermission(flashcard, currentUserId); // Bỏ kiểm tra quyền
        flashcardRepository.delete(flashcard);
    }

    @Transactional
    // public void deleteVocabulary(Long vocabularyId, Long currentUserId) { // Bỏ currentUserId
    public void deleteVocabulary(Long vocabularyId) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", vocabularyId));
        // checkFlashcardModificationPermission(vocabulary.getFlashcard(), currentUserId); // Bỏ kiểm tra quyền
        vocabularyRepository.delete(vocabulary);
    }

    // Bỏ các phương thức check permission
    // private void checkFlashcardViewPermission(...) { ... }
    // private void checkFlashcardModificationPermission(...) { ... }

    // Các phương thức Mapping giữ nguyên (vẫn map creatorId=1)
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
}