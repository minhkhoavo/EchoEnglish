package com.echo_english.controller;

import com.echo_english.entity.Test;
import com.echo_english.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping
    public ResponseEntity<List<Test>> getAllTests() {
        return ResponseEntity.status(HttpStatus.OK).body(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Integer id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

}