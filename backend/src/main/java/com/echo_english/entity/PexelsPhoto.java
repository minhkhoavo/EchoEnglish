package com.echo_english.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PexelsPhoto {
    @JsonProperty("id")
    private int id;
    @JsonProperty("width")
    private int width;
    @JsonProperty("height")
    private int height;
    @JsonProperty("url")
    private String pexelsUrl; // URL trang Pexels
    @JsonProperty("photographer")
    private String photographer;
    @JsonProperty("photographer_url")
    private String photographerUrl;
    @JsonProperty("photographer_id")
    private int photographerId;
    @JsonProperty("avg_color")
    private String avgColor;
    @JsonProperty("src") // Object chứa các URL ảnh
    private PexelsPhotoSource src;
    @JsonProperty("liked")
    private boolean liked;
    @JsonProperty("alt")
    private String alt;
}