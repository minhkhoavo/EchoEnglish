package com.echo_english.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItemResponse {
    private String id;
    private String description;
    private boolean completed;
}
