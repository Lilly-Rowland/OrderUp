package com.example.game;

import java.io.IOException;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiAPIClient {
    private static final String MODEL_ID = "models/gemini-3-flash-preview";
    private static final int MAX_RETRIES = 5;

    // Create a helper function that returns the JSON as a string
    public static String getMenuOptionsJson() {
        try (java.io.InputStream inputStream = GeminiAPIClient.class.getResourceAsStream("/menu_options.json")) {
            if (inputStream == null) {
                System.out.println("ERROR: Could not find menu_options.json");
                return null;
            }
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            System.out.println("ERROR: Failed to read menu_options.json - " + e.getMessage());
            return null;
        }
    }

    private static GenerateContentResponse generateWithRetry(Client client, String prompt) {
        long backoffMs = 1000;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return client.models.generateContent(MODEL_ID, prompt, null);
            } catch (Exception e) {
                String msg = (e.getMessage() == null) ? "" : e.getMessage().toLowerCase();
                boolean isRateLimit = msg.contains("429") || msg.contains("too many requests") || msg.contains("resource_exhausted");
                boolean isLastAttempt = attempt == MAX_RETRIES;

                if (!isRateLimit || isLastAttempt) {
                    throw e;
                }

                System.out.println("Rate limit hit (attempt " + attempt + "/" + MAX_RETRIES + "). Retrying in " + backoffMs + " ms...");

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", interruptedException);
                }

                backoffMs *= 2;
            }
        }

        throw new RuntimeException("Request failed after retries");
    }

    private static String buildSuggestionsPrompt(String menuJson){
        return "Here is my restaurant menu data. " + menuJson + 
        "\n\nBased on this menu, can you give me honest suggestions on how I can improve the restaurant?";
    }

    private static String buildFeedbackPrompt(String menuJson){
        return "Here is my restaurant menu data. " + menuJson + 
        "\n\nBased on this menu, can you give me honest feedback on how my restaurant is doing and what I can improve?";
    }

    public static String getSuggestions(){
        Client client = new Client();
        String menuJson = getMenuOptionsJson();
        if (menuJson == null) {
            return "Error: Could not load menu data.";
        }
        String prompt = buildSuggestionsPrompt(menuJson);
        GenerateContentResponse response = generateWithRetry(client, prompt);
        return response.text();
    }

    public static String getCustomerReviews(){
        Client client = new Client();
        String menuJson = getMenuOptionsJson();
        if (menuJson == null) {
            return "Error: Could not load menu data.";
        }
        String prompt = buildFeedbackPrompt(menuJson);
        GenerateContentResponse response = generateWithRetry(client, prompt);
        return response.text();
    }
}

