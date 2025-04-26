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
            "LEFT JOIN FETCH tp.test t " +             // Fetch Test cha
            "LEFT JOIN FETCH tp.groups g " +          // Fetch Set groups
            "LEFT JOIN FETCH g.questions q " +       // Fetch List questions (OK vì groups là Set)
            "WHERE t.testId = :testId AND tp.partNumber = :partNumber") // Lọc theo testId VÀ partNumber
    Optional<TestPart> findDetailsByTestIdAndPartNumber(
            @Param("testId") Integer testId,
            @Param("partNumber") Integer partNumber); // Tham số là partNumber
}
