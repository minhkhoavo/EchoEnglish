package com.echo_english.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhonemeComparisonDTO {
    private String result;
    @JsonProperty("start_index")
    private int startIndex;
    @JsonProperty("actual_phoneme")
    private String actualPhoneme;
    @JsonProperty("end_index")
    private int endIndex;
    @JsonProperty("correct_phoneme")
    private String correctPhoneme;
}
