package com.example.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class RestaurantSimulator {
    // Metrics to track!!
    private int year;
    private int month;
    private double totalMoney;
    private double monthlyEarnings;
    private double totalEarnings;
    private double monthlySpendings;
    private double suppliesSpendings;
    private double rent;
    private int size;
    private double rating; // 0.0 - 5.0, derived from menu quality
    private final List<MenuItem> menu = new ArrayList<>(); //current menu
    private final Random rand = new Random();
    // persistent modifier applied by events to rating (fractional, e.g., 0.1 = +10%)
    private double ratingModifier = 0.0;
    // Metric Trackings
    private Queue<Integer> recentCustomers = new LinkedList<>();
    private Queue<Double> recentRatings = new LinkedList<>();
    private Queue<Double> recentSpendings = new LinkedList<>();
    private Queue<Double> recentEarings = new LinkedList<>();

    // employee wage: base pay and computed monthly wage
    private final int baseEmployeePay = 10;
    private int employeeWage = baseEmployeePay;

    // cached average menu quality (0.0 - 1.0)
    private double avgMenuQuality = 0.0;
    // last computed customers this month
    private int lastCustomers = 0;

    public RestaurantSimulator(int startYear, int startMonth, double startTotalMoney, double rent) {
        this.year = startYear;
        this.month = startMonth;
        this.totalMoney = startTotalMoney;
        this.rent = rent; // will be overridden by derived rent after size set
        this.monthlyEarnings = 0.0;
        this.totalEarnings = 0.0;
        this.monthlySpendings = 0.0;
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
        monthlySpendings += cost;
        int prevSize = size;
        size = (int)Math.round(size * 1.5);
        // recompute rent from new size
        this.rent = this.size * 50.0;
        SizeLevel prev = sizeLevel;
        sizeLevel = sizeLevel.next();
        return new UpgradeResult(true, cost, size, sizeLevel, String.format("%d",sizeLevel.level()));
    }

    // MenuItem is a top-level class in com.example.game.MenuItem

    private void recomputeRatingFromMenu() {
        if (menu.isEmpty()) {
            rating = 3.0;
            avgMenuQuality = 0.0;
            return;
        }
        double sumQuality = 0.0;
        for (MenuItem it : menu) sumQuality += it.quality;
        double avgQuality = sumQuality / menu.size();
        this.avgMenuQuality = avgQuality;

        // If average quality is below 0.2, floor rating at 1.0
        if (avgQuality < 0.2) {
            rating = 1.0;
        } else {
            // map avgQuality in [0.2 .. 1.0] -> rating [1.0 .. 5.0]
            double t = (avgQuality - 0.2) / (1.0 - 0.2); // 0..1
            rating = 1.0 + t * 4.0;
        }
    }

    public AdvanceResult advanceMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }

        double delta = -50 + rand.nextDouble() * 100; // random expense/income
        totalMoney += delta;

    // compute monthly earnings from menu average price, capacity (size) and effective rating
    recomputeRatingFromMenu(); // rating still derived from menu quality
    // effective rating includes persistent modifier from events
    double effectiveRating = rating * (1.0 + ratingModifier);
    if (effectiveRating < 1.0) effectiveRating = 1.0;
    if (effectiveRating > 5.0) effectiveRating = 5.0;
        // compute average price from the current menu; fallback to 50.0 when menu is empty
        double avgPrice;
        if (!menu.isEmpty()) {
            double sum = 0.0;
            for (MenuItem it : menu) sum += it.price;
            avgPrice = sum / menu.size();
        } else {
            avgPrice = 50.0;
        }
        // compute employee wage based on average menu quality: wage = basePay * (1 + avgMenuQuality)
        this.employeeWage = (int)Math.round(baseEmployeePay * (1.0 + this.avgMenuQuality));

    // determine number of customers this month — influenced by capacity (size) and effective rating
    double t = (effectiveRating - 1.0) / 4.0; // 0..1
    double demandFactor = 0.1 + t * 0.9; // 0.1 .. 1.0
    // compute monthly customers (assume up to size customers per day * 30 days)
    int customers = (int)Math.round(size * demandFactor * 30.0*3);
        if (customers < 0) customers = 0;
        int maxMonthly = size * 30 * 3;
        if (customers > maxMonthly) customers = maxMonthly; // cannot exceed monthly capacity

        // earnings = avgPrice * customers * 3, with small randomness +/-15%
        double variability = 0.85 + rand.nextDouble() * 0.3; // 0.85 - 1.15
        double earnings = Math.round((avgPrice * customers * variability) * 100.0) / 100.0;
        monthlyEarnings += earnings;
        totalEarnings += earnings;
        totalMoney += earnings;

    // subtract employee wage (monthly spending)
    totalMoney -= this.employeeWage;

    // supplies/spoilage spendings grow with menu quality (higher quality uses more/better ingredients)
    // model as a fraction of earnings: factor = 5% .. 20% depending on avgMenuQuality (0.0 -> 0.05, 1.0 -> 0.20)
    double suppliesFactor = 0.05 + (this.avgMenuQuality * 0.15);
    this.suppliesSpendings = Math.round((earnings * suppliesFactor) * 100.0) / 100.0;
    totalMoney -= this.suppliesSpendings;

    totalMoney -= rent; // automatic monthly rent deduction
    monthlySpendings += this.employeeWage + rent + this.suppliesSpendings;

    // record last customers (monthly) for later processing by updateData()
    this.lastCustomers = customers;

        // decay persistent rating modifier towards 0 by 0.1 each month
        if (this.ratingModifier > 0.0) {
            this.ratingModifier = Math.max(0.0, this.ratingModifier - 0.1);
        } else if (this.ratingModifier < 0.0) {
            this.ratingModifier = Math.min(0.0, this.ratingModifier + 0.1);
        }

    return new AdvanceResult(year, month, delta, earnings, rent, totalMoney, totalEarnings, customers, size, effectiveRating, this.employeeWage, this.monthlySpendings);
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

    // next upgrade cost for UI display
    public synchronized double getNextUpgradeCost() {
        return 50000.0 * (0.8 * sizeLevel.level());
    }

    // Apply a percentage change to the next monthly income (e.g., 0.1 => +10%) — this is a one-time immediate effect on monthlyEarnings
    public synchronized void applyMonthlyIncomePercentChange(double pct) {
        // scale monthlyEarnings by (1 + pct) for the purposes of the current bookkeeping.
        this.monthlyEarnings += Math.round((this.monthlyEarnings * (1.0 + pct)) * 100.0) / 100.0;
        this.totalEarnings = Math.round((this.totalEarnings * (1.0 + pct)) * 100.0) / 100.0;
    }

    // Add or subtract an absolute amount to total money (positive or negative)
    public synchronized void addToTotalMoney(double delta) {
        this.totalMoney += delta;
    }

    // Adjust rating by a fractional percent (e.g., 0.05 -> increase rating by 5%) — persistent modifier
    public synchronized void adjustRatingByPercent(double pct) {
        this.ratingModifier += pct;
        // clamp modifier to reasonable range (-0.9 .. +5.0)
        if (this.ratingModifier < -0.9) this.ratingModifier = -0.9;
        if (this.ratingModifier > 5.0) this.ratingModifier = 5.0;
    }

    public synchronized double getRating() {
        double effective = rating * (1.0 + ratingModifier);
        if (effective < 1.0) effective = 1.0;
        if (effective > 5.0) effective = 5.0;
        return effective;
    }

    public synchronized int getEmployeeWage() { return employeeWage; }

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
        public final int employeeWage;
        public final double monthlySpendings;

        public AdvanceResult(int year, int month, double delta, double monthlyEarnings, double rent, double totalMoney, double totalEarnings, int customers, int size, double rating, int employeeWage, double monthlySpendings) {
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
            this.employeeWage = employeeWage;
            this.monthlySpendings = monthlySpendings;
        }

    }

    public Queue<Integer> getCustomerData() {
        return recentCustomers;
    }

    public Queue<Double> getRatingData(){
        return recentRatings;
    }

    public Queue<Double> getRecentEarnings(){
        return recentEarings;
    }

    public Queue<Double> getRecentSpendings(){
        return recentSpendings;
    }

    public void updateEarnings(double earnings){
        monthlyEarnings += earnings;
    }

    public void updateSpending(double spending){
        monthlySpendings += Math.abs(spending);
    }

    public void updateData() {
        int avgDaily = (int)Math.round(this.lastCustomers / 30.0);
        if (recentCustomers.size() < 12) {
            recentCustomers.add(avgDaily);
        } else {
            recentCustomers.remove();
            recentCustomers.add(avgDaily);
        }
        double effectiveRating = getRating();
        if(recentRatings.size() < 12){
            recentRatings.add(effectiveRating);
        }else{
            recentRatings.remove();
            recentRatings.add(effectiveRating);
        }
        if(recentEarings.size() < 12){
            recentEarings.add(monthlyEarnings);
        }else{
            recentEarings.remove();
            recentEarings.add(monthlyEarnings);
        }
        if(recentSpendings.size() < 12){
            recentSpendings.add(monthlySpendings);
        }else{
            recentSpendings.remove();
            recentSpendings.add(monthlySpendings);
        }
        System.out.println(monthlySpendings);
        monthlySpendings = 0.0;
        monthlyEarnings = 0.0;
    }
}
