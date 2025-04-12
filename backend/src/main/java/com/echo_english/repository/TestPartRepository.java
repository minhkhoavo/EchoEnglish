package com.echo_english.repository;

import com.echo_english.entity.TestPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestPartRepository extends JpaRepository<TestPart, Integer> {
    List<TestPart> findByPartNumber(int partNumber);
}
