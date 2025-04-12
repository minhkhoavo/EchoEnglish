package com.echo_english.service;

import com.echo_english.entity.TestPart;
import com.echo_english.repository.TestPartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestPartService {
    @Autowired
    private TestPartRepository testPartRepository;

    public List<TestPart> getByPartNumber(int partNumber) {
        return testPartRepository.findByPartNumber(partNumber);
    }
}
