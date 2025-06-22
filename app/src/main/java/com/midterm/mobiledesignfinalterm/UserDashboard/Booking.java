package com.midterm.mobiledesignfinalterm.UserDashboard;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

/**
 * Simplified Booking class that supports both old and new booking formats
 */
public class Booking {
    // Fields used in old format
    private long renter_id;
    private long vehicle_id;
    private String pickup_date;
    private String return_date;
    private String status;
    private double total_price;

    // Fields used in new format
    private String userId;
    private String carId;
    private String pickupDate;
    private String dropoffDate;
    private String bookingId;
    private String totalAmount;
    private Timestamp createdAt;

    // Constructor
    public Booking() {
        // Required empty constructor for Firestore
    }

    // Getters and setters for old format
    public long getRenter_id() { return renter_id; }
    public void setRenter_id(long renter_id) { this.renter_id = renter_id; }

    public long getVehicle_id() { return vehicle_id; }
    public void setVehicle_id(long vehicle_id) { this.vehicle_id = vehicle_id; }

    public String getPickup_date() { return pickup_date; }
    public void setPickup_date(String pickup_date) { this.pickup_date = pickup_date; }

    public String getReturn_date() { return return_date; }
    public void setReturn_date(String return_date) { this.return_date = return_date; }

    public String getStatus() {
        if (status != null) return status;
        return "unknown";
    }
    public void setStatus(String status) { this.status = status; }

    public double getTotal_price() { return total_price; }
    public void setTotal_price(double total_price) { this.total_price = total_price; }

    // Getters and setters for new format
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }

    public String getPickupDate() { return pickupDate; }
    public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }

    public String getDropoffDate() { return dropoffDate; }
    public void setDropoffDate(String dropoffDate) { this.dropoffDate = dropoffDate; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Booking{" +
                "vehicle_id=" + vehicle_id +
                ", carId='" + carId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
