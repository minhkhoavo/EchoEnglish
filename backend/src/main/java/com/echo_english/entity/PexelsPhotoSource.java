package com.echo_english.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Dùng Lombok cho gọn hoặc tự tạo getter/setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường không khai báo
public class PexelsPhotoSource {
    @JsonProperty("original")
    private String original;
    @JsonProperty("large2x")
    private String large2x;
    @JsonProperty("large")
    private String large;
    @JsonProperty("medium")
    private String medium;
    @JsonProperty("small")
    private String small;
    @JsonProperty("portrait")
    private String portrait;
    @JsonProperty("landscape")
    private String landscape;
    @JsonProperty("tiny")
    private String tiny;
}
