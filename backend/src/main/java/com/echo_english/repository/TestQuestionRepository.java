package com.echo_english.repository;

import com.echo_english.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Integer> {
}
