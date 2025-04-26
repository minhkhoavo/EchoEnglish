package com.echo_english.repository;

import com.echo_english.entity.TestChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestChoiceRepository extends JpaRepository<TestChoice, Integer> {
}