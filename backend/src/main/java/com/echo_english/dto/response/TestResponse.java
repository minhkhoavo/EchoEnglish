package com.echo_english.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface TestResponse {
    Integer getTestId();
    String getSlug();
    String getName();
}