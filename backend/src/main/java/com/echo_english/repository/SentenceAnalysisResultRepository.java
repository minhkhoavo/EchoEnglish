package com.echo_english.repository;

import com.echo_english.entity.SentenceAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceAnalysisResultRepository extends MongoRepository<SentenceAnalysisResult, String> {
    List<SentenceAnalysisResult> findByStatus(String status);
}