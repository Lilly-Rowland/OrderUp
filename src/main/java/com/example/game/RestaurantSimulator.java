package com.example.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RestaurantSimulator {

    private int year;
    private int month;
    private double totalMoney;
    private double monthlyEarnings;
    private double totalEarnings;
    private double rent;
    private int num_customers;
    private int size; // seating / capacity
    private double rating; // 0.0 - 5.0, derived from menu quality
    private final List<MenuItem> menu = new ArrayList<>();
    private final Random rand = new Random();

    public RestaurantSimulator(int startYear, int startMonth, double startTotalMoney, double rent) {
        this.year = startYear;
        this.month = startMonth;
        this.totalMoney = startTotalMoney;
        this.rent = rent;
        this.monthlyEarnings = 0.0;
        this.totalEarnings = 0.0;
        this.num_customers = 100; // default fixed number of customers
        this.size = 50; // default capacity
        this.rating = 3.0; // neutral default
        // default sample menu to seed rating
        menu.add(new MenuItem("Sample Dish A", 12.0, 0.6));
        menu.add(new MenuItem("Sample Dish B", 9.5, 0.5));
        recomputeRatingFromMenu();
    }

    public static class MenuItem {
        public final String name;
        public final double price;
        public final double quality; // 0.0 - 1.0

        public MenuItem(String name, double price, double quality) {
            this.name = name;
            this.price = price;
            this.quality = Math.max(0.0, Math.min(1.0, quality));
        }
    }

    private void recomputeRatingFromMenu() {
        if (menu.isEmpty()) {
            rating = 3.0;
            return;
        }
        double sumQuality = 0.0;
        for (MenuItem it : menu) sumQuality += it.quality;
        double avgQuality = sumQuality / menu.size();
        // map avgQuality (0..1) to rating (1..5)
        rating = 1.0 + avgQuality * 4.0;
    }

    public AdvanceResult advanceMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }

        double delta = -50 + rand.nextDouble() * 250; // random expense/income
        totalMoney += delta;

        // compute monthly earnings from customers, size, and rating (menu quality)
        recomputeRatingFromMenu();
        double avgPrice = 10.0;
        if (!menu.isEmpty()) {
            double sum = 0.0;
            for (MenuItem it : menu) sum += it.price;
            avgPrice = sum / menu.size();
        }
        int customersServed = Math.min(num_customers, size);
        double base = customersServed * avgPrice; // naive monthly revenue before modifiers
        double variability = 0.7 + rand.nextDouble() * 0.8; // 0.7 - 1.5
        double earnings = Math.round(base * (rating / 3.0) * variability * 100.0) / 100.0;
        monthlyEarnings = earnings;
        totalEarnings += earnings;
        totalMoney += earnings;

        totalMoney -= rent; // automatic monthly rent deduction

        return new AdvanceResult(year, month, delta, earnings, rent, totalMoney, totalEarnings, num_customers, size, rating);
    }

    // menu management
    public synchronized void setMenu(List<MenuItem> newMenu) {
        menu.clear();
        if (newMenu != null) menu.addAll(newMenu);
        recomputeRatingFromMenu();
    }

    public synchronized List<MenuItem> getMenu() {
        return new ArrayList<>(menu);
    }

    public synchronized void setNumCustomers(int n) { this.num_customers = n; }
    public synchronized int getNumCustomers() { return num_customers; }

    public synchronized void setSize(int s) { this.size = s; }
    public synchronized int getSize() { return size; }

    public synchronized double getRating() { return rating; }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public double getTotalMoney() { return totalMoney; }
    public double getMonthlyEarnings() { return monthlyEarnings; }
    public double getTotalEarnings() { return totalEarnings; }
    public double getRent() { return rent; }

    public static class AdvanceResult {
        public final int year;
        public final int month;
        public final double delta;
        public final double monthlyEarnings;
        public final double rent;
        public final double totalMoney;
        public final double totalEarnings;
        public final int customers;
        public final int size;
        public final double rating;

        public AdvanceResult(int year, int month, double delta, double monthlyEarnings, double rent, double totalMoney, double totalEarnings, int customers, int size, double rating) {
            this.year = year;
            this.month = month;
            this.delta = delta;
            this.monthlyEarnings = monthlyEarnings;
            this.rent = rent;
            this.totalMoney = totalMoney;
            this.totalEarnings = totalEarnings;
            this.customers = customers;
            this.size = size;
            this.rating = rating;
        }

    }
}
