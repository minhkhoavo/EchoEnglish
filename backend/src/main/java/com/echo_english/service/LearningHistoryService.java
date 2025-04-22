package com.echo_english.service;

import com.echo_english.dto.request.LearningRecordRequest;
import com.echo_english.dto.response.LearningHistoryResponse;
// import com.echo_english.entity.Flashcard; // Không cần Flashcard trực tiếp ở đây nữa
import com.echo_english.dto.response.LearningProgressResponse;
import com.echo_english.entity.Flashcard;
import com.echo_english.entity.FlashcardLearningHistory;
import com.echo_english.entity.User;
import com.echo_english.entity.Vocabulary;
import com.echo_english.exception.ResourceNotFoundException;
import com.echo_english.repository.FlashcardLearningHistoryRepository;
// import com.echo_english.repository.FlashcardRepository; // Không cần nữa
import com.echo_english.repository.FlashcardRepository;
import com.echo_english.repository.UserRepository;
import com.echo_english.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FlashcardRepository flashcardRepository; // Đã loại bỏ

    @Transactional
    public void recordLearning(LearningRecordRequest recordRequest) {
        // Lấy User
        User user = userRepository.findById(recordRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recordRequest.getUserId()));

        // Tìm Vocabulary
        Vocabulary vocabulary = vocabularyRepository.findById(recordRequest.getVocabularyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", "id", recordRequest.getVocabularyId()));

        // Tìm hoặc tạo mới bản ghi lịch sử
        Optional<FlashcardLearningHistory> existingHistoryOpt = historyRepository.findByUserIdAndVocabularyId(user.getId(), vocabulary.getId());

        FlashcardLearningHistory historyRecord;
        if (existingHistoryOpt.isPresent()) {
            // Cập nhật bản ghi cũ
            historyRecord = existingHistoryOpt.get();
            historyRecord.setRememberCount(historyRecord.getRememberCount() + 1); // Tăng biến đếm
            historyRecord.setLearnedAt(LocalDateTime.now()); // Cập nhật thời gian học cuối
            logger.debug("Updating learning history for user {} and vocabulary {}. New count: {}", user.getId(), vocabulary.getId(), historyRecord.getRememberCount());
        } else {
            // Tạo bản ghi mới
            historyRecord = FlashcardLearningHistory.builder()
                    .user(user)
                    .vocabulary(vocabulary) // Liên kết chính
                    .rememberCount(1) // Bắt đầu đếm từ 1
                    .learnedAt(LocalDateTime.now())
                    .build();
            logger.debug("Creating new learning history for user {} and vocabulary {}.", user.getId(), vocabulary.getId());
        }

        historyRepository.save(historyRecord);
    }

    @Transactional(readOnly = true)
    public List<LearningHistoryResponse> getLearningHistoryForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<FlashcardLearningHistory> histories = historyRepository.findByUserId(userId);
        return histories.stream()
                .map(history -> mapToLearningHistoryResponse(history, user.getName()))
                .collect(Collectors.toList());
    }

    // --- ĐÃ LOẠI BỎ phương thức getLearningHistoryForUserAndFlashcard ---
    // vì không còn liên kết trực tiếp và endpoint tương ứng cũng bị loại bỏ.

    // --- Cập nhật phương thức Mapping ---
    private LearningHistoryResponse mapToLearningHistoryResponse(FlashcardLearningHistory history, String userName) {
        String vocabularyWord = "N/A";
        Long vocabularyId = null;
        if(history.getVocabulary() != null) {
            vocabularyWord = history.getVocabulary().getWord();
            vocabularyId = history.getVocabulary().getId();
        }

        return LearningHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser() != null ? history.getUser().getId() : null)
                .userName(userName)
                // --- Đã loại bỏ flashcardId, flashcardName ---
                .vocabularyId(vocabularyId)
                .vocabularyWord(vocabularyWord)
                .learnedAt(history.getLearnedAt())
                .rememberCount(history.getRememberCount())
                .build();
    }

    @Transactional(readOnly = true)
    public LearningProgressResponse getLearningProgress(Long userId, Long flashcardId) {
        // ... (Logic getLearningProgress giữ nguyên như trước, nó sẽ dùng query count và findByUserId) ...
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", flashcardId));

        long totalVocabulariesLong = flashcardRepository.countVocabulariesByFlashcardId(flashcardId);
        int totalVocabularies = (int) totalVocabulariesLong;

        // Thay vì gọi findByUserIdAndFlashcardId, lấy hết history của user rồi lọc
        List<FlashcardLearningHistory> userHistory = historyRepository.findByUserId(userId);
        Set<Long> learnedVocabularyIdsInFlashcard = userHistory.stream()
                .filter(h -> h.getVocabulary() != null && flashcardId.equals(h.getVocabulary().getFlashcard().getId())) // Lọc theo flashcardId của vocabulary
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