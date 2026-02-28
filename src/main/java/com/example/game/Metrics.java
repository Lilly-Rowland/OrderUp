package com.example.game;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class Metrics {
  public Metrics(){
  }

  public static Scene getMetrics(){
    System.out.println("Metrics updated");
    VBox chartContainer = new VBox(20);
    for(int i = 0; i < 3; i++){
      chartContainer.getChildren().add(createLineChart(i));
    }

    ScrollPane scrollPane = new ScrollPane(chartContainer);
    scrollPane.setFitToWidth(true);   // charts fill width
    scrollPane.setPannable(true); 

    Scene scene = new Scene(scrollPane, 900, 600);
    return scene;
  }

  private static LineChart<Number, Number> createLineChart(int graphIndex){
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Time");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Customers");

    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle("Graph " + (graphIndex + 1));
    chart.setPrefHeight(300);

    int[] time = {0, 1, 2, 3, 4, 5};
    int[] customers = {
            5 + graphIndex,
            8 + graphIndex,
            6 + graphIndex,
            10 + graphIndex,
            12 + graphIndex,
            9 + graphIndex
    };

    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    for (int i = 0; i < time.length; i++) {
        series.getData().add(new XYChart.Data<>(time[i], customers[i]));
    }

    chart.getData().add(series);

    return chart;
  }
}
