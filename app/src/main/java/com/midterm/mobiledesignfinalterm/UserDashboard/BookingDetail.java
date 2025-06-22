package com.midterm.mobiledesignfinalterm.UserDashboard;

public class BookingDetail {
    private Booking booking;
    private Vehicle vehicle;

    public BookingDetail(Booking booking, Vehicle vehicle) {
        this.booking = booking;
        this.vehicle = vehicle;
    }

    // Getters
    public Booking getBooking() { return booking; }
    public Vehicle getVehicle() { return vehicle; }
}
