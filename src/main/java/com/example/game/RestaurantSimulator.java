package com.example.game;

import java.util.Random;

public class RestaurantSimulator {

    private int year;
    private int month;
    private double totalMoney;
    private double monthlyEarnings;
    private double totalEarnings;
    private double rent;
    private final Random rand = new Random();

    public RestaurantSimulator(int startYear, int startMonth, double startTotalMoney, double rent) {
        this.year = startYear;
        this.month = startMonth;
        this.totalMoney = startTotalMoney;
        this.rent = rent;
        this.monthlyEarnings = 0.0;
        this.totalEarnings = 0.0;
    }

    public AdvanceResult advanceMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }

        double delta = -50 + rand.nextDouble() * 250; // random expense/income
        totalMoney += delta;

        double earnings = Math.round(rand.nextDouble() * 2000.0 * 100.0) / 100.0; // 0..2000
        monthlyEarnings = earnings;
        totalEarnings += earnings;
        totalMoney += earnings;

        totalMoney -= rent; // automatic monthly rent deduction

        return new AdvanceResult(year, month, delta, earnings, rent, totalMoney, totalEarnings);
    }

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

        public AdvanceResult(int year, int month, double delta, double monthlyEarnings, double rent, double totalMoney, double totalEarnings) {
            this.year = year;
            this.month = month;
            this.delta = delta;
            this.monthlyEarnings = monthlyEarnings;
            this.rent = rent;
            this.totalMoney = totalMoney;
            this.totalEarnings = totalEarnings;
        }
    }
}
