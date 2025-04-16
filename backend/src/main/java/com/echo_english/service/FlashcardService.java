package com.echo_english.service;

//import com.echo_english.dto.request.FlashcardCreateRequest;
//import com.echo_english.dto.request.VocabularyCreateRequest;
//import com.echo_english.dto.response.FlashcardBasicResponse;
//import com.echo_english.dto.response.FlashcardDetailResponse;
//import com.echo_english.dto.response.VocabularyResponse;
import com.echo_english.entity.Category;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.User;
import com.echo_english.entity.Vocabulary;
import com.echo_english.exception.ForbiddenAccessException;
import com.echo_english.exception.ResourceNotFoundException;
import com.echo_english.repository.CategoryRepository;
import com.echo_english.repository.FlashcardRepository;
import com.echo_english.repository.UserRepository;
import com.echo_english.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

//    private final FlashcardRepository flashcardRepository;
//    private final CategoryRepository categoryRepository;
//    private final VocabularyRepository vocabularyRepository;
//    private final UserRepository userRepository;
//
//    private static final Long USER_DEFINED_CATEGORY_ID = 1L;
//
//    // --- Phương thức tạo Flashcard ---
//    @Transactional
//    public FlashcardDetailResponse createUserDefinedFlashcard(FlashcardCreateRequest createRequest, Long creatorUserId) {
//        // Lấy thông tin User tạo
//        User creator = userRepository.findById(creatorUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId)); // Hoặc .orElseThrow()
//
//        // Lấy category mặc định
//        Category defaultCategory = categoryRepository.findById(USER_DEFINED_CATEGORY_ID)
//                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", USER_DEFINED_CATEGORY_ID)); // Hoặc .orElseThrow()
//
//
//        Flashcard flashcard = Flashcard.builder()
//                .name(createRequest.getName())
//                .imageUrl(createRequest.getImageUrl())
//                .category(defaultCategory) // Category ID 1
//                .creator(creator)        // Gán người tạo
//                .build();
//
//        Flashcard savedFlashcard = flashcardRepository.save(flashcard);
//        return mapToFlashcardDetailResponse(savedFlashcard);
//    }
//
//    // --- Phương thức thêm Vocabulary ---
//    @Transactional
//    public VocabularyResponse addVocabularyToFlashcard(Long flashcardId, VocabularyCreateRequest createRequest, Long currentUserId) {
//        Flashcard flashcard = flashcardRepository.findById(flashcardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId)); // Hoặc .orElseThrow()
//
//
//        // *** Kiểm tra quyền thêm vocabulary ***
//        checkFlashcardModificationPermission(flashcard, currentUserId);
//
//        Vocabulary vocabulary = Vocabulary.builder()
//                .word(createRequest.getWord())
//                .definition(createRequest.getDefinition())
//                .phonetic(createRequest.getPhonetic())
//                .example(createRequest.getExample())
//                .type(createRequest.getType())
//                .imageUrl(createRequest.getImageUrl())
//                .flashcard(flashcard)
//                .build();
//
//        // Thêm vào list trong flashcard nếu cần (do có orphanRemoval=true)
//        flashcard.getVocabularies().add(vocabulary);
//        // Lưu vocabulary (cascade sẽ tự động cập nhật flashcard nếu cần)
//        Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
//        // flashcardRepository.save(flashcard); // Không cần thiết nếu cascade đúng
//
//        return mapToVocabularyResponse(savedVocabulary);
//    }
//
//    // --- Phương thức lấy chi tiết Flashcard ---
//    @Transactional(readOnly = true)
//    public FlashcardDetailResponse getFlashcardDetails(Long flashcardId, Long currentUserId) {
//        Flashcard flashcard = flashcardRepository.findById(flashcardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId)); // Hoặc .orElseThrow()
//
//
//        // *** Kiểm tra quyền xem ***
//        checkFlashcardViewPermission(flashcard, currentUserId);
//
//        // Eagerly fetch vocabularies if needed (hoặc dựa vào Lazy Loading + Transaction)
//        // Hibernate.initialize(flashcard.getVocabularies()); // Cách khác để trigger load
//        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId); // Tải tường minh
//        flashcard.setVocabularies(vocabularies);
//
//        return mapToFlashcardDetailResponse(flashcard);
//    }
//
//    // --- Phương thức lấy Flashcards của TÔI (User-Defined) ---
//    @Transactional(readOnly = true)
//    public List<FlashcardBasicResponse> getMyUserDefinedFlashcards(Long currentUserId) {
//        List<Flashcard> flashcards = flashcardRepository.findByCategoryIdAndCreatorId(USER_DEFINED_CATEGORY_ID, currentUserId);
//        return flashcards.stream()
//                .map(this::mapToFlashcardBasicResponse)
//                .collect(Collectors.toList());
//    }
//
//    // --- Phương thức lấy Flashcards Public (khác Category 1) ---
//    @Transactional(readOnly = true)
//    public List<FlashcardBasicResponse> getPublicFlashcards() {
//        List<Flashcard> flashcards = flashcardRepository.findByCategoryIdNot(USER_DEFINED_CATEGORY_ID);
//        return flashcards.stream()
//                .map(this::mapToFlashcardBasicResponse)
//                .collect(Collectors.toList());
//    }
//
//    // --- Phương thức lấy Vocabularies (kiểm tra quyền xem flashcard trước) ---
//    @Transactional(readOnly = true)
//    public List<VocabularyResponse> getVocabulariesByFlashcard(Long flashcardId, Long currentUserId) {
//        Flashcard flashcard = flashcardRepository.findById(flashcardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId)); // Hoặc .orElseThrow()
//
//        // *** Kiểm tra quyền xem flashcard chứa vocabularies ***
//        checkFlashcardViewPermission(flashcard, currentUserId);
//
//        List<Vocabulary> vocabularies = vocabularyRepository.findByFlashcardId(flashcardId);
//        return vocabularies.stream()
//                .map(this::mapToVocabularyResponse)
//                .collect(Collectors.toList());
//    }
//
//    // --- Phương thức xóa Flashcard ---
//    @Transactional
//    public void deleteFlashcard(Long flashcardId, Long currentUserId) {
//        Flashcard flashcard = flashcardRepository.findById(flashcardId)
//                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId)); // Hoặc .orElseThrow()
//
//        // *** Kiểm tra quyền xóa flashcard ***
//        checkFlashcardModificationPermission(flashcard, currentUserId);
//
//        flashcardRepository.delete(flashcard); // Cascade sẽ xóa vocabularies
//    }
//
//    // --- Phương thức xóa Vocabulary ---
//    @Transactional
//    public void deleteVocabulary(Long vocabularyId, Long currentUserId) {
//        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
//                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", vocabularyId)); // Hoặc .orElseThrow()
//
//        // *** Kiểm tra quyền xóa thông qua flashcard cha ***
//        checkFlashcardModificationPermission(vocabulary.getFlashcard(), currentUserId);
//
//        // Xóa trực tiếp hoặc thông qua list của flashcard (nếu có orphanRemoval)
//        // vocabulary.getFlashcard().getVocabularies().remove(vocabulary); // Nếu dùng orphanRemoval
//        // flashcardRepository.save(vocabulary.getFlashcard()); // Cần save lại flashcard
//        // Hoặc đơn giản là xóa vocabulary:
//        vocabularyRepository.delete(vocabulary);
//    }
//
//    // --- Phương thức kiểm tra quyền helper ---
//
//    /**
//     * Kiểm tra quyền XEM một flashcard.
//     * Cho phép xem nếu là public (category != 1) HOẶC là flashcard user-defined (category == 1) và user hiện tại là người tạo.
//     * Ném ForbiddenAccessException nếu không có quyền.
//     */
//    private void checkFlashcardViewPermission(Flashcard flashcard, Long currentUserId) {
//        if (flashcard.getCategory() == null) {
//            throw new IllegalStateException("Flashcard " + flashcard.getId() + " has no category assigned.");
//        }
//        // Nếu là public (category != 1), ai cũng xem được
//        if (!USER_DEFINED_CATEGORY_ID.equals(flashcard.getCategory().getId())) {
//            return; // OK
//        }
//        // Nếu là user-defined (category == 1), chỉ người tạo được xem
//        if (flashcard.getCreator() == null || !flashcard.getCreator().getId().equals(currentUserId)) {
//            throw new ForbiddenAccessException("You do not have permission to view this user-defined flashcard.");
//        }
//        // OK, là người tạo
//    }
//
//    /**
//     * Kiểm tra quyền CHỈNH SỬA/XÓA một flashcard.
//     * Hiện tại: Chỉ cho phép nếu là flashcard user-defined (category == 1) và user hiện tại là người tạo.
//     * (Có thể mở rộng cho admin sửa/xóa public flashcards sau)
//     * Ném ForbiddenAccessException nếu không có quyền.
//     */
//    private void checkFlashcardModificationPermission(Flashcard flashcard, Long currentUserId) {
//        if (flashcard.getCategory() == null) {
//            throw new IllegalStateException("Flashcard " + flashcard.getId() + " has no category assigned.");
//        }
//        // Chỉ cho phép sửa/xóa flashcard user-defined bởi chính người tạo
//        if (!USER_DEFINED_CATEGORY_ID.equals(flashcard.getCategory().getId())) {
//            throw new ForbiddenAccessException("Modification of public flashcards is not allowed (or requires admin privileges)."); // Hoặc logic khác cho admin
//        }
//        if (flashcard.getCreator() == null || !flashcard.getCreator().getId().equals(currentUserId)) {
//            throw new ForbiddenAccessException("You do not have permission to modify this user-defined flashcard.");
//        }
//        // OK, là người tạo flashcard user-defined
//    }
//
//
//    // --- Helper Mapping Methods (Cập nhật để thêm creator info) ---
//
//    private FlashcardDetailResponse mapToFlashcardDetailResponse(Flashcard flashcard) {
//        return FlashcardDetailResponse.builder()
//                .id(flashcard.getId())
//                .name(flashcard.getName())
//                .imageUrl(flashcard.getImageUrl())
//                .categoryId(flashcard.getCategory() != null ? flashcard.getCategory().getId() : null)
//                .categoryName(flashcard.getCategory() != null ? flashcard.getCategory().getName() : null)
//                .creatorId(flashcard.getCreator() != null ? flashcard.getCreator().getId() : null) // Thêm creatorId
//                .creatorName(flashcard.getCreator() != null ? flashcard.getCreator().getName() : null) // Thêm creatorName
//                .vocabularies(flashcard.getVocabularies() != null ?
//                        flashcard.getVocabularies().stream().map(this::mapToVocabularyResponse).collect(Collectors.toList()) :
//                        List.of())
//                .build();
//    }
//
//    private FlashcardBasicResponse mapToFlashcardBasicResponse(Flashcard flashcard) {
//        return FlashcardBasicResponse.builder()
//                .id(flashcard.getId())
//                .name(flashcard.getName())
//                .imageUrl(flashcard.getImageUrl())
//                .categoryId(flashcard.getCategory() != null ? flashcard.getCategory().getId() : null)
//                .categoryName(flashcard.getCategory() != null ? flashcard.getCategory().getName() : null)
//                .creatorId(flashcard.getCreator() != null ? flashcard.getCreator().getId() : null) // Thêm creatorId
//                .creatorName(flashcard.getCreator() != null ? flashcard.getCreator().getName() : null) // Thêm creatorName
//                .build();
//    }
//
//    private VocabularyResponse mapToVocabularyResponse(Vocabulary vocabulary) {
//        // Không thay đổi
//        return VocabularyResponse.builder()
//                // ... các trường ...
//                .build();
//    }
}