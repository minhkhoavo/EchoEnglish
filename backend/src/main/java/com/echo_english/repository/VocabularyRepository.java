package com.echo_english.repository;

import com.echo_english.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    List<Vocabulary> findByFlashcardId(Long flashcardId);
}
