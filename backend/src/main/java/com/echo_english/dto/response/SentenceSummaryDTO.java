package com.echo_english.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentenceSummaryDTO {
    private double total_duration;
    private int word_count;
    private double speaking_rate_wpm;
    private int filter_word_count;

    @JsonProperty("total_duration_feedback")
    public String getTotalDurationFeedback() {
        if (total_duration < 1.0) {
            return "The duration is too short. Consider extending your speech to provide more context and details.";
        } else if (total_duration > 3.0) {
            return "The duration is quite long. Try to be more concise and focus on the key points for clarity.";
        }
        return "The duration is appropriate. Keep up the good work and consider slight adjustments for optimal engagement.";
    }

    @JsonProperty("word_count_feedback")
    public String getWordCountFeedback() {
        if (word_count < 5) {
            return "The word count is low. Consider adding more information to make your message more comprehensive.";
        } else if (word_count > 10) {
            return "The word count is high. Try to simplify and shorten your message to improve clarity and impact.";
        }
        return "The word count is balanced. Maintain this level and focus on clarity and conciseness.";
    }

    @JsonProperty("speaking_rate_wpm_feedback")
    public String getSpeakingRateFeedback() {
        if (speaking_rate_wpm < 100) {
            return "The speaking rate is slow. Increasing the pace slightly could help maintain the listener's interest.";
        } else if (speaking_rate_wpm > 200) {
            return "The speaking rate is fast. Consider slowing down to ensure clarity and better comprehension.";
        }
        return "The speaking rate is ideal. Continue with this pace, but always be mindful of your audience's understanding.";
    }

    @JsonProperty("filter_word_count_feedback")
    public String getFilterWordCountFeedback() {
        if (filter_word_count == 0) {
            return "No filtered words detected. This indicates that the speech content is clean, but you may review it for language refinement.";
        }
        return "Some words were filtered. Review the filtered words and consider adjusting your language to maintain clarity and appropriateness.";
    }
}
