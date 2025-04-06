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

    public List<Word> getAllWords() {
        List<Word> words = wordRepository.findAll();
        for (Word word : words) {
            if (word.getImageUrl() != null && !word.getImageUrl().isEmpty()) {
                String formattedUrl = "https://raw.githubusercontent.com/vovantri123/my-images/main/" + word.getImageUrl();
                word.setImageUrl(formattedUrl);
            }
        }
        return words;
    }

    public Word getWordByWord(String word) {
        Word foundWord = wordRepository.findByWord(word);
//        if (foundWord == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found: " + word);
//        }

        if (foundWord.getImageUrl() != null && !foundWord.getImageUrl().isEmpty()) {
            String newUrl = "https://raw.githubusercontent.com/vovantri123/my-images/main/" + foundWord.getImageUrl();
            foundWord.setImageUrl(newUrl);
        }

        return foundWord;
    }

    public List<String> getPartOfSpeechForWord(String word) {
        return meaningRepository.findPartOfSpeechByWord(word);
    }

    public List<String> getLevelForWord(String word) {
        return meaningRepository.findLevelByWord(word);
    }
}