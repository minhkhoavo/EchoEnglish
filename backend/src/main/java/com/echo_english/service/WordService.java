package com.echo_english.service;


import com.echo_english.entity.Word;
import com.echo_english.repository.MeaningRepository;
import com.echo_english.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Integer> countWordsByCEFRLevel(String sentence) {
        Map<String, Integer> levelCounts = new HashMap<>();
        String[] cefrLevels = {"A1", "A2", "B1", "B2", "C1", "C2"};
        for (String level : cefrLevels) {
            levelCounts.put(level, 0);
        }

        if (sentence == null || sentence.isEmpty()) {
            return levelCounts;
        }

        String cleaned = sentence.replaceAll("\\[UM\\]|\\[UH\\]", "").replaceAll("[^a-zA-Z ]", "");
        String[] words = cleaned.trim().split("\\s+");

        for (String word : words) {
            if (word.isEmpty()) continue;
            List<String> levels = getLevelForWord(word.toLowerCase());
            if (levels != null) {
                for (String level : levels) {
                    if (levelCounts.containsKey(level)) {
                        levelCounts.put(level, levelCounts.get(level) + 1);
                    }
                }
            }
        }

        return levelCounts;
    }
}