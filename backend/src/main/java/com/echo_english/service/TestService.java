package com.echo_english.service;

import com.echo_english.entity.Test;
import com.echo_english.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {
    @Autowired
    private TestRepository testRepository;

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Test getTestById(Integer id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with id = " + id));
    }
}
