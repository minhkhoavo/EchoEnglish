package com.echo_english.repository;

import com.echo_english.entity.TestPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestPartRepository extends JpaRepository<TestPart, Integer> {
    List<TestPart> findByPartNumber(int partNumber);

    @Query("SELECT DISTINCT tp FROM TestPart tp " +
            "LEFT JOIN FETCH tp.groups g " +
            "LEFT JOIN FETCH g.questions q " +
            "LEFT JOIN FETCH g.contents c " + // Fetch contents associated with the group
            "LEFT JOIN FETCH q.choices ch " + // Fetch choices associated with the question
            "WHERE tp.partId = :partId AND tp.test.testId = :testId")
    Optional<TestPart> findByPartIdAndTestIdWithDetails(
            @Param("partId") Integer partId,
            @Param("testId") Integer testId);
}
