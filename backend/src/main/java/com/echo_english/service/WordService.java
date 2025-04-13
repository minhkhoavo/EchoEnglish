package com.echo_english.service;


import com.echo_english.entity.Word;
import com.echo_english.repository.MeaningRepository;
import com.echo_english.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WordService {

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private MeaningRepository meaningRepository;

    public Word getWordByWord(String word) {
        return wordRepository.findByWord(word)
                .orElseThrow(() -> new RuntimeException("Word not found: " + word));
    }

    public List<String> getPartOfSpeechForWord(String word) {
        return meaningRepository.findPartOfSpeechByWord(word);
    }

    public List<String> getLevelForWord(String word) {
        return meaningRepository.findLevelByWord(word);
    }
}