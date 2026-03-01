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

    public static void main(String[] args) {
        // The Client reads automatically from GOOGLE_API_KEY environment variable
        Client client = new Client();

        // Read the menu JSON file
        String menuJson = getMenuOptionsJson();
        if (menuJson == null) {
            System.out.println("Failed to load menu data.");
            return;
        }

        // Create a prompt that includes the JSON data
        String suggestionsPrompt = "Here is my restaurant menu data in JSON format:\n\n" + 
                        menuJson + 
                        "\n\nBased on this menu, give me 3 suggestions for how I can improve customer satisfaction in a restaurant.";

        String customersPrompt = "Here is my restaurant menu data in JSON format:\n\n" + 
                        menuJson + 
                        "\n\nBased on this menu, give me three honest reviews (can be positive or negative) that customers might leave about this restaurant.";

        // Send the prompt with embedded JSON to Gemini
        GenerateContentResponse customer_fb;
        try {
            customer_fb = generateWithRetry(client, customersPrompt);
        } catch (Exception e) {
            System.out.println("Gemini request failed after retries. If this keeps happening, wait 1-2 minutes before retrying.");
            System.out.println("Details: " + e.getMessage());
            return;
        }
        
        System.out.println(customer_fb.text());
    }
}
