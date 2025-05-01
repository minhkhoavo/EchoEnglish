package com.echo_english.controller;

import com.echo_english.dto.request.WritingFeedbackRequest;
import com.echo_english.service.WritingFeedbackService;
import com.echo_english.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
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

    @GetMapping("/result/my")
    public List<Map> getWritingFeedbacksCurrentUser() {
        return writingFeedbackService.getWritingFeedbacksCurrentUser();
    }
}
