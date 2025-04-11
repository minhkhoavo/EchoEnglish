package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "gram_content")
public class GramContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "gram_topic_id")
    private GramTopic topic;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "gram_subsection_id")
    private GramSubsection subsection;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "gram_section_id")
    private GramSection section;

    private int orderIndex;
    private String contentType;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    @Column(columnDefinition = "TEXT")
    private String imageSrc;

    @Column(columnDefinition = "TEXT")
    private String imageAlt;

    @Column(columnDefinition = "JSON")
    private String listItemsJson;
}
