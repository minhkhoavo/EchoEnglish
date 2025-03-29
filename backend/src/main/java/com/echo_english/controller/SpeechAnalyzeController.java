package com.echo_english.controller;

import com.echo_english.dto.response.PhonemeComparisonDTO;
import com.echo_english.service.SpeechAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/speech")
public class SpeechAnalyzeController {
    @Autowired
    private SpeechAnalyzeService speechAnalyzeService;

    @GetMapping("/analyze/word")
    public Mono<ResponseEntity<List<PhonemeComparisonDTO>>> analyzeSpeech() {
        return speechAnalyzeService.analyzeSpeech()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}