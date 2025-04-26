package com.echo_english.service;

import com.echo_english.dto.request.StartTestRequest;
import com.echo_english.dto.request.SubmitAnswerRequest;
import com.echo_english.dto.response.StartTestResponse;
import com.echo_english.entity.*;
import com.echo_english.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TestHistoryService {

    private static final Logger log = LoggerFactory.getLogger(TestHistoryService.class);

    @Autowired
    private TestHistoryRepository testHistoryRepository;
    @Autowired
    private TestHistoryDetailRepository testHistoryDetailRepository;
    @Autowired
    private UserRepository userRepository; // Now expects JpaRepository<User, Long>
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private TestQuestionRepository testQuestionRepository;
    @Autowired
    private TestChoiceRepository testChoiceRepository;

    @Transactional
    public StartTestResponse startTest(StartTestRequest request) {
        // Use Long userId from request
        log.info("Attempting to start test for user id: {}, test: {}", request.getUserId(), request.getTestId());

        // Find user by Long id
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + request.getTestId()));

        TestHistory testHistory = new TestHistory();
        testHistory.setUser(user);
        testHistory.setTest(test);
        testHistory.setStartedAt(LocalDateTime.now());
        testHistory.setDetails(new java.util.ArrayList<>()); // Initialize details list

        TestHistory savedHistory = testHistoryRepository.save(testHistory);
        log.info("Test started successfully with history ID: {}", savedHistory.getId());
        return new StartTestResponse(savedHistory.getId());
    }

    @Transactional
    public void submitAnswer(SubmitAnswerRequest request) {
        // This method doesn't directly use userId, so no change needed here
        // for the User ID part. Logic remains the same.
        log.info("Attempting to submit answer for history: {}, question: {}, choice: {}",
                request.getTestHistoryId(), request.getQuestionId(), request.getChoiceId());

        TestHistory testHistory = testHistoryRepository.findById(request.getTestHistoryId())
                .orElseThrow(() -> new EntityNotFoundException("TestHistory not found with id: " + request.getTestHistoryId()));

        if (testHistory.getCompletedAt() != null) {
            log.warn("Attempted to submit answer for already completed test history ID: {}", testHistory.getId());
            throw new IllegalStateException("Test has already been completed.");
        }

        TestQuestion question = testQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("TestQuestion not found with id: " + request.getQuestionId()));

        TestChoice choice = testChoiceRepository.findById(request.getChoiceId())
                .orElseThrow(() -> new EntityNotFoundException("TestChoice not found with id: " + request.getChoiceId()));

        boolean choiceBelongsToQuestion = question.getChoices().stream()
                .anyMatch(qChoice -> qChoice.getChoiceId().equals(choice.getChoiceId()));
        if (!choiceBelongsToQuestion) {
            log.error("Invalid submission: Choice ID {} does not belong to Question ID {}", choice.getChoiceId(), question.getQuestionId());
            throw new IllegalArgumentException("Selected choice does not belong to the specified question.");
        }

        Optional<TestHistoryDetail> existingDetailOpt = testHistoryDetailRepository
                .findByTestHistoryIdAndQuestionQuestionId(testHistory.getId(), question.getQuestionId());

        TestHistoryDetail detailToSave;
        if (existingDetailOpt.isPresent()) {
            detailToSave = existingDetailOpt.get();
            detailToSave.setChoice(choice);
            log.debug("Updating existing TestHistoryDetail for historyId: {}, questionId: {}", testHistory.getId(), question.getQuestionId());
        } else {
            detailToSave = new TestHistoryDetail();
            detailToSave.setTestHistory(testHistory);
            detailToSave.setQuestion(question);
            detailToSave.setChoice(choice);
            testHistory.getDetails().add(detailToSave);
            log.debug("Creating new TestHistoryDetail for historyId: {}, questionId: {}", testHistory.getId(), question.getQuestionId());
        }

        testHistoryDetailRepository.save(detailToSave);
        log.info("Answer submitted successfully for history: {}, question: {}", request.getTestHistoryId(), request.getQuestionId());
    }

    @Transactional
    public TestHistory completeTest(Long historyId) {
        // This method doesn't directly use userId, so no change needed here
        // for the User ID part. Logic remains the same.
        log.info("Attempting to complete test for history ID: {}", historyId);

        TestHistory testHistory = testHistoryRepository.findByIdWithDetails(historyId)
                .orElseThrow(() -> new EntityNotFoundException("TestHistory not found with id: " + historyId));

        if (testHistory.getCompletedAt() != null) {
            log.warn("Test history ID: {} is already completed.", historyId);
            return testHistory;
        }

        testHistory.setCompletedAt(LocalDateTime.now());

        int totalQuestions = 0;
        int correctAnswers = 0;

        if (testHistory.getDetails() != null) {
            for (TestHistoryDetail detail : testHistory.getDetails()) {
                totalQuestions++;
                TestQuestion question = detail.getQuestion();
                TestChoice userChoice = detail.getChoice();

                if (question != null && userChoice != null && question.getCorrectAnswerLabel() != null) {
                    boolean isCorrect = question.getCorrectAnswerLabel().equalsIgnoreCase(userChoice.getChoiceLabel());
                    detail.setIsCorrect(isCorrect);
                    if (isCorrect) {
                        correctAnswers++;
                    }
                } else {
                    detail.setIsCorrect(false);
                    log.warn("Could not determine correctness for detail ID {} due to missing question, choice, or correct answer label.", detail.getId());
                }
            }
        } else {
            log.warn("No details found for TestHistory ID {} during completion.", historyId);
        }

        testHistory.setTotalQuestions(totalQuestions);
        testHistory.setCorrectAnswers(correctAnswers);
        if (totalQuestions > 0) {
            testHistory.setScore(((double) correctAnswers / totalQuestions) * 100.0);
        } else {
            testHistory.setScore(0.0);
        }

        TestHistory savedHistory = testHistoryRepository.save(testHistory);
        log.info("Test history ID: {} completed. Score: {}/{} ({}%)",
                savedHistory.getId(), correctAnswers, totalQuestions, savedHistory.getScore());
        return savedHistory;
    }

    @Transactional(readOnly = true)
    // Change method parameter type from String to Long
    public List<TestHistory> getUserTestHistory(Long userId) {
        log.debug("Fetching test history for user ID: {}", userId);
        // Ensure user exists using Long id
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        // Use the updated repository method
        return testHistoryRepository.findByUser_IdOrderByStartedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public TestHistory getTestHistoryDetails(Long historyId) {
        // This method doesn't directly use userId, so no change needed here
        log.debug("Fetching details for test history ID: {}", historyId);
        return testHistoryRepository.findByIdWithDetails(historyId)
                .orElseThrow(() -> new EntityNotFoundException("TestHistory not found with id: " + historyId));
    }
}