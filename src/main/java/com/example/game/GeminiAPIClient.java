package com.example.game;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiAPIClient {
    public static void main(String[] args) {
        // The Client reads automatically from GOOGLE_API_KEY environment variable
        Client client = new Client();

        // Write some restaurant prompts that help with customer satisfaction
        GenerateContentResponse customer_fb = client.models.generateContent("gemini-3-flash-preview",
            "Give me a suggestion for improving customer satisfaction in a restaurant", null);
        
        System.out.println(customer_fb.text());
    }
}
