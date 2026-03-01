package com.example.game;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Initialize backend components
    RestaurantSimulator simulator = new RestaurantSimulator(1, 1, 100000.0, 2500.0);
    // Metrics UI helper is available via Metrics.getMetrics() when needed
    // a VBox and ListView that will show the current menu items in the main UI
    VBox menuDisplay = new VBox(4);
    menuDisplay.setPadding(new Insets(6));
    menuDisplay.setPrefWidth(200);
    javafx.scene.control.ListView<String> menuListView = new javafx.scene.control.ListView<>();
    menuListView.setPrefWidth(200);

    // load default menu options and pick 3 random items as the starting menu
    try (InputStreamReader r = new InputStreamReader(Main.class.getResourceAsStream("/menu_options.json"))) {
        Gson g = new Gson();
        Type t = new TypeToken<List<MenuItem>>(){}.getType();
        List<MenuItem> all = g.fromJson(r, t);
        if (all != null && !all.isEmpty()) {
            Collections.shuffle(all, new Random());
            List<MenuItem> chosen = new ArrayList<>();
            for (int i = 0; i < Math.min(3, all.size()); i++) {
                MenuItem src = all.get(i);
                chosen.add(new MenuItem(src.name, src.price, src.quality));
            }
            simulator.setMenu(chosen);
        }
    } catch (Exception ex) {
        // ignore - will start with empty menu
    }

    MenuManager menuManager = new MenuManager(simulator, menuDisplay, menuListView);
    EventManager eventManager = new EventManager();

    // CREATE MAIN WINDOW
    primaryStage.setTitle("Order Up");

    // shared styles
    final String BUTTON_STYLE = "-fx-background-color: linear-gradient(#ffffff,#e6e6e6); -fx-border-color:#cfcfcf; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 10; -fx-font-weight:bold;";
    final String TOPBAR_STYLE = "-fx-background-color: linear-gradient(#ffffff, #f3f3f3); -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
    final String MENU_STYLE = "-fx-border-color:#ddd; -fx-border-width:1; -fx-background-color:#fbfbfb; -fx-padding:6;";

        // Create restaurant image
        Image image = new Image(getClass().getResource("/images/restaurant_v1.png").toExternalForm());
        ImageView centralImage = new ImageView(image);
        centralImage.setPreserveRatio(true);
        centralImage.setFitWidth(200);

        // Log area on center-right
        // Create Log Area
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefColumnCount(20);
        logArea.setPrefRowCount(10);

        // LEFT/ CENTER/ RIGHT layout per new design
        // LEFT: Year/Month label (updates), customize menu (real), metrics button, STATS (rent, last spending)
        Label yearMonthLabel = new Label();
        yearMonthLabel.setStyle("-fx-font-weight:bold;");
        updateYearMonthText(yearMonthLabel, simulator.getYear(), simulator.getMonth());

        // keep existing metric labels for internal updates
        Label monthlyEarningsLabel = new Label(String.format("Monthly earnings: $%.2f", simulator.getMonthlyEarnings()));
        Label customersLabel = new Label(String.format("Customers: %d", simulator.getNumCustomers()));
        Label sizeLabel = new Label(String.format("Size: %d", simulator.getSize()));
        Label ratingLabel = new Label(String.format("Rating: %.2f", simulator.getRating()));
        // make rating more visible in the left column
        ratingLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#333;");
        ratingLabel.setPadding(new Insets(4,0,4,0));
    
            Button totalMoneyButton = new Button();
            totalMoneyButton.setFocusTraversable(false);
            updateTotalMoneyButton(totalMoneyButton, simulator.getTotalMoney());

        Button upgradeSizeButton = new Button();
        Runnable refreshUpgradeButton = () -> {
            double cost = simulator.getNextUpgradeCost();
            upgradeSizeButton.setText(String.format("Upgrade size ($%.0f)", cost));
            if (simulator.getTotalMoney() < cost) {
                upgradeSizeButton.setDisable(true);
                upgradeSizeButton.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: #888; -fx-border-color:#cfcfcf; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 10; -fx-font-weight:bold;");
            } else {
                upgradeSizeButton.setDisable(false);
                upgradeSizeButton.setStyle(BUTTON_STYLE);
            }
        };

        upgradeSizeButton.setOnAction(e -> {
            RestaurantSimulator.UpgradeResult ur = simulator.upgradeSize();
            updateTotalMoneyButton(totalMoneyButton, simulator.getTotalMoney());
            sizeLabel.setText(String.format("Size: %d", simulator.getSize()));
            if(ur.success){
                String message = String.format("Upgrade successful! \n Current Level: %s Cost: -$%.2f", ur.message.toString(), ur.cost);
                appendLog(logArea, message);
            }else{
                String message = String.format("Failed to upgrade size. Not enough money.", ur.cost);
                appendLog(logArea, message);
            }
            refreshUpgradeButton.run();
        });
        // initial refresh
        refreshUpgradeButton.run();

        // Customize menu button from MenuManager - wrap so we can refresh UI after popup closes
        Button rawMenuBtn = menuManager.createMenuButton(primaryStage);
        rawMenuBtn.setStyle(BUTTON_STYLE);
        Button customizeButton = new Button("Customize Menu");
        customizeButton.setOnAction(e -> {
            rawMenuBtn.fire(); // opens popup and blocks until closed
            // refresh rating after potential menu changes
            ratingLabel.setText(String.format("Rating: %.2f", simulator.getRating()));
        });
        customizeButton.setStyle(BUTTON_STYLE);

        // Metrics button (same action as before)
        Button metricsButton = new Button("metrics");
        metricsButton.setOnAction(e -> {
            Queue<Integer> recentCustomers = simulator.getCustomerData();
            Queue<Double> recentRatings = simulator.getRatingData();
            Queue<Double> recentEarnings = simulator.getRecentEarnings();
            Queue<Double> recentSpendings = simulator.getRecentSpendings();
            Scene metricsScene = Metrics.getMetrics(recentCustomers, recentRatings, recentEarnings, recentSpendings);
            Stage popup = new Stage();
            popup.initOwner(primaryStage);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("METRICS");
            popup.setScene(metricsScene);
            popup.showAndWait();
    });
        
        // STATS area
        Label rentLabel = new Label(String.format("Rent: $%.2f", simulator.getRent()));
        Label lastSpendingLabel = new Label(String.format("Last month wage: $%d | delta: %.2f", simulator.getEmployeeWage(), 0.0));

        // Advance month button (will be placed in right column)
        Button advanceMonth = new Button("advance month");
        advanceMonth.setOnAction(e -> {
            RestaurantSimulator.AdvanceResult r = simulator.advanceMonth();
            updateYearMonthText(yearMonthLabel, r.year, r.month);
            updateTotalMoneyButton(totalMoneyButton, r.totalMoney);
            monthlyEarningsLabel.setText(String.format("Monthly earnings: $%.2f", r.monthlyEarnings));
            customersLabel.setText(String.format("Customers: %d", r.customers));
            sizeLabel.setText(String.format("Size: %d", r.size));
            ratingLabel.setText(String.format("Rating: %.2f", r.rating));
            printMonthlyLogs(r.year, r.month, r.monthlyEarnings, r.rent, r.employeeWage, logArea);
            rentLabel.setText(String.format("Rent: $%.2f", r.rent));
            lastSpendingLabel.setText(String.format("Last month wage: $%d | delta: %.2f", r.employeeWage, r.delta));
            simulator.updateData();
            // refresh upgrade affordance after monthly changes
            refreshUpgradeButton.run();
            // possibly trigger a random event (will show modal popup if one occurs)
            eventManager.maybeTriggerEvent(primaryStage, simulator, r, logArea);
        });

        // Set up scene layout
        BorderPane root = new BorderPane();
        root.setCenter(centralImage);

        Label menuHeader = new Label("Menu");
        menuHeader.setStyle("-fx-font-weight:bold;");
        menuHeader.setUnderline(true);
        menuDisplay.setStyle(MENU_STYLE);

    VBox leftCol = new VBox(10, yearMonthLabel, customizeButton, metricsButton, rentLabel, lastSpendingLabel, ratingLabel, upgradeSizeButton, menuHeader, menuListView);
        leftCol.setAlignment(Pos.TOP_LEFT);
        leftCol.setPadding(new Insets(8));

        Label title = new Label("OrderUp!");
        title.setStyle("-fx-font-size:24px; -fx-font-weight:bold;");
        Button howTo = new Button("How to play...");
        howTo.setOnAction(e -> {
            Stage popup = new Stage();
            popup.initOwner(primaryStage);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("How to play");
            TextArea ta = new TextArea("Place your menu, upgrade your restaurant, advance months, and manage finances.\nThis is a simple demo.");
            ta.setWrapText(true);
            ta.setEditable(false);
            VBox box = new VBox(8, ta);
            box.setPadding(new Insets(10));
            popup.setScene(new Scene(box, 360, 200));
            popup.showAndWait();
        });
        howTo.setStyle(BUTTON_STYLE);

        VBox centerCol = new VBox(10, title, howTo, centralImage);
        centerCol.setAlignment(Pos.CENTER);
        centerCol.setPadding(new Insets(8));

        // RIGHT column
        VBox rightCol = new VBox(8);
        rightCol.setPadding(new Insets(8));
        rightCol.setAlignment(Pos.TOP_RIGHT);
        totalMoneyButton.setStyle(BUTTON_STYLE);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        rightCol.getChildren().addAll(totalMoneyButton, logArea, advanceMonth);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(leftCol);
        topBar.setCenter(centerCol);
        topBar.setRight(rightCol);
        topBar.setStyle(TOPBAR_STYLE);
        root.setTop(topBar);

        // BOTTOM area: left = spening button, right = advance month
        Button spening = new Button("spening");
        spening.setOnAction(e -> {
            double rent = simulator.getRent();
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

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // helper to set year/month label text
    private static void updateYearMonthText(Label lbl, int year, int month) {
        lbl.setText(String.format("Year %d   Month %02d", year, month));
    }

    // Update money
    private static void updateTotalMoneyButton(Button btn, double total) {
        btn.setText(String.format("Total: $%.2f", total));
        if (total >= 0) {
            btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;"); // green
        } else {
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); // red
        }
    }

    // Log helper — prefix with timestamp
    private static void appendLog(TextArea logArea, String line) {
        String entry = String.format("%s", line);
        String prev = logArea.getText();
        if (prev == null || prev.isEmpty()) {
            logArea.setText(entry);
        } else {
            logArea.setText(prev + "\n" + entry);
        }
        logArea.positionCaret(logArea.getText().length());
    }

    private static void printMonthlyLogs(int year, int month, double monthlyEarnings, double rent, int employeeWage, TextArea logArea) {
        appendLog(logArea, "------" + String.format("%d-%02d", year, month) + "------");
        appendLog(logArea, "Paid Rent: -$" + String.format("%.2f", rent));
        appendLog(logArea, "Paid Employee Wages: -$" + employeeWage);
        appendLog(logArea, "Monthly Earnings: +$" + String.format("%.2f", monthlyEarnings));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
