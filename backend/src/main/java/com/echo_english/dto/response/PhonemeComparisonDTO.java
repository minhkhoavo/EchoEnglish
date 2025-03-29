package com.echo_english.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhonemeComparisonDTO {
    private String result;
    private int startIndex;
    private String actualPhoneme;
    private int endIndex;
    private String correctPhoneme;
}
