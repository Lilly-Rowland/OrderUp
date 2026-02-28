package com.example.game;

public class GeminiResult {
    private String review;
    private int rating;
    private double tipPercentage;

    public GeminiResult(String review, int rating, double tipPercentage) {
        this.review = review;
        this.rating = rating;
        this.tipPercentage = tipPercentage;
    }

    public String getReview() {
        return review;
    }

    public int getRating() {
        return rating;
    }

    public double getTipPercentage() {
        return tipPercentage;
    }

}
