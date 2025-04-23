package com.echo_english.dto.request;

import com.echo_english.dto.response.ChecklistItemResponse;
import com.echo_english.dto.response.ChatMessageResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConverseRequest {
    private List<ChatMessageResponse> history;
    private String currentUserInput;
    private String context;
    private List<ChecklistItemResponse> currentChecklist;
}
