package com.example.game;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Loads event definitions from resources and can randomly trigger an event when months advance.
 * Event JSON fields: name, description, incomePercent (e.g. 0.1 = +10%), wealthDelta (absolute), popularityPercent (e.g. 0.05 = +5%), image (path under /images).
 */
public class EventManager {
    private final List<EventData> events = new ArrayList<>();
    private final Random rand = new Random();

    public static class EventData {
        public String name;
        public String description;
        public double incomePercent; // multiplicative change to monthly income (fraction)
        public double wealthDelta; // absolute add/subtract to total money
        public double popularityPercent; // fractional change to popularity (additive factor applied to popularity)
        public String image; // path like /images/foo.png
    }

    public EventManager() {
        // load events.json from classpath (resources folder)
        InputStreamReader r = null;
        try {
            java.io.InputStream is = EventManager.class.getResourceAsStream("/events.json");
            if (is != null) {
                r = new InputStreamReader(is);
                Gson g = new Gson();
                Type t = new TypeToken<List<EventData>>(){}.getType();
                List<EventData> list = g.fromJson(r, t);
                if (list != null) events.addAll(list);
            }
        } catch (Exception ex) {
            System.err.println("EventManager: failed to load events.json: " + ex.getMessage());
        } finally {
            try { if (r != null) r.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Possibly trigger a random event. Probability of any event happening is 30% by default.
     */
    public void maybeTriggerEvent(Stage owner, RestaurantSimulator sim, RestaurantSimulator.AdvanceResult lastAdvance, TextArea logArea) {
        if (events.isEmpty()) return;
        double chance = 0.30; // assumption: 30% chance per month
    double roll = rand.nextDouble();
    if (roll >= chance) return; // trigger only when roll < chance

        EventData e = events.get(rand.nextInt(events.size()));
        // apply effects
        if (e.incomePercent != 0.0) {
            sim.applyMonthlyIncomePercentChange(e.incomePercent);
        }
        if (e.wealthDelta != 0.0) {
            sim.addToTotalMoney(e.wealthDelta);
        }
        if (e.popularityPercent != 0.0) {
            sim.adjustPopularityByPercent(e.popularityPercent);
        }

        // show popup with name/description/image
        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Event: " + e.name);
        Label title = new Label(e.name);
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        Label desc = new Label(e.description);
        desc.setWrapText(true);
        VBox box = new VBox(8, title, desc);
        box.setPadding(new Insets(8));
        if (e.image != null && !e.image.isEmpty()) {
            try {
                Image img = new Image(getClass().getResource(e.image).toExternalForm());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(240);
                iv.setPreserveRatio(true);
                box.getChildren().add(iv);
            } catch (Exception ex) {
                // ignore missing image
            }
        }
        popup.setScene(new Scene(box, 360, 260));
        popup.showAndWait();

        // log event.
        if (logArea != null) {
            String sign = e.wealthDelta >= 0 ? "+" : "";
            String log = String.format("EVENT: %s — %s | income%% %.2f%% | wealth %s%.2f | popularity%% %.2f%%", e.name, e.description, e.incomePercent * 100.0, sign, e.wealthDelta, e.popularityPercent * 100.0);
            String prev = logArea.getText();
            if (prev == null || prev.isEmpty()) logArea.setText(log);
            else logArea.setText(prev + "\n" + log);
            logArea.positionCaret(logArea.getText().length());
        }
    }
}
