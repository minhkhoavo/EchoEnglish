package com.echo_english.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SearchResultItem {
    private String title;
    private String link;
    private String snippet;

    @Override
    public String toString() {
        return "SearchResultItem{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}
