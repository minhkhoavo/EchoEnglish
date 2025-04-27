package com.echo_english.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LearningRecordRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId; // Sẽ được kiểm tra khớp với user đang login

    // Thay bằng vocabularyId
    @NotNull(message = "Vocabulary ID cannot be null")
    private Long vocabularyId; // ID từ vựng đã học

    // Add this field: true if remembered, false if forgotten
    private Boolean isRemembered;
}
