package com.example.game;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiAPIClient {
    public static void main(String[] args) {
        // The Client reads automatically from GOOGLE_API_KEY environment variable
        Client client = new Client();

        GenerateContentResponse response = client.models.generateContent("gemini-3-flash-preview",
            "Explain how AI works in a few words", null);
        
        System.out.println(response.text());
    }
}
