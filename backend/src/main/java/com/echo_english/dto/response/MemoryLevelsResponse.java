package com.echo_english.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoryLevelsResponse {
    private long level0; // rememberCount = 0
    private long level1; // rememberCount = 1
    private long level2; // rememberCount = 2
    private long level3; // rememberCount = 3
    private long level4; // rememberCount = 4
    private long mastered; // rememberCount >= 5
}