package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "uk_pronunciation")
    private String ukPronunciation;

    @Column(name = "us_pronunciation")
    private String usPronunciation;

    @Column(name = "uk_audio", columnDefinition = "TEXT")
    private String ukAudio;

    @Column(name = "us_audio", columnDefinition = "TEXT")
    private String usAudio;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Meaning> meanings;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Synonym> synonyms;
}