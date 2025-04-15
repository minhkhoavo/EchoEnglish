package com.echo_english.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@UtilityClass
@Slf4j
public class JSONUtils {
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\s*([\\s\\S]+?)\\s*```");

    public static String extractPureJson(String rawJson) {
        if (rawJson == null) return "{}";

        Matcher matcher = JSON_BLOCK_PATTERN.matcher(rawJson);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            String trimmedJson = rawJson.trim();
            if (trimmedJson.startsWith("{") && trimmedJson.endsWith("}")) {
                return trimmedJson;
            }
            log.warn("Could not extract pure JSON from response: {}", rawJson);
            return rawJson;
        }
    }
}
