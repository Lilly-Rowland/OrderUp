package com.example.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class RestaurantSimulator {

    private int year;
    private int month;
    private double totalMoney;
    private double monthlyEarnings;
    private double totalEarnings;
    private double rent;
    private int size; // seating / capacity (also number of customers)
    private double rating; // 0.0 - 5.0, derived from menu quality
    private final List<MenuItem> menu = new ArrayList<>();
    private final Random rand = new Random();
    private double popularity = 0.5; // 0.0 - 1.0
    // Metric Trackings
    private Queue<Integer> recentCustomers = new LinkedList<>();
   /* private Queue<Integer> recentRatings;
    private Queue<Integer> recentEarnings;
    private Queue<Integer> recentSpendings;*/



    public RestaurantSimulator(int startYear, int startMonth, double startTotalMoney, double rent) {
        this.year = startYear;
        this.month = startMonth;
        this.totalMoney = startTotalMoney;
        this.rent = rent; // will be overridden by derived rent after size set
        this.monthlyEarnings = 0.0;
        this.totalEarnings = 0.0;
        this.size = 20; // starting capacity is 10 (also starting customers)
        this.rating = 3.0; // neutral default
        // derive rent from size
        this.rent = this.size * 50.0;
    }

    public enum SizeLevel {
        LEVEL1(1), LEVEL2(2), LEVEL3(3), LEVEL4(4), LEVEL5(5), LEVEL6(6), LEVEL7(7), LEVEL8(8), LEVEL9(9), LEVEL10(10);

        private final int level;
        SizeLevel(int level) { this.level = level; }
        public int level() { return level; }
        public SizeLevel next() {
            int idx = this.ordinal();
            if (idx + 1 < SizeLevel.values().length) return SizeLevel.values()[idx + 1];
            return this;
        }
    }

    private SizeLevel sizeLevel = SizeLevel.LEVEL1;

    public static class UpgradeResult {
        public final boolean success;
        public final double cost;
        public final int newSize;
        public final SizeLevel newLevel;
        public final String message;

        public UpgradeResult(boolean success, double cost, int newSize, SizeLevel newLevel, String message) {
            this.success = success;
            this.cost = cost;
            this.newSize = newSize;
            this.newLevel = newLevel;
            this.message = message;
        }
    }

    // cost = 50,000 * (0.8 * current restaurant size level)
    public synchronized UpgradeResult upgradeSize() {
        double cost = 50000.0 * (0.8 * sizeLevel.level());
        if (totalMoney < cost) {
            return new UpgradeResult(false, cost, size, sizeLevel, "Insufficient funds");
        }
        totalMoney -= cost;
        int prevSize = size;
        size = (int)Math.round(size * 1.5);
    // recompute rent from new size
    this.rent = this.size * 50.0;
        SizeLevel prev = sizeLevel;
        sizeLevel = sizeLevel.next();
        return new UpgradeResult(true, cost, size, sizeLevel, String.format("Upgraded from %s(size=%d) to %s(size=%d)", prev.name(), prevSize, sizeLevel.name(), size));
    }

    // PLACEHOLDER FOR MENU STUFF
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

    // compute monthly earnings from menu average price, capacity (size) and popularity
    recomputeRatingFromMenu(); // rating still derived from menu quality
    // popularity is derived linearly from rating: rating=1 -> 0.1, rating=5 -> 1.0
    popularity = 0.1 + (rating - 1.0) * (0.9 / 4.0);
    if (popularity < 0.1) popularity = 0.1;
    if (popularity > 1.0) popularity = 1.0;
        double avgPrice = 50.0;
        if (!menu.isEmpty()) {
            double sum = 0.0;
            for (MenuItem it : menu) sum += it.price;
            avgPrice = sum / menu.size();
        }
    // earnings = avgPrice * capacity * 3 * popularity, with small randomness +/-15%
    double variability = 0.85 + rand.nextDouble() * 0.3; // 0.85 - 1.15
    double earnings = Math.round((avgPrice * size * 3.0 * popularity * variability) * 100.0) / 100.0;
        monthlyEarnings = earnings;
        totalEarnings += earnings;
        totalMoney += earnings;

        totalMoney -= rent; // automatic monthly rent deduction

    return new AdvanceResult(year, month, delta, earnings, rent, totalMoney, totalEarnings, size, size, rating);
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

    public synchronized void setNumCustomers(int n) { setSize(n); }
    public synchronized int getNumCustomers() { return size; }

    public synchronized void setSize(int s) { this.size = s; this.rent = this.size * 50.0; }
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

    public Queue<Integer> getUpdatedData() {
        return recentCustomers;
    }

    public void updateData() {
        if(recentCustomers.size() < 12){
            recentCustomers.add(size);
        }else{
            recentCustomers.remove();
            recentCustomers.add(size);
        }
        
    }
}
