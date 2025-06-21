package com.midterm.mobiledesignfinalterm.admin.models;

public class AdminStats {
    private int totalBookings;
    private int totalCars;
    private int todayBookings;
    private int weekBookings;
    private int monthBookings;
    private double cancelRate;
    private double successRate;

    public AdminStats() {}

    public AdminStats(int totalBookings, int totalCars, int todayBookings, int weekBookings, 
                     int monthBookings, double cancelRate, double successRate) {
        this.totalBookings = totalBookings;
        this.totalCars = totalCars;
        this.todayBookings = todayBookings;
        this.weekBookings = weekBookings;
        this.monthBookings = monthBookings;
        this.cancelRate = cancelRate;
        this.successRate = successRate;
    }

    // Getters and Setters
    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }

    public int getTotalCars() { return totalCars; }
    public void setTotalCars(int totalCars) { this.totalCars = totalCars; }

    public int getTodayBookings() { return todayBookings; }
    public void setTodayBookings(int todayBookings) { this.todayBookings = todayBookings; }

    public int getWeekBookings() { return weekBookings; }
    public void setWeekBookings(int weekBookings) { this.weekBookings = weekBookings; }

    public int getMonthBookings() { return monthBookings; }
    public void setMonthBookings(int monthBookings) { this.monthBookings = monthBookings; }

    public double getCancelRate() { return cancelRate; }
    public void setCancelRate(double cancelRate) { this.cancelRate = cancelRate; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
}
