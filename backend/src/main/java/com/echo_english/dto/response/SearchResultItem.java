package com.echo_english.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SearchResultItem {
    private String title;
    private String link;
    private String snippet;
    private Pagemap pagemap;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagemap {
        private List<Map<String, String>> metatags;

        public List<Map<String, String>> getMetatags() {
            return metatags != null ? metatags : Collections.emptyList();
        }

        public void setMetatags(List<Map<String, String>> metatags) {
            this.metatags = metatags;
        }
    }

}
