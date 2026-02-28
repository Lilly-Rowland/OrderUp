package com.example.game;
import java.util.Arrays;
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
    System.out.println(recentCustomers);
    int[] recentMonths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; // Placeholder for actual month data
    //double[] customers = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 70}; // Placeholder for actual customer data
    //double[] rating = {3, 3, 3, 3, 4, 4, 4, 4, 4.5, 4.5, 4.5, 4.5}; // Placeholder for actual rating data
   // double[] earnings = {1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000}; // Placeholder for actual earnings data
   // double[] spendings = {0, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000}; // Placeholder for actual spendings data

    LineChart<Number, Number> customersChart = createLineChart(0, "Customers", recentMonths, recentCustomers.stream().mapToDouble(Integer::doubleValue).toArray());
    LineChart<Number, Number> earningsChart = createTwoLineChart(1, "Spendings(Red) and Earnings(Orange)", recentMonths, spendings.stream().mapToDouble(Double::doubleValue).toArray(), earnings.stream().mapToDouble(Double::doubleValue).toArray());
    LineChart<Number, Number> ratingChart = createLineChart(2, "Rating", recentMonths, rating.stream().mapToDouble(Double::doubleValue).toArray());

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
    System.out.println(Arrays.toString(yAxisData));
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
}
