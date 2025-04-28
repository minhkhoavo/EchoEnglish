package com.echo_english.service;

import com.echo_english.utils.JSONUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class WritingFeedbackService {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MongoTemplate mongoTemplate;
    public String evaluateFeedback(String inputText, String inputContext) {
        try {
            String prompt = buildWritingFeedbackPrompt(inputText, inputContext);
            String feedbackJson = JSONUtils.extractPureJson(chatClient.prompt(prompt).call().content());

            Map<String, Object> result = new HashMap<>();
            result.put("inputText", inputText);
            result.put("inputContext", inputContext);
            result.put("feedback", objectMapper.readValue(feedbackJson, Map.class));
            result.put("date", LocalDateTime.now());
            result.put("_id", UUID.randomUUID().toString());

            mongoTemplate.insert(result, "feedback_results");

            return feedbackJson;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process feedback: " + e.getMessage());
        }
    }

    private String buildWritingFeedbackPrompt(String inputText, String inputContext) {
        return """
            ROLE: You are a highly experienced expert in English language analysis and writing assessment, possessing the ability to provide deep analysis and detailed, constructive feedback. Your task is to evaluate an English text passage based on standard academic criteria (similar to IELTS Writing) and return the analysis results as a single JSON object, strictly adhering to the specified structure.
            TASK: Analyze the English text provided below. Identify strengths, weaknesses, grammatical errors, vocabulary issues, coherence/cohesion problems, and propose specific improvements for each sentence and the overall text. Return the ENTIRE analysis result as a single, valid JSON object. Do NOT add any explanatory text outside this JSON object.
            INPUT TEXT:
            %s
            
            (Optional) TOPIC CONTEXT:
            %s
            
            OUTPUT REQUIREMENTS:
            You MUST generate a single JSON object with the following structure. Pay close attention to each field and its detailed requirements:
            {
              "original_text": "...", // **IMPORTANT:** Contains the ENTIRE original text provided in the INPUT TEXT section.
              "overall_assessment": {
                "task_achievement": { // How well the text addresses the (implied) task
                  "score": null, // Leave null or provide a brief descriptive assessment (e.g., "Partially Addressed", "Fully Addressed")
                  "comments": [ // Provide 1-3 overview comments on task fulfillment (if inferable from the text)
                    "Comment 1...",
                    "Comment 2..."
                  ]
                },
                "coherence_cohesion": { // Organization, flow, linking
                  "score": null,
                  "comments": [ // Provide 1-3 overview comments on overall coherence, logic, paragraph organization, use of linking words.
                    "Comment 1...",
                    "Comment 2..."
                  ]
                },
                "lexical_resource": { // Vocabulary range, accuracy, appropriacy
                  "score": null,
                  "comments": [ // Provide 1-3 overview comments on vocabulary diversity, accuracy, and appropriacy. Mention common errors if present.
                    "Comment 1...",
                    "Comment 2..."
                  ]
                },
                "grammatical_range_accuracy": { // Sentence structures, grammar correctness
                  "score": null,
                  "comments": [ // Provide 1-3 overview comments on sentence structure variety and grammatical accuracy. Mention common errors if present.
                    "Comment 1...",
                    "Comment 2..."
                  ]
                },
                "overall_impression": {
                  "score": null, // Can be null or an estimated overall score (e.g., "Band 6.0 Estimate")
                  "summary": "Brief summary (1-2 sentences) of the overall impression of the writing, highlighting key strengths/weaknesses."
                }
              },
              "detailed_breakdown": [ // Array containing objects representing each PARAGRAPH
                // --- START OF A PARAGRAPH OBJECT ---
                {
                  "paragraph_index": 0, // Start from 0 for the first paragraph
                  "original_paragraph": "...", // The original text OF THIS PARAGRAPH. Ensure it matches original_text.
                  "paragraph_level_analysis": { // Analysis at the PARAGRAPH level
                    "topic_sentence_clarity": "Assessment of the topic sentence (clear, faulty, missing, etc.).",
                    "idea_development": "Assessment of idea development within the paragraph (sufficient, superficial, logical, etc.).",
                    "cohesion_within_paragraph": "Assessment of linking BETWEEN SENTENCES within the paragraph (good, needs improvement, effective/ineffective use of cohesive devices, etc.).",
                    "transition_to_next": "Assessment of the transition to the NEXT paragraph (smooth, abrupt, missing link, N/A if last paragraph)."
                  },
                  "sentences": [ // Array containing objects representing each SENTENCE within the paragraph
                    // --- START OF A SENTENCE OBJECT ---
                    {
                      "sentence_index": 0, // Start from 0 for the first sentence in the paragraph
                      "original_sentence": "...", // **IMPORTANT:** The exact original sentence from the text.
                      "upgraded_sentence": "...", // **IMPORTANT:** Provide an ERROR-CORRECTED AND ENHANCED version of the original sentence. It should be natural and improve grammar/vocabulary/cohesion.
                      "analysis_points": [ // Array containing detailed analysis points for THIS SENTENCE
                        // --- EXAMPLE ANALYSIS POINT (CAN HAVE MULTIPLE) ---
                        {
                          "criterion": "Grammatical Range and Accuracy", // (TA, CC, LR, GRA - reflecting IELTS criteria)
                          "type": "grammar_error", // Type: e.g., grammar_error, vocab_issue, inappropriate_word_choice, spelling_error, punctuation_error, linking_issue, style_suggestion, positive_point, clarity_issue, awkward_phrasing
                          "issue": "Brief description of the problem (e.g., Subject-Verb Agreement, Incorrect Tense, Missing Article, Word Form Error, Awkward Phrasing, Basic Linking Word)",
                          "explanation": "DETAILED explanation of the error or issue.",
                          "suggestion": "Specific suggestion for correction (can overlap with or elaborate on upgraded_sentence)."
                        }
                        // --- ADD OTHER ANALYSIS POINTS AS NEEDED ---
                      ],
                      "related_vocabulary": [ // Array containing ADVANCED and RELEVANT vocabulary related to the sentence's topic
                        {
                          "word_or_phrase": "Advanced word/phrase 1",
                          "definition_or_usage": "Brief definition or usage in a relevant context."
                        },
                        {
                          "word_or_phrase": "Related collocation",
                          "definition_or_usage": "Common word combination relevant to the sentence topic."
                        }
                        // --- ADD OTHER VOCABULARY AS NEEDED ---
                      ],
                      "identified_errors_summary": [ // Array for a QUICK summary of main error types in this sentence
                         {
                           "error_type": "grammar", // Error category: "grammar", "vocabulary", "linking", "spelling", "punctuation", "style", "clarity", "context"
                           "description": "Brief description of the error type (e.g., Subject-Verb Agreement, Word Choice, Spelling)"
                         }
                         // --- ADD OTHER IDENTIFIED ERRORS ---
                      ]
                    }
                    // --- END OF SENTENCE OBJECT, ADD OTHER SENTENCES IN THE PARAGRAPH ---
                  ]
                }
                // --- END OF PARAGRAPH OBJECT, ADD OTHER PARAGRAPHS ---
              ]
            }
            CONSTRAINTS & GUIDELINES:
            JSON STRICTNESS: The output MUST be a single, valid JSON object. No introductory, explanatory, or concluding text outside the main JSON object's curly braces {}. NO Markdown formatting (like ```json). Do NOT wrap the JSON output in Markdown code fences (e.g., ```json ... ```) or any other formatting syntax
            COMPLETENESS: Populate all required fields in the JSON structure. If there are no errors or suggestions for a specific item (e.g., related_vocabulary or analysis_points for a perfect sentence), use an empty array [].
            ACCURACY: The analysis must be accurate. Correctly identify grammatical, spelling, vocabulary, and punctuation errors.
            SPECIFICITY: In analysis_points, be specific about the issue and provide a clear explanation. The suggestion must be constructive.
            UPGRADED SENTENCE: The upgraded_sentence field is crucial. It should not only fix errors but also demonstrate improvements in phrasing, vocabulary, or sentence structure where possible, making the sentence more natural and effective.
            CONSISTENCY: Ensure analysis_points, identified_errors_summary, and upgraded_sentence are consistent. Errors mentioned in analysis_points and identified_errors_summary must be addressed in the upgraded_sentence.
            THOROUGHNESS: Analyze each sentence, paragraph, and the overall text meticulously. Do not overlook obvious errors or areas for improvement. Include analysis of links between sentences (linking_issue in analysis_points) and between paragraphs (cohesion_within_paragraph, transition_to_next in paragraph_level_analysis).
            RELATED VOCABULARY: Provide vocabulary that is genuinely relevant and represents an upgrade from the original sentence's wording.
            
            """.formatted(inputText, inputContext);
    }

}
