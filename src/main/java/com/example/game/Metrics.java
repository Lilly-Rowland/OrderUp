package com.example.game;
import java.util.Queue;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class Metrics {
  public Metrics(){
  }

  public static Scene getMetrics(Queue<Integer> recentCustomers, Queue<Double> rating, Queue<Double> earnings, Queue<Double> spendings) {
    int[] recentMonths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

  LineChart<Number, Number> customersChart = createLineChart(0, "Average daily customers", recentMonths, recentCustomers.stream().mapToDouble(Integer::doubleValue).toArray());
  LineChart<Number, Number> earningsChart = createTwoLineChart(1, "Spendings(Red) and Earnings(Orange)", recentMonths, spendings.stream().mapToDouble(Double::doubleValue).toArray(), earnings.stream().mapToDouble(Double::doubleValue).toArray());
  LineChart<Number, Number> ratingChart = createLineChart(2, "Rating", recentMonths, rating.stream().mapToDouble(Double::doubleValue).toArray());

  // apply color scheme: customers=#cc0066, spendings=#ff5050, earnings=#ff6600, rating=#cc0066
  applySeriesColor(customersChart, 0, "#cc0066");
  applySeriesColor(earningsChart, 0, "#ff5050"); // spendings series first
  applySeriesColor(earningsChart, 1, "#ff6600"); // earnings series second
  applySeriesColor(ratingChart, 0, "#cc0066");

    VBox chartContainer = new VBox(20);
    chartContainer.getChildren().add(customersChart);
    chartContainer.getChildren().add(earningsChart);
    chartContainer.getChildren().add(ratingChart);

    ScrollPane scrollPane = new ScrollPane(chartContainer);
    scrollPane.setFitToWidth(true);   // charts fill width
    scrollPane.setPannable(true); 

    Scene scene = new Scene(scrollPane, 900, 600);
    return scene;
  }

  private static LineChart<Number, Number> createTwoLineChart(int graphIndex, String title, int[] recentMonths, double[] yAxisData, double[] yAxisData2){
    NumberAxis xAxis = new NumberAxis(1, 12, 1);
    xAxis.setLabel("Time");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(title);
    
    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle(title);
    chart.setPrefHeight(300);

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    XYChart.Series<Number, Number> series2 = new XYChart.Series<>();

    for (int i = 0; i < recentMonths.length; i++) {
      if(i < yAxisData.length){
        series.getData().add(new XYChart.Data<>(recentMonths[i], yAxisData[i]));
        series2.getData().add(new XYChart.Data<>(recentMonths[i], yAxisData2[i]));
      }
    }

    chart.getData().add(series);
    chart.getData().add(series2);
    chart.setLegendVisible(false);

  return chart;
  }

  private static LineChart<Number, Number> createLineChart(int graphIndex, String title, int[] recentMonths, double[] yAxisData) {
    NumberAxis xAxis = new NumberAxis(1, 12, 1);
    xAxis.setLabel("Time");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(title);
    

    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle(title);
    chart.setPrefHeight(300);

    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    for (int i = 0; i < recentMonths.length; i++) {
      if(i < yAxisData.length){
        series.getData().add(new XYChart.Data<>(recentMonths[i], yAxisData[i]));
      }
    }

    chart.getData().add(series);
    chart.setLegendVisible(false);

    return chart;
  }

  
  private static void applySeriesColor(LineChart<Number, Number> chart, int seriesIndex, String color) {
    if (chart.getData().size() > seriesIndex) {
      XYChart.Series<Number, Number> s = (XYChart.Series<Number, Number>) chart.getData().get(seriesIndex);
      // apply a style to the series nodes after they are attached to scene graph
      s.nodeProperty().addListener((obs, oldN, newN) -> {
        if (newN != null) {
          newN.setStyle(String.format("-fx-stroke: %s; -fx-stroke-width: 2px;", color));
        }
      });
    
      if (s.getNode() != null) {
        s.getNode().setStyle(String.format("-fx-stroke: %s; -fx-stroke-width: 2px;", color));
      }
    }
  }
}
