package com.echo_english.repository;

import com.echo_english.entity.Meaning;
import com.echo_english.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeaningRepository extends JpaRepository<Meaning, Long> {

    @Query("SELECT DISTINCT m.partOfSpeech FROM Meaning m INNER JOIN m.word w WHERE w.word = :word")
    List<String> findPartOfSpeechByWord(String word);

    @Query("SELECT DISTINCT m.level FROM Meaning m INNER JOIN m.word w WHERE w.word = :word AND m.level IS NOT NULL")
    List<String> findLevelByWord(String word);
}