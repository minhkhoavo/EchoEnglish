package com.echo_english.repository;

import com.echo_english.entity.TestHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestHistoryDetailRepository extends JpaRepository<TestHistoryDetail, Long> {

    // Find an existing detail entry for a specific test attempt and question
    // Useful for updating an answer if the user changes their mind
    Optional<TestHistoryDetail> findByTestHistoryIdAndQuestionQuestionId(Long testHistoryId, Integer questionId);
}