package com.echo_english.repository;

import com.echo_english.dto.response.TestResponse;
import com.echo_english.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Integer> {
    @Query("SELECT t.testId AS testId, t.slug AS slug, t.name AS name FROM Test t")
    List<TestResponse> findAllTestSummaries();
}
