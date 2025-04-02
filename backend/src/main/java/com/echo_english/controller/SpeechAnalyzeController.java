package com.echo_english.controller;

import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.dto.response.SentenceAnalysisResultDTO;
import com.echo_english.service.SpeechAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/speech")
public class SpeechAnalyzeController {
    @Autowired
    private SpeechAnalyzeService speechAnalyzeService;

    @PostMapping(value = "/analyze/word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<List<PhonemeComparisonDTO>>> analyzeSpeech(
            @RequestParam("target_word") String targetWord,
            @RequestPart("audio_file") MultipartFile audioFile) {

        return speechAnalyzeService.analyzeSpeech(targetWord, audioFile)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PostMapping(value = "/analyze/sentences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<SentenceAnalysisResultDTO>> analyzeSentences(
            @RequestParam("target_word") String targetWord,
            @RequestPart("audio_file") MultipartFile audioFile
    ) {
        return speechAnalyzeService.analyzeSentence(targetWord, audioFile)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}