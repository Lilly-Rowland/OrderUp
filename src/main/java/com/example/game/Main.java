package com.example.game;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple 2D Game Framework");

        // central image (placeholder) - replace URL with your own resource if needed
        ImageView centralImage = new ImageView(new Image("https://via.placeholder.com/200"));
        centralImage.setPreserveRatio(true);
        centralImage.setFitWidth(200);

        // buttons around the image
        Button topButton = new Button("Top");
        Button bottomButton = new Button("Bottom");
        Button leftButton = new Button("Left");
        Button rightButton = new Button("Right");

        HBox topBox = new HBox(topButton);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        HBox bottomBox = new HBox(bottomButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        VBox leftBox = new VBox(leftButton);
        leftBox.setAlignment(Pos.CENTER);
        leftBox.setPadding(new Insets(10));

        VBox rightBox = new VBox(rightButton);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPadding(new Insets(10));

        // menu button that opens a simple context menu
        Button menuButton = new Button("Menu");
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Option 1");
        MenuItem item2 = new MenuItem("Option 2");
        MenuItem item3 = new MenuItem("Option 3");
        contextMenu.getItems().addAll(item1, item2, item3);
        menuButton.setOnAction(e -> {
            contextMenu.show(menuButton, Side.BOTTOM, 0, 0);
        });

        // place menu button in top-right corner via BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(centralImage);
        root.setTop(topBox);
        root.setBottom(bottomBox);
        root.setLeft(leftBox);
        root.setRight(rightBox);

        // put menu button in a small container and align to top-right
        BorderPane topRightContainer = new BorderPane();
        topRightContainer.setRight(menuButton);
        topRightContainer.setPadding(new Insets(5));
        root.setTop(topRightContainer);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
