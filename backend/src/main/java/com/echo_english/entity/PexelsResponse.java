package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PexelsResponse {
    @JsonProperty("page")
    private int page;
    @JsonProperty("per_page")
    private int perPage;
    @JsonProperty("photos") // Danh sách ảnh
    private List<PexelsPhoto> photos;
    @JsonProperty("total_results")
    private int totalResults;
    @JsonProperty("next_page")
    private String nextPage;
    @JsonProperty("prev_page")
    private String prevPage;
}