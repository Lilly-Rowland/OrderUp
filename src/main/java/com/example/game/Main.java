package com.example.game;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
import javafx.scene.control.Tooltip;
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
        RestaurantSimulator simulator = new RestaurantSimulator(1, 1, 10000.0, 2000.0);
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

    // shared styles using requested color scheme: #cc0066, #ff5050, #ff6600
    final String ACCENT1 = "#cc0066"; // primary
    final String ACCENT2 = "#ff5050"; // secondary
    final String ACCENT3 = "#ff6600"; // highlight
    final String BUTTON_STYLE = String.join("",
        "-fx-background-color: linear-gradient(#ffffff, derive(" + ACCENT1 + ", -10%));",
        " -fx-border-color: derive(" + ACCENT1 + ", -20%);",
        " -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12; -fx-font-weight:bold; -fx-text-fill: white;",
        " -fx-effect: dropshadow( gaussian , rgba(0,0,0,0.12) , 4,0,0,1);");
    // keep panels transparent so wallpaper is visible
    final String TOPBAR_STYLE = "-fx-background-color: transparent;";
    final String MENU_STYLE = "-fx-border-color: transparent; -fx-background-color: transparent; -fx-padding:8;";

        // Create restaurant image
        Image image = new Image(getClass().getResource("/images/restaurant_v1.png").toExternalForm());
        ImageView centralImage = new ImageView(image);
        centralImage.setPreserveRatio(true);
        centralImage.setFitWidth(500);

    // Log area on center-right
    // Create Log Area (styled for readability)
    TextArea logArea = new TextArea();
    logArea.setEditable(false);
    logArea.setWrapText(true);
    logArea.setPrefColumnCount(30);
    logArea.setPrefRowCount(12);
    logArea.setFocusTraversable(false);
    // monospace font and subtle background so wallpaper shows through
    logArea.setStyle("-fx-font-family: 'Menlo', 'Monaco', 'Courier New', monospace; -fx-font-size:12px; -fx-control-inner-background: rgba(255,255,255,0.06); -fx-text-fill: #111; -fx-padding:8;");

        // LEFT/ CENTER/ RIGHT layout per new design
        // LEFT: Year/Month label (updates), customize menu (real), metrics button, STATS (rent, last spending)
    Label yearMonthLabel = new Label();
    // pop-out style: larger, colored background, rounded and slightly elevated
    yearMonthLabel.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-background-color: " + ACCENT1 + "; -fx-text-fill: white; -fx-padding:6 10; -fx-background-radius:8; -fx-effect: dropshadow( gaussian , rgba(0,0,0,0.18) , 6,0,0,2);");
    yearMonthLabel.setAlignment(Pos.CENTER_LEFT);
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
                String message = String.format("Current Level: %s | Cost: -$%.2f", ur.message, ur.cost);
                appendLog(logArea, "Upgrade successful", message);
                if(simulator.getLevel() == 3){
                    centralImage.setImage(new Image(getClass().getResource("/images/restaurant_v2.png").toExternalForm()));
                }else if(simulator.getLevel() > 6){
                    centralImage.setImage(new Image(getClass().getResource("/images/restaurant_v3.png").toExternalForm()));
                }

            }else{
                String message = String.format("Needed: $%.2f", ur.cost);
                appendLog(logArea, "Upgrade failed", message);
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

    // Metrics button (styled)
    Button metricsButton = new Button("Metrics");
    metricsButton.setStyle(BUTTON_STYLE + " -fx-background-color: linear-gradient(" + ACCENT2 + ", " + ACCENT1 + "); -fx-font-size:13px; -fx-padding:8 14; -fx-text-fill: white;");
    metricsButton.setPrefWidth(140);
    metricsButton.setPrefHeight(40);
    Tooltip.install(metricsButton, new Tooltip("Open performance metrics and charts"));
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
    Button advanceMonth = new Button("Advance Month");
    // larger, prettier style
    advanceMonth.setStyle(BUTTON_STYLE + " -fx-font-size:14px; -fx-padding:10 18; -fx-background-color: linear-gradient(" + ACCENT3 + ", " + ACCENT2 + "); -fx-text-fill: white;");
    advanceMonth.setPrefWidth(180);
    advanceMonth.setPrefHeight(44);
        advanceMonth.setOnAction(e -> {
            // possibly trigger a random event (will show modal popup if one occurs)
            RestaurantSimulator.AdvanceResult pre = new RestaurantSimulator.AdvanceResult(simulator.getYear(), simulator.getMonth(), 0.0, simulator.getMonthlyEarnings(), simulator.getRent(), simulator.getTotalMoney(), simulator.getTotalEarnings(), 0, simulator.getSize(), simulator.getRating(), simulator.getEmployeeWage(), 0.0);
            eventManager.maybeTriggerEvent(primaryStage, simulator, pre, logArea);
            // advance the month after events may have modified simulator state
            RestaurantSimulator.AdvanceResult r = simulator.advanceMonth();
            updateYearMonthText(yearMonthLabel, r.year, r.month);
            updateTotalMoneyButton(totalMoneyButton, r.totalMoney);
            monthlyEarningsLabel.setText(String.format("Monthly earnings: $%.2f", r.monthlyEarnings));
            customersLabel.setText(String.format("Customers: %d", r.customers));
            sizeLabel.setText(String.format("Size: %d", r.size));
            ratingLabel.setText(String.format("Rating: %.2f", r.rating));
            printMonthlyLogs(r.year, r.month, r.monthlyEarnings, r.rent, r.employeeWage, simulator.getSuppliesSpendings(), logArea);
            rentLabel.setText(String.format("Rent: $%.2f", r.rent));
            lastSpendingLabel.setText(String.format("Last month wage: $%d | delta: %.2f", r.employeeWage, r.delta));
            // refresh data queues after finalizing so queues record the updated rating
            simulator.updateData();
            // refresh upgrade affordance after monthly changes
            refreshUpgradeButton.run();
            // refresh rating in UI in case event changed it
            ratingLabel.setText(String.format("Rating: %.2f", simulator.getRating()));
            // refresh money and earnings in case event adjusted them
            updateTotalMoneyButton(totalMoneyButton, simulator.getTotalMoney());
            monthlyEarningsLabel.setText(String.format("Monthly earnings: $%.2f", simulator.getMonthlyEarnings()));
        });

        // Set up scene layout and wallpaper background
        BorderPane root = new BorderPane();
        root.setCenter(centralImage);
        try {
            Image wallpaper = new Image(getClass().getResource("/images/wallpaper.png").toExternalForm());
            root.setStyle(String.format("-fx-background-image: url('%s'); -fx-background-size: cover; -fx-background-position: center center;", wallpaper.getUrl()));
        } catch (Exception ex) {
            // fallback: transparent background (no white overlay)
            root.setStyle("-fx-background-color: transparent;");
        }

        Label menuHeader = new Label("Menu");
        menuHeader.setStyle("-fx-font-weight:bold;");
        menuHeader.setUnderline(true);
        menuDisplay.setStyle(MENU_STYLE);

    VBox leftCol = new VBox(10, yearMonthLabel, customizeButton, metricsButton, rentLabel, lastSpendingLabel, ratingLabel, upgradeSizeButton, menuHeader, menuListView);
        leftCol.setAlignment(Pos.TOP_LEFT);
        leftCol.setPadding(new Insets(8));

    Label title = new Label("OrderUp!");
    title.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill: " + ACCENT1 + ";");
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
    howTo.setStyle(BUTTON_STYLE + " -fx-background-color: linear-gradient(" + ACCENT1 + ", " + ACCENT2 + ");");

        VBox centerCol = new VBox(10, title, howTo, centralImage);
        centerCol.setAlignment(Pos.CENTER);
        centerCol.setPadding(new Insets(8));

    // RIGHT column
        VBox rightCol = new VBox(8);
        rightCol.setPadding(new Insets(8));
        rightCol.setAlignment(Pos.TOP_RIGHT);
    totalMoneyButton.setStyle(BUTTON_STYLE + " -fx-background-color: linear-gradient(" + ACCENT3 + ", derive(" + ACCENT3 + ", -10%));");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        rightCol.getChildren().addAll(totalMoneyButton, logArea, advanceMonth);

    BorderPane topBar = new BorderPane();
        topBar.setLeft(leftCol);
        topBar.setCenter(centerCol);
        topBar.setRight(rightCol);
        topBar.setStyle(TOPBAR_STYLE);
        root.setTop(topBar);

    // BOTTOM area: left = spening button, right = advance month
    Button spening = new Button("Spending Info");
    // larger, prettier style
    spening.setStyle(BUTTON_STYLE + " -fx-font-size:13px; -fx-padding:8 14; -fx-background-color: linear-gradient(" + ACCENT1 + ", derive(" + ACCENT1 + ", -10%)); -fx-text-fill: white;");
    spening.setPrefWidth(160);
    spening.setPrefHeight(40);
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

    // Log helper — timestamped, spaced entries. Made public so EventManager can reuse it.
    public static void appendLog(TextArea logArea, String title, String body) {
        if (logArea == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append(body).append("\n");
        sb.append("--------------------------------------------------\n");

        String prev = logArea.getText();
        if (prev == null || prev.isEmpty()) logArea.setText(sb.toString());
        else logArea.setText(prev + "\n" + sb.toString());
        logArea.positionCaret(logArea.getText().length());
    }

    private static void printMonthlyLogs(int year, int month, double monthlyEarnings, double rent, int employeeWage, double supplies, TextArea logArea) {
        String title = String.format("Monthly Summary %d-%02d", year, month);
        String body = String.format("Paid Rent: -$%.2f\nPaid Employee Wages: -$%d\nPaid Supplies: -$%.2f\nMonthly Earnings: +$%.2f", rent, employeeWage, supplies, monthlyEarnings);
        appendLog(logArea, title, body);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
