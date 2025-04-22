package com.echo_english.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartConversationRequest {
    private String context;
    private String initialUserInput;
}
