package com.echo_english.controller;

import com.echo_english.entity.Word;
import com.echo_english.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/words")
public class WordController {

    @Autowired
    private WordService wordService;

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords() {
        return ResponseEntity.status(HttpStatus.OK).body(wordService.getAllWords());
    }

    @GetMapping("/{word}")
    public ResponseEntity<Word> getWordByWord(@PathVariable String word) {
        return ResponseEntity.status(HttpStatus.OK).body(wordService.getWordByWord(word));
    }

    @GetMapping("/part-of-speech/{word}")
    public ResponseEntity<List<String>> getPartOfSpeech(@PathVariable String word) {
        return ResponseEntity.status(HttpStatus.OK).body(wordService.getPartOfSpeechForWord(word));
    }

    @GetMapping("/level/{word}")
    public ResponseEntity<List<String>> getLevel(@PathVariable String word) {
        return ResponseEntity.status(HttpStatus.OK).body(wordService.getLevelForWord(word));
    }
}