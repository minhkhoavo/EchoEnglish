package com.echo_english.service;


import com.echo_english.entity.Word;
import com.echo_english.repository.MeaningRepository;
import com.echo_english.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordService {

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private MeaningRepository meaningRepository;

    public Word getWordByWord(String word) {
        return wordRepository.findByWord(word);
    }

    public List<String> getPartOfSpeechForWord(String word) {
        return meaningRepository.findPartOfSpeechByWord(word);
    }

    public List<String> getLevelForWord(String word) {
        return meaningRepository.findLevelByWord(word);
    }

    public List<Word> findWordsStartingWith(String prefix) {
        return wordRepository.findByWordStartingWithIgnoreCase(prefix);
    }
}