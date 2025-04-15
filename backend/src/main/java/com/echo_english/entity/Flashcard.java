package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
// import java.util.Set; // Không dùng Set nữa

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm fetch lazy
    @JoinColumn(name = "id_category", nullable = false) // Đảm bảo category không null
    // @JsonBackReference // Giữ lại nếu cần serialize Category->Flashcards
    private Category category;

    // !!! Quan trọng: Thêm liên kết đến User tạo ra flashcard !!!
    @ManyToOne(fetch = FetchType.LAZY) // Thêm fetch lazy
    @JoinColumn(name = "creator_user_id", nullable = true) // Cho phép null ban đầu nếu cần, nhưng nên là false khi tạo
    private User creator; // User tạo flashcard này

    // Sử dụng List và khởi tạo để tránh NullPointerException
    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // orphanRemoval=true để xóa vocab khi xóa khỏi list
    @JsonManagedReference // Giữ lại để serialize Flashcard->Vocabularies
    @Builder.Default // Khởi tạo list rỗng với Lombok Builder
    private List<Vocabulary> vocabularies = new ArrayList<>();
}