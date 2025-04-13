package com.echo_english.controller;

import com.echo_english.entity.TestPart;
import com.echo_english.service.TestPartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-part")
public class TestPartController {

    @Autowired
    private TestPartService testPartService;

    @GetMapping("/{partNumber}")
    public ResponseEntity<List<TestPart>> getPartNumberOne(@PathVariable Integer partNumber) {
        return ResponseEntity.status(HttpStatus.OK).body(testPartService.getByPartNumber(partNumber));
    }
}