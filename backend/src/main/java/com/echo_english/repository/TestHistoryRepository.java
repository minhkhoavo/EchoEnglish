package com.echo_english.repository;

import com.echo_english.entity.TestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestHistoryRepository extends JpaRepository<TestHistory, Long> {

    // Find history for a specific user by navigating the relationship user -> id
    // Change the parameter type from String to Long
    List<TestHistory> findByUser_IdOrderByStartedAtDesc(Long userId);

    // Find a specific history record and eagerly fetch its details and related entities
    // This query remains the same as it fetches based on historyId
    @Query("SELECT DISTINCT th FROM TestHistory th " +
            "LEFT JOIN FETCH th.details d " +
            "LEFT JOIN FETCH d.question q " +
            "LEFT JOIN FETCH d.choice c " +
            "LEFT JOIN FETCH q.choices qc " + // Fetch all choices for the question for score calculation
            "LEFT JOIN FETCH th.user u " +    // Fetch user info if needed
            "LEFT JOIN FETCH th.test t " +    // Fetch test info if needed
            "WHERE th.id = :historyId")
    Optional<TestHistory> findByIdWithDetails(@Param("historyId") Long historyId);
}