package com.echo_english.service;

import com.echo_english.entity.GramSection;
import com.echo_english.repository.GramSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GramSectionService {

    @Autowired
    private GramSectionRepository gramSectionRepository;

    public List<GramSection> getAllSections() {
        return gramSectionRepository.findAll();
    }
}
