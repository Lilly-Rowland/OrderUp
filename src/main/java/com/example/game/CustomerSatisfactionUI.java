package com.example.game;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.Scene.*;
import javafx.geometry.Insets;

public class CustomerSatisfactionUI {
    public void Feedback(GeminiResult result) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        Label review = new Label(result.getReview());
        Label rating = new Label("Rating: " + result.getRating());
        Label tip = new Label("Tip: " + result.getTipPercentage() * 100 + "%");
        VBox layout = new VBox(10, review, rating, tip);
        layout.setPadding(new Insets(15));
        popup.setScene(new Scene(layout, 300, 200));
        popup.showAndWait();
    }
}
