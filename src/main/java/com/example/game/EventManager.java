package com.example.game;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    public double popularityPercent; // fractional change to rating modifier (additive factor applied to rating)
    public boolean optional = false; // if true, user can accept/decline the event
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

        // For optional events we prompt the user to accept/decline. If accepted, apply effects.
        boolean applied = true;
        if (e.optional) {
            Stage choice = new Stage();
            choice.initOwner(owner);
            choice.initModality(Modality.APPLICATION_MODAL);
            choice.setTitle("Optional Event: " + e.name);
            Label title = new Label(e.name);
            title.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
            Label desc = new Label(e.description + "\n\nAccept this event? (Decline will skip it)");
            desc.setWrapText(true);
            Button yes = new Button("Accept");
            Button no = new Button("Decline");
            HBox actions = new HBox(8, yes, no);
            actions.setAlignment(Pos.CENTER);
            VBox box = new VBox(8, title, desc, actions);
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
            Scene scene = new Scene(box, 420, 300);
            choice.setScene(scene);

            final boolean[] userAccepted = {false};
            yes.setOnAction(ae -> { userAccepted[0] = true; choice.close(); });
            no.setOnAction(ae -> { userAccepted[0] = false; choice.close(); });

            choice.showAndWait();
            applied = userAccepted[0];
        }

        if (applied) {
            // apply effects
            if (e.incomePercent != 0.0) {
                sim.applyMonthlyIncomePercentChange(e.incomePercent);
            }
            if (e.wealthDelta != 0.0) {
                sim.addToTotalMoney(e.wealthDelta);
            }
            if (e.popularityPercent != 0.0) {
                sim.adjustRatingByPercent(e.popularityPercent);
            }

            // show popup with name/description/image confirming the event
            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Event: " + e.name);
            Label title2 = new Label(e.name);
            title2.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
            Label desc2 = new Label(e.description);
            desc2.setWrapText(true);
            VBox box2 = new VBox(8, title2, desc2);
            box2.setPadding(new Insets(8));
            if (e.image != null && !e.image.isEmpty()) {
                try {
                    Image img = new Image(getClass().getResource(e.image).toExternalForm());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(240);
                    iv.setPreserveRatio(true);
                    box2.getChildren().add(iv);
                } catch (Exception ex) {
                    // ignore missing image
                }
            }
            popup.setScene(new Scene(box2, 360, 260));
            popup.showAndWait();

            // log event using Main.appendLog for nicer formatting
            if (logArea != null) {
                String sign = e.wealthDelta >= 0 ? "+" : "";
                String body = String.format("%s\nIncome%%: %.2f%% | Wealth: %s%.2f | Rating%%: %.2f%%", e.description, e.incomePercent * 100.0, sign, e.wealthDelta, e.popularityPercent * 100.0);
                Main.appendLog(logArea, "Event: " + e.name, body);
            }
        } else {
            // user declined optional event — log that it was skipped
            if (logArea != null) {
                Main.appendLog(logArea, "Event declined", String.format("%s was declined by user.", e.name));
            }
        }
        if(e.wealthDelta >= 0){
            sim.updateEarnings(e.wealthDelta);
        } else {
            sim.updateSpending(e.wealthDelta);
        }
    }
}
