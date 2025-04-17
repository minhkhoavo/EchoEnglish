package com.echo_english.entity;

// import com.fasterxml.jackson.annotation.JsonIgnore; // Có thể dùng nếu không muốn category trả về list flashcards

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
// import java.util.Set; // Không dùng Set

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Nên dùng LAZY và khởi tạo list
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flashcard> flashcards = new ArrayList<>();
}