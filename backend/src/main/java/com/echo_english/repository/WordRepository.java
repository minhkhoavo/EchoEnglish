package com.echo_english.repository;

import com.echo_english.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    Word findByWord(String word);

    List<Word> findByWordStartingWithIgnoreCase(String prefix);
}