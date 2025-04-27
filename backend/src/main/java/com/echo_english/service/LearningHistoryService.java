package com.echo_english.service;

import com.echo_english.dto.request.LearningRecordRequest;
// import com.echo_english.dto.response.LearningHistoryResponse; // We won't return this list directly anymore
import com.echo_english.dto.response.DueReviewCountResponse;
import com.echo_english.dto.response.LearningProgressResponse;
import com.echo_english.dto.response.MemoryLevelsResponse;
import com.echo_english.dto.response.VocabularyReviewResponse; // Import the new DTO
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.FlashcardLearningHistory;
import com.echo_english.entity.User;
import com.echo_english.entity.Vocabulary;
import com.echo_english.exception.ResourceNotFoundException;
import com.echo_english.repository.FlashcardLearningHistoryRepository;
import com.echo_english.repository.FlashcardRepository;
import com.echo_english.repository.UserRepository;
import com.echo_english.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(LearningHistoryService.class);

    private final FlashcardLearningHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final FlashcardRepository flashcardRepository;

    private static final Duration INTERVAL_0 = Duration.ofMinutes(0);
    private static final Duration INTERVAL_1 = Duration.ofMinutes(1);
    private static final Duration INTERVAL_2 = Duration.ofMinutes(30);
    private static final Duration INTERVAL_3 = Duration.ofHours(2);
    private static final Duration INTERVAL_4 = Duration.ofDays(1);
    private static final Duration INTERVAL_5_PLUS = Duration.ofDays(3);

    // Helper method to get interval (used in sorting and filtering)
    private Duration getIntervalForRememberCount(int rememberCount) {
        switch (rememberCount) {
            case 0: return INTERVAL_0;
            case 1: return INTERVAL_1;
            case 2: return INTERVAL_2;
            case 3: return INTERVAL_3;
            case 4: return INTERVAL_4;
            default: return INTERVAL_5_PLUS;
        }
    }

    // ... trong man hinh ds cac flashcard ...
    @Transactional(readOnly = true)
    public LearningProgressResponse getLearningProgress(Long userId, Long flashcardId) {
        // ... (kiểm tra user và flashcard tồn tại) ...

        long totalVocabulariesLong = flashcardRepository.countVocabulariesByFlashcardId(flashcardId);
        int totalVocabularies = (int) totalVocabulariesLong;

        List<FlashcardLearningHistory> userHistory = historyRepository.findByUserId(userId);
        Set<Long> learnedVocabularyIdsInFlashcard = userHistory.stream()
                .filter(h -> h.getVocabulary() != null
                        && h.getVocabulary().getFlashcard() != null
                        && flashcardId.equals(h.getVocabulary().getFlashcard().getId())
                        && h.getRememberCount() >= 1) // ** THÊM ĐIỀU KIỆN NÀY **
                .map(h -> h.getVocabulary().getId())
                .collect(Collectors.toSet());
        int learnedVocabularies = learnedVocabularyIdsInFlashcard.size();


        double completionPercentage = (totalVocabularies > 0)
                ? Math.round(((double) learnedVocabularies / totalVocabularies) * 1000.0) / 10.0
                : 0.0;

        return LearningProgressResponse.builder()
                .flashcardId(flashcardId)
                .userId(userId)
                .totalVocabularies(totalVocabularies)
                .learnedVocabularies(learnedVocabularies)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Transactional
    public void recordLearning(LearningRecordRequest recordRequest) {
        User user = userRepository.findById(recordRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recordRequest.getUserId()));

        Vocabulary vocabulary = vocabularyRepository.findById(recordRequest.getVocabularyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", recordRequest.getVocabularyId()));

        Optional<FlashcardLearningHistory> existingHistoryOpt = historyRepository.findByUserIdAndVocabularyId(user.getId(), vocabulary.getId());

        FlashcardLearningHistory historyRecord;
        if (existingHistoryOpt.isPresent()) {
            historyRecord = existingHistoryOpt.get();

            // ** ĐIỀU CHỈNH Ở ĐÂY KHI DÙNG Boolean **
            // Kiểm tra xem recordRequest.getIsRemembered() có phải là TRUE không
            if (Boolean.TRUE.equals(recordRequest.getIsRemembered())) {
                // Logic khi người dùng NHỚ (đã nhận được true từ DTO)
                int currentCount = historyRecord.getRememberCount();
                historyRecord.setRememberCount(currentCount + 1);
                logger.debug("User {} remembered vocabulary {}. Count incremented from {} to {}", user.getId(), vocabulary.getId(), currentCount, historyRecord.getRememberCount());
            } else {
                // Logic khi người dùng QUÊN (nhận được false) HOẶC trường bị thiếu/null (xử lý như quên)
                // Nếu trường isRemembered trong JSON bị thiếu và DTO là Boolean, nó sẽ là null.
                // Nếu trường isRemembered là false, nó sẽ là false.
                // Cả hai trường hợp này đều được coi là "quên" trong logic hiện tại của bạn.
                historyRecord.setRememberCount(0); // Luôn đặt lại về 0 khi quên hoặc không rõ trạng thái
                logger.debug("User {} forgot/unknown vocabulary {}. Count reset to 0", user.getId(), vocabulary.getId());
            }
            historyRecord.setLearnedAt(LocalDateTime.now()); // Luôn cập nhật thời gian
            logger.debug("Updating learning history record with ID: {}", historyRecord.getId());

        } else {
            // ** ĐIỀU CHỈNH Ở ĐÂY KHI DÙNG Boolean CHO LẦN TẠO MỚI **
            // Kiểm tra giá trị của isRemembered từ DTO để quyết định count ban đầu
            int initialCount = (Boolean.TRUE.equals(recordRequest.getIsRemembered())) ? 1 : 0;
            // Nếu recordRequest.getIsRemembered() là TRUE -> initialCount = 1
            // Nếu recordRequest.getIsRemembered() là FALSE hoặc NULL -> initialCount = 0

            historyRecord = FlashcardLearningHistory.builder()
                    .user(user)
                    .vocabulary(vocabulary)
                    .rememberCount(initialCount)
                    .learnedAt(LocalDateTime.now())
                    .build();
            logger.debug("Creating new learning history for user {} and vocabulary {}. Initial count: {}", user.getId(), vocabulary.getId(), initialCount);
        }

        historyRepository.save(historyRecord);
        logger.debug("Learning history record saved successfully.");
    }

    // Removed getLearningHistoryForUser as it's not directly needed for the review flow
    // If you still need it for other purposes, keep the previous version.
    // For this review feature, we only care about the history records themselves to determine due words.


    // getDueVocabulariesForReview (trả về list các từ đến hạn - hoạt động toàn cầu)
    @Transactional(readOnly = true)
    public List<VocabularyReviewResponse> getDueVocabulariesForReview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Lấy tất cả lịch sử học của người dùng, JOIN FETCH để lấy Vocabulary và Flashcard
        // Sử dụng phương thức findByUserIdWithVocabularyAndFlashcard đã sửa trong repo
        List<FlashcardLearningHistory> userHistories = historyRepository.findByUserIdWithVocabularyAndFlashcard(userId);

        LocalDateTime now = LocalDateTime.now();

        List<VocabularyReviewResponse> dueVocabularies = userHistories.stream()
                .filter(history -> {
                    if (history.getVocabulary() == null || history.getLearnedAt() == null) {
                        return false;
                    }
                    Duration interval = getIntervalForRememberCount(history.getRememberCount());
                    LocalDateTime nextReviewTime = history.getLearnedAt().plus(interval);
                    return !now.isBefore(nextReviewTime);
                })
                // Sắp xếp các từ đến hạn
                .sorted((h1, h2) -> {
                    int rememberCompare = Integer.compare(h1.getRememberCount(), h2.getRememberCount());
                    if (rememberCompare != 0) {
                        return rememberCompare;
                    }
                    return h1.getLearnedAt().compareTo(h2.getLearnedAt()); // Càng cũ càng ưu tiên
                })
                .map(this::mapToVocabularyReviewResponse)
                .collect(Collectors.toList());

        return dueVocabularies;
    }


    // New mapping method for VocabularyReviewResponse
    private VocabularyReviewResponse mapToVocabularyReviewResponse(FlashcardLearningHistory history) {
        Vocabulary vocabulary = history.getVocabulary(); // Get the associated Vocabulary entity

        if (vocabulary == null) {
            // Handle case where vocabulary might be null (shouldn't happen with proper FKs, but defensive)
            return null; // Or throw an exception
        }

        return VocabularyReviewResponse.builder()
                .id(vocabulary.getId())
                .word(vocabulary.getWord())
                .definition(vocabulary.getDefinition())
                .phonetic(vocabulary.getPhonetic())
                .example(vocabulary.getExample())
                .type(vocabulary.getType())
                .imageUrl(vocabulary.getImageUrl())
                .learningHistoryId(history.getId()) // Include the history ID
                .rememberCount(history.getRememberCount())
                .learnedAt(history.getLearnedAt())
                .build();
    }



    // Phương thức lấy số lượng từ vựng ở từng cấp độ ghi nhớ (TOÀN BỘ từ vựng của user)
    @Transactional(readOnly = true)
    public MemoryLevelsResponse getMemoryLevels(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        long level0Count = historyRepository.countByUserIdAndRememberCount(userId, 0);
        long level1Count = historyRepository.countByUserIdAndRememberCount(userId, 1);
        long level2Count = historyRepository.countByUserIdAndRememberCount(userId, 2);
        long level3Count = historyRepository.countByUserIdAndRememberCount(userId, 3);
        long level4Count = historyRepository.countByUserIdAndRememberCount(userId, 4);
        long masteredCount = historyRepository.countByUserIdAndRememberCountGreaterThanEqual(userId, 5);


        return MemoryLevelsResponse.builder()
                .level0(level0Count)
                .level1(level1Count)
                .level2(level2Count)
                .level3(level3Count)
                .level4(level4Count)
                .mastered(masteredCount)
                .build();
    }

    // Phương thức lấy số lượng từ vựng đến hạn ôn tập (đã hoạt động toàn cầu)
    @Transactional(readOnly = true)
    public DueReviewCountResponse getDueReviewCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Lấy tất cả lịch sử học của người dùng (để lọc trong Service)
        List<FlashcardLearningHistory> userHistories = historyRepository.findByUserId(userId);

        LocalDateTime now = LocalDateTime.now();

        int dueCount = (int) userHistories.stream()
                .filter(history -> {
                    if (history.getVocabulary() == null || history.getLearnedAt() == null) {
                        return false;
                    }

                    Duration interval = getIntervalForRememberCount(history.getRememberCount());
                    LocalDateTime nextReviewTime = history.getLearnedAt().plus(interval);

                    return !now.isBefore(nextReviewTime);
                })
                .count();

        return DueReviewCountResponse.builder()
                .count(dueCount)
                .build();
    }
}