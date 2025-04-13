package com.echo_english.controller;

import com.echo_english.dto.response.ApiResponse;
import com.echo_english.entity.Flashcard;
import com.echo_english.service.FlashcardService;
import com.echo_english.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashcards")
public class FlashcardController {
    @Autowired
    private FlashcardService flashcardService;

}
