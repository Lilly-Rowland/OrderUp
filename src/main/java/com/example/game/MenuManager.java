package com.example.game;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Simple UI helper to manage a restaurant menu. Opens a popup to add items
 * (name, price, quality) and updates the simulator. Also updates a provided
 * VBox in the main UI to list current menu items and prices.
 */
public class MenuManager {

    private final RestaurantSimulator simulator;
    private final VBox menuDisplay;
    private final ListView<String> menuListView; // optional visual list for main UI

    public MenuManager(RestaurantSimulator simulator, VBox menuDisplay, ListView<String> menuListView) {
        this.simulator = simulator;
        this.menuDisplay = menuDisplay;
        this.menuListView = menuListView;
        refreshDisplay();
    }

    // backward-compatible constructor if only VBox provided
    public MenuManager(RestaurantSimulator simulator, VBox menuDisplay) {
        this(simulator, menuDisplay, null);
    }

    public Button createMenuButton(Stage owner) {
        Button menuButton = new Button("Menu");
        menuButton.setOnAction(e -> openMenuPopup(owner));
        return menuButton;
    }

    private void openMenuPopup(Stage owner) {
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Edit Menu");

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));

    // load menu options from JSON resource
    List<MenuItem> options = loadOptionsFromJson();

    // feedback label for duplicate-add attempts
    Label feedback = new Label();

        Button close = new Button("Close");
        close.setOnAction(ev -> popup.close());

        // show available options as selectable buttons
        VBox optionsBox = new VBox(6);
        optionsBox.setPadding(new Insets(6));
        if (options.isEmpty()) {
            optionsBox.getChildren().add(new Label("(no options available)"));
        } else {
                for (MenuItem opt : options) {
                Button optBtn = new Button(String.format("%s — $%.2f", opt.name, opt.price));
                optBtn.setOnAction(ev -> {
                    // add a copy of the selected option to the simulator menu
                    MenuItem it = new MenuItem(opt.name, opt.price, opt.quality);
                    List<MenuItem> m = simulator.getMenu();
                    boolean exists = false;
                    for (MenuItem ex : m) {
                        if (ex.name.equalsIgnoreCase(it.name)) { exists = true; break; }
                    }
                    if (exists) {
                        feedback.setText("Item already in menu: " + it.name);
                    } else {
                        m.add(it);
                        simulator.setMenu(m);
                        refreshDisplay();
                        feedback.setText("Added: " + it.name);
                    }
                });
                optionsBox.getChildren().add(optBtn);
            }
        }

        ScrollPane optionsScroll = new ScrollPane(optionsBox);
        optionsScroll.setPrefHeight(150);

    root.getChildren().addAll(new Label("Available menu options:"), optionsScroll, feedback, new Label("Current menu:"), menuDisplay, close);

        Scene s = new Scene(root, 520, 360);
        popup.setScene(s);
        popup.showAndWait();
    }

    private void refreshDisplay() {
        menuDisplay.getChildren().clear();
        List<MenuItem> list = simulator.getMenu();
        if (list.isEmpty()) {
            menuDisplay.getChildren().add(new Label("(menu empty)"));
            if (menuListView != null) menuListView.getItems().clear();
            return;
        }

        // update VBox display (with remove buttons)
        for (MenuItem it : list) {
            Label l = new Label(String.format("%s — $%.2f", it.name, it.price));
            Button remove = new Button("Remove");
            remove.setOnAction(ev -> {
                List<MenuItem> m = simulator.getMenu();
                MenuItem toRemove = null;
                for (MenuItem mi : m) {
                    if (mi.name.equalsIgnoreCase(it.name)) { toRemove = mi; break; }
                }
                if (toRemove != null) {
                    m.remove(toRemove);
                    simulator.setMenu(m);
                    refreshDisplay();
                }
            });
            HBox row = new HBox(8, l, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            menuDisplay.getChildren().add(row);
        }

        // update optional ListView for main UI
        if (menuListView != null) {
            menuListView.getItems().clear();
            for (MenuItem it : list) {
                menuListView.getItems().add(String.format("%s — $%.2f", it.name, it.price));
            }
        }
    }

    private List<MenuItem> loadOptionsFromJson() {
        try (InputStreamReader r = new InputStreamReader(getClass().getResourceAsStream("/menu_options.json"))) {
            Gson g = new Gson();
            Type t = new TypeToken<List<MenuItem>>(){}.getType();
            List<MenuItem> list = g.fromJson(r, t);
            return list != null ? list : new ArrayList<>();
        } catch (Exception ex) {
            // resource missing or parse error
            return new ArrayList<>();
        }
    }
}
