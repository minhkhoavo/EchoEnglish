package com.echo_english.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StartTestRequest {
    private Long userId; // ID of the user starting the test
    private Integer testId; // ID of the overall test
}