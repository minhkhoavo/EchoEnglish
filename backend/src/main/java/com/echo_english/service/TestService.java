package com.echo_english.service;

import com.echo_english.entity.Test;
import com.echo_english.entity.TestPart; // Import TestPart
import com.echo_english.entity.TestQuestion;
import com.echo_english.entity.TestQuestionGroup;
import com.echo_english.repository.TestPartRepository; // Import TestPartRepository
import com.echo_english.repository.TestRepository;
import jakarta.persistence.EntityNotFoundException; // Good practice for specific exception
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for lazy loading if not using FETCH

import java.util.List;
import java.util.Optional;

@Service
public class TestService {
    @Autowired
    private TestRepository testRepository;

    @Autowired // Autowire the new repository
    private TestPartRepository testPartRepository;

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Test getTestById(Integer id) {
        // Fetch the test with all its parts eagerly if needed elsewhere, or keep lazy
        return testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id = " + id));
    }

    @Transactional(readOnly = true) // BẮT BUỘC cho lazy loading
    public TestPart getTestPartByNumber(Integer testId, Integer partNumber) {

        // 1. Gọi repository mới để lấy TestPart theo testId và partNumber
        TestPart testPart = testPartRepository.findDetailsByTestIdAndPartNumber(testId, partNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TestPart not found with partNumber = " + partNumber + " for Test id = " + testId));

        Integer partId = testPart.getPartId(); // Lấy partId để log


        // 2. Trigger Lazy Loading cho 'contents' và 'choices'
        triggerLazyLoading(testPart, partId); // Tách ra thành hàm riêng cho rõ ràng

         return testPart;
    }

    // Hàm helper để trigger lazy loading, tránh lặp code
    private void triggerLazyLoading(TestPart testPart, Integer partIdForLog) {
        if (testPart.getGroups() != null && !testPart.getGroups().isEmpty()) {
            log.debug("PartId {}: Found {} groups. Initializing nested collections...", partIdForLog, testPart.getGroups().size());
            for (TestQuestionGroup group : testPart.getGroups()) {
                // Trigger loading contents
                if (group.getContents() != null) {
                    group.getContents().size();
                    log.trace("PartId {}: Initialized {} contents for group {}", partIdForLog, group.getContents().size(), group.getGroupId());
                } else {
                    log.warn("PartId {}: Contents collection is null for group {}", partIdForLog, group.getGroupId());
                }

                // Trigger loading choices
                if (group.getQuestions() != null) {
                    for (TestQuestion question : group.getQuestions()) {
                        if (question.getChoices() != null) {
                            question.getChoices().size();
                            log.trace("PartId {}: Initialized {} choices for question {}", partIdForLog, question.getChoices().size(), question.getQuestionId());
                        } else {
                            log.warn("PartId {}: Choices collection is null for question {}", partIdForLog, question.getQuestionId());
                        }
                    }
                } else {
                    log.warn("PartId {}: Questions collection is null for group {}", partIdForLog, group.getGroupId());
                }
            }
        } else if (testPart.getGroups() == null) {
            log.warn("PartId {}: Groups collection is null", partIdForLog);
        } else {
            log.debug("PartId {}: No groups found.", partIdForLog);
        }
    }
}