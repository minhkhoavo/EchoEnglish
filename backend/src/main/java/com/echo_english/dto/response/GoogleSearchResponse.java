package com.echo_english.dto.response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GoogleSearchResponse {
    private List<SearchResultItem> items;
}
