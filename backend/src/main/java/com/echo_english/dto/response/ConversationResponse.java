package com.echo_english.dto.response;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String aiResponse;
    private List<ChecklistItemResponse> updatedChecklist;
    private boolean allTasksCompleted;
}