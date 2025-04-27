package com.echo_english.service;

import com.echo_english.dto.request.LearningRecordRequest;
// import com.echo_english.dto.response.LearningHistoryResponse; // We won't return this list directly anymore
import com.echo_english.dto.response.LearningProgressResponse;
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

    private static final Duration INTERVAL_0 = Duration.ofMinutes(1);
    private static final Duration INTERVAL_1 = Duration.ofMinutes(5);
    private static final Duration INTERVAL_2 = Duration.ofMinutes(30);
    private static final Duration INTERVAL_3 = Duration.ofHours(2);
    private static final Duration INTERVAL_4 = Duration.ofDays(1);
    private static final Duration INTERVAL_5_PLUS = Duration.ofDays(3);

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


    @Transactional(readOnly = true)
    // Change return type to List of the new VocabularyReviewResponse
    public List<VocabularyReviewResponse> getDueVocabulariesForReview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<FlashcardLearningHistory> userHistories = historyRepository.findByUserId(userId);

        LocalDateTime now = LocalDateTime.now();

        List<FlashcardLearningHistory> dueHistories = userHistories.stream()
                .filter(history -> {
                    Duration interval;
                    switch (history.getRememberCount()) {
                        case 0:
                            interval = INTERVAL_0;
                            break;
                        case 1:
                            interval = INTERVAL_1;
                            break;
                        case 2:
                            interval = INTERVAL_2;
                            break;
                        case 3:
                            interval = INTERVAL_3;
                            break;
                        case 4:
                            interval = INTERVAL_4;
                            break;
                        default: // rememberCount >= 5
                            interval = INTERVAL_5_PLUS;
                            break;
                    }

                    LocalDateTime nextReviewTime = history.getLearnedAt().plus(interval);

                    return !now.isBefore(nextReviewTime);
                })
                .collect(Collectors.toList());

        // Map the due history entries to the new VocabularyReviewResponse DTO
        return dueHistories.stream()
                .map(this::mapToVocabularyReviewResponse)
                .collect(Collectors.toList());
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

    // ... (getLearningProgress method remains the same) ...
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
}