package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartTestResponse {
    private Long historyId; // The ID of the created TestHistory record
}