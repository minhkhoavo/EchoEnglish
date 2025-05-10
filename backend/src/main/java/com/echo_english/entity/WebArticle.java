package com.echo_english.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 1024)
    private String url;

    @Column(nullable = false, length = 512)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String snippet;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String processedContent;

    @Column(length = 100)
    private String source;

    private LocalDateTime publishedDate;

    @Column(nullable = false)
    private boolean suitableForLearners;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String moderationNotes;
}
