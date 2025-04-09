package com.echo_english.repository;

import com.echo_english.entity.Vocabulary;
import com.echo_english.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByWord(String word);
}