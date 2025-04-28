package com.echo_english.repository;

import com.echo_english.entity.SentenceAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentenceAnalysisResultRepository extends MongoRepository<SentenceAnalysisResult, String> {
}