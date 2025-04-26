package com.echo_english.service;

import com.echo_english.entity.Test;
import com.echo_english.entity.TestPart; // Import TestPart
import com.echo_english.repository.TestPartRepository; // Import TestPartRepository
import com.echo_english.repository.TestRepository;
import jakarta.persistence.EntityNotFoundException; // Good practice for specific exception
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

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Test getTestById(Integer id) {
        // Fetch the test with all its parts eagerly if needed elsewhere, or keep lazy
        return testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id = " + id));
    }

    // New method to get a specific part of a test
    @Transactional(readOnly = true) // Good practice for read operations, ensures session is open for potential lazy loads if not using FETCH
    public TestPart getTestPartById(Integer testId, Integer partId) {
        // Use the custom repository method with eager fetching
        return testPartRepository.findByPartIdAndTestIdWithDetails(partId, testId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TestPart not found with id = " + partId + " for Test id = " + testId));

        /* // --- Alternative if using the simpler findByPartIdAndTest_TestId ---
           // This relies more on lazy loading, @Transactional is more critical here.
        Optional<TestPart> testPartOpt = testPartRepository.findByPartIdAndTest_TestId(partId, testId);
        if (testPartOpt.isEmpty()) {
            throw new EntityNotFoundException(
                    "TestPart not found with id = " + partId + " for Test id = " + testId);
        }
        // You might need to explicitly initialize collections if lazy loading causes issues during serialization
        // Hibernate.initialize(testPartOpt.get().getGroups()); // Example if needed
        return testPartOpt.get();
        */

       /* // --- Alternative without TestPartRepository (less efficient) ---
       Test test = getTestById(testId); // Fetches the whole test first
       return test.getParts().stream()
               .filter(part -> part.getPartId().equals(partId))
               .findFirst()
               .orElseThrow(() -> new EntityNotFoundException(
                       "TestPart not found with id = " + partId + " within Test id = " + testId));
       */
    }
}