package com.midterm.mobiledesignfinalterm.BookingCar;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BookingData implements Serializable {
    private String bookingId;
    private String carName;
    private String pickupLocation;
    private String dropoffLocation;
    private String pickupDate;
    private String pickupTime;
    private String dropoffDate;
    private String dropoffTime;
    private String fullName;
    private String email;
    private String phone;
    private String aadharNumber;
    private String panNumber;
    private String paymentMethod;
    private double totalAmount;
    private long bookingTimestamp;
    private String bookingStatus;
    private String formattedBookingDate;

    public BookingData() {
        // Generate unique booking ID with current timestamp
        this.bookingTimestamp = System.currentTimeMillis();
        this.bookingId = generateBookingId();
        this.bookingStatus = "PENDING";
        this.formattedBookingDate = getCurrentFormattedDate();
    }

    // Constructor with car name parameter
    public BookingData(String carName) {
        this();
        this.carName = carName;
    }

    // Generate unique booking ID
    private String generateBookingId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "BK" + timestamp.substring(timestamp.length() - 6) + uuid;
    }

    // Get current formatted date
    private String getCurrentFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(bookingTimestamp));
    }

    // Method to update booking timestamp to current time
    public void updateTimestamp() {
        this.bookingTimestamp = System.currentTimeMillis();
        this.formattedBookingDate = getCurrentFormattedDate();
    }

    // Method to calculate rental duration in hours
    public long getRentalDurationInHours() {
        // This is a simplified calculation - you might want to implement proper date/time parsing
        // For now, returning a default value - implement based on your date format
        return 24; // Default 24 hours
    }

    // Method to validate booking data
    public boolean isValidBookingData() {
        return pickupLocation != null && !pickupLocation.trim().isEmpty() &&
                dropoffLocation != null && !dropoffLocation.trim().isEmpty() &&
                pickupDate != null && !pickupDate.trim().isEmpty() &&
                pickupTime != null && !pickupTime.trim().isEmpty() &&
                dropoffDate != null && !dropoffDate.trim().isEmpty() &&
                dropoffTime != null && !dropoffTime.trim().isEmpty() &&
                fullName != null && !fullName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty();
    }

    // Method to get booking summary
    public String getBookingSummary() {
        return String.format(
                "Booking ID: %s\nCar: %s\nFrom: %s to %s\nPickup: %s %s\nDropoff: %s %s\nCustomer: %s\nAmount: $%.2f",
                bookingId,
                carName != null ? carName : "Not specified",
                pickupLocation, dropoffLocation,
                pickupDate, pickupTime,
                dropoffDate, dropoffTime,
                fullName,
                totalAmount
        );
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getDropoffDate() {
        return dropoffDate;
    }

    public void setDropoffDate(String dropoffDate) {
        this.dropoffDate = dropoffDate;
    }

    public String getDropoffTime() {
        return dropoffTime;
    }

    public void setDropoffTime(String dropoffTime) {
        this.dropoffTime = dropoffTime;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getBookingTimestamp() {
        return bookingTimestamp;
    }

    public void setBookingTimestamp(long bookingTimestamp) {
        this.bookingTimestamp = bookingTimestamp;
        this.formattedBookingDate = getCurrentFormattedDate();
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getFormattedBookingDate() {
        return formattedBookingDate;
    }

    @Override
    public String toString() {
        return "BookingData{" +
                "bookingId='" + bookingId + '\'' +
                ", carName='" + carName + '\'' +
                ", pickupLocation='" + pickupLocation + '\'' +
                ", dropoffLocation='" + dropoffLocation + '\'' +
                ", pickupDate='" + pickupDate + '\'' +
                ", pickupTime='" + pickupTime + '\'' +
                ", dropoffDate='" + dropoffDate + '\'' +
                ", dropoffTime='" + dropoffTime + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", totalAmount=" + totalAmount +
                ", bookingStatus='" + bookingStatus + '\'' +
                ", formattedBookingDate='" + formattedBookingDate + '\'' +
                '}';
    }
}