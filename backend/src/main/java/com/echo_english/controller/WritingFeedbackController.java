package com.echo_english.controller;

import com.echo_english.dto.request.WritingFeedbackRequest;
import com.echo_english.service.WritingFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/writing")
public class WritingFeedbackController {
    @Autowired
    private WritingFeedbackService writingFeedbackService;

    @PostMapping(value = "/analyze")
    public ResponseEntity<String> analyzeWriting(
            @RequestBody WritingFeedbackRequest request) {
        String feedbackJson = writingFeedbackService.evaluateFeedback(request.getInputText(), request.getInputContext()
        );
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(feedbackJson);

    }
}
