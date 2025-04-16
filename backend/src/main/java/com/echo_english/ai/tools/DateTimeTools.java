package com.echo_english.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateTimeTools {

    @Tool(name = "get_current_date", description = "Get the current date")
    public String getCurrentDate() {
        System.out.println("Get Current date:::::::: " + LocalDate.now());
        return LocalDate.now().toString();
    }

    @Tool(name = "calculate_tomorrow_date", description = "Calculate the date of tomorrow")
    public String getTomorrowDate() {
        System.out.println("Get getTomorrowDate date:::::::: ");
        return LocalDate.now().plusDays(1).toString();
    }
}