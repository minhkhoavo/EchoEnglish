package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "gram_subsection")
public class GramSubsection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gram_section_id")
    @JsonBackReference
    private GramSection section;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @OneToMany(mappedBy = "subsection", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<GramTopic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "subsection", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<GramContent> contents = new ArrayList<>();
}
