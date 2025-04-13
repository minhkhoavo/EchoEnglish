package com.echo_english.controller;

import com.echo_english.entity.GramSection;
import com.echo_english.entity.Word;
import com.echo_english.service.GramSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grammars")
public class GramSectionController {

    @Autowired
    private GramSectionService gramSectionService;

    @GetMapping
    public ResponseEntity<List<GramSection>> getAllSections() {
        List<GramSection> sections = gramSectionService.getAllSections();
        System.out.println(sections);
        return ResponseEntity.status(HttpStatus.OK).body(sections);
    }

}
