package com.example.game;

import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
    primaryStage.setTitle("Simple 2D Game Framework");

        // central image (placeholder) - replace URL with your own resource if needed
        Image image = new Image(getClass().getResource("/images/restaraunt_v1.png").toExternalForm());
        ImageView centralImage = new ImageView(image);
        
        centralImage.setPreserveRatio(true);
        centralImage.setFitWidth(200);

    // buttons and state (directional placeholders removed)

    // Application state: year, month, total money
    final int[] year = {2023};
    final int[] month = {1}; // 1-12
    final double[] totalMoney = {100000.0}; // starts at 100,000
    final double[] monthlyEarnings = {0.0}; // placeholder monthly earnings
    final double[] totalEarnings = {0.0}; // cumulative earnings
        Random rand = new Random();

        // Log area on center-right
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefColumnCount(20);
        logArea.setPrefRowCount(10);

        // Year & Month button (top-left)
        Button yearMonthButton = new Button();
        yearMonthButton.setFocusTraversable(false);
        // set initial text
        updateYearMonthText(yearMonthButton, year[0], month[0]);

    // Monthly earnings label (placeholder value shown)
    Label monthlyEarningsLabel = new Label(String.format("Monthly earnings: $%.2f", monthlyEarnings[0]));

        // Customize placeholder button which opens a popup
        Button customizePlaceholder = new Button("cusotmize menu place hold");
        customizePlaceholder.setOnAction(e -> {
            Stage popup = new Stage();
            popup.initOwner(primaryStage);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Customize Menu (placeholder)");
            TextArea ta = new TextArea("This is a placeholder for customize menu.\nAdd controls here later.");
            ta.setWrapText(true);
            ta.setEditable(false);
            Scene s = new Scene(ta, 300, 200);
            popup.setScene(s);
            popup.showAndWait();
        });

        // Metrics button (below placeholder)
        Button metricsButton = new Button("metrics");
        metricsButton.setOnAction(e -> {
            String msg = String.format("Metrics requested - year=%d month=%02d total=%.2f", year[0], month[0], totalMoney[0]);
            appendLog(logArea, msg);
        });

        // Total money button (top-right)
        Button totalMoneyButton = new Button();
        totalMoneyButton.setFocusTraversable(false);
        updateTotalMoneyButton(totalMoneyButton, totalMoney[0]);

        // Advance month button (bottom-right)
        Button advanceMonth = new Button("advance month");
        advanceMonth.setOnAction(e -> {
            // advance month and update year/month button
            month[0]++;
            if (month[0] > 12) {
                month[0] = 1;
                year[0]++;
            }
            // simulate money change when advancing month (small demo change)
            double delta = -50 + rand.nextDouble() * 250; // -50..200
            totalMoney[0] += delta;
            // placeholder monthly earnings
            double earnings = Math.round(rand.nextDouble() * 2000.0 * 100.0) / 100.0; // 0.00 - 2000.00
            monthlyEarnings[0] = earnings;
            totalEarnings[0] += earnings;
            totalMoney[0] += earnings;
            // automatically deduct rent each month
            double rent = 2500.0;
            totalMoney[0] -= rent;
            updateYearMonthText(yearMonthButton, year[0], month[0]);
            updateTotalMoneyButton(totalMoneyButton, totalMoney[0]);
            monthlyEarningsLabel.setText(String.format("Monthly earnings: $%.2f", monthlyEarnings[0]));
            appendLog(logArea, String.format("Advanced to %d-%02d: random change %.2f, earnings +%.2f, rent -%.2f, total %.2f, cumulative earnings %.2f", year[0], month[0], delta, earnings, rent, totalMoney[0], totalEarnings[0]));
        });

    // menu button that opens a simple context menu
    Button menuButton = new Button("Menu");
    ContextMenu contextMenu = new ContextMenu();
    MenuItem item1 = new MenuItem("Option 1");
    MenuItem item2 = new MenuItem("Option 2");
    MenuItem item3 = new MenuItem("Option 3");
    contextMenu.getItems().addAll(item1, item2, item3);
    menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 0));

    // place menu button in top-right corner via BorderPane
    BorderPane root = new BorderPane();
    root.setCenter(centralImage);

    // TOP bar: left (customize, year/month, metrics) and right (total money + menu)
    VBox topLeftVBox = new VBox(5, customizePlaceholder, yearMonthButton, metricsButton, monthlyEarningsLabel);
    topLeftVBox.setAlignment(Pos.TOP_LEFT);
    topLeftVBox.setPadding(new Insets(8));

    HBox topRightHBox = new HBox(8, totalMoneyButton, menuButton);
    topRightHBox.setAlignment(Pos.TOP_RIGHT);
    topRightHBox.setPadding(new Insets(8));

    BorderPane topBar = new BorderPane();
    topBar.setLeft(topLeftVBox);
    topBar.setRight(topRightHBox);
    root.setTop(topBar);

    // RIGHT (center-right) area: LOG button and log area
    Button logButton = new Button("LOG");
    logButton.setOnAction(e -> appendLog(logArea, "LOG button pressed"));
    VBox rightVBox = new VBox(6, logButton, logArea);
    rightVBox.setAlignment(Pos.CENTER_RIGHT);
    rightVBox.setPadding(new Insets(8));
    VBox.setVgrow(logArea, Priority.ALWAYS);
    root.setRight(rightVBox);

        // BOTTOM area: left = spening button, right = advance month
        Button spening = new Button("spening");
        spening.setOnAction(e -> {
            double rent = 800.0; // fixed rent amount
            Stage popup = new Stage();
            popup.initOwner(primaryStage);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Rent Info");
            Label msg = new Label(String.format("Rent this month: $%.2f\nRent is automatically deducted when the month advances.", rent));
            VBox box = new VBox(10, msg);
            box.setPadding(new Insets(10));
            Scene ps = new Scene(box, 320, 100);
            popup.setScene(ps);
            popup.showAndWait();
        });

        BorderPane bottomBar = new BorderPane();
        bottomBar.setLeft(spening);
        BorderPane.setAlignment(spening, Pos.CENTER_LEFT);
        bottomBar.setRight(advanceMonth);
        BorderPane.setAlignment(advanceMonth, Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10));
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // helper to set year/month button text
    private static void updateYearMonthText(Button btn, int year, int month) {
        btn.setText(String.format("Year %d   Month %02d", year, month));
    }

    // helper to update total money button text and color
    private static void updateTotalMoneyButton(Button btn, double total) {
        btn.setText(String.format("Total: $%.2f", total));
        if (total >= 0) {
            btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;"); // green
        } else {
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); // red
        }
    }

    // helper to append a line to the log area
    private static void appendLog(TextArea logArea, String line) {
        String prev = logArea.getText();
        if (prev == null || prev.isEmpty()) {
            logArea.setText(line);
        } else {
            logArea.setText(prev + "\n" + line);
        }
        // scroll to end
        logArea.positionCaret(logArea.getText().length());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
