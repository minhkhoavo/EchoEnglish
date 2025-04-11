package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "gram_section")
public class GramSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<GramSubsection> subsections = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<GramContent> contents = new ArrayList<>();
}
