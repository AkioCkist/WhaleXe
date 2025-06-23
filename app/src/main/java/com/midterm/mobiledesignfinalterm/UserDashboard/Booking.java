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

    // Document ID
    private String id;

    // Added fields for vehicle details
    private String vehicleName;
    private String vehicleImage;

    // Fields for old and new location/time
    @com.google.firebase.firestore.PropertyName("pickup_location")
    private String oldPickupLocation;
    @com.google.firebase.firestore.PropertyName("return_location")
    private String oldDropoffLocation;
    @com.google.firebase.firestore.PropertyName("pickupLocation")
    private String newPickupLocation;
    @com.google.firebase.firestore.PropertyName("dropoffLocation")
    private String newDropoffLocation;
    @com.google.firebase.firestore.PropertyName("pickup_time")
    private String oldPickupTime;
    @com.google.firebase.firestore.PropertyName("return_time")
    private String oldDropoffTime;
    @com.google.firebase.firestore.PropertyName("pickupTime")
    private String newPickupTime;
    @com.google.firebase.firestore.PropertyName("dropoffTime")
    private String newDropoffTime;

    // Constructor
    public Booking() {
        // Required empty constructor for Firestore
    }

    // Getters and setters for document ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Setters for Firestore mapping
    @com.google.firebase.firestore.PropertyName("pickup_location")
    public void setOldPickupLocation(String val) { this.oldPickupLocation = val; }
    @com.google.firebase.firestore.PropertyName("return_location")
    public void setOldDropoffLocation(String val) { this.oldDropoffLocation = val; }
    @com.google.firebase.firestore.PropertyName("pickupLocation")
    public void setNewPickupLocation(String val) { this.newPickupLocation = val; }
    @com.google.firebase.firestore.PropertyName("dropoffLocation")
    public void setNewDropoffLocation(String val) { this.newDropoffLocation = val; }
    @com.google.firebase.firestore.PropertyName("pickup_time")
    public void setOldPickupTime(String val) { this.oldPickupTime = val; }
    @com.google.firebase.firestore.PropertyName("return_time")
    public void setOldDropoffTime(String val) { this.oldDropoffTime = val; }
    @com.google.firebase.firestore.PropertyName("pickupTime")
    public void setNewPickupTime(String val) { this.newPickupTime = val; }
    @com.google.firebase.firestore.PropertyName("dropoffTime")
    public void setNewDropoffTime(String val) { this.newDropoffTime = val; }
    // Inside your Booking.java class

    public void setCreatedAt(Object createdAt) {
        if (createdAt instanceof Timestamp) {
            this.createdAt = (Timestamp) createdAt;
        } else if (createdAt instanceof java.util.Map) {
            // Handle the case where it's a map from a server timestamp
            // This is a simplified example; you might need to adjust based on the exact map structure
            // For now, we'll leave it null and let the getter handle it,
            // or you can parse the map if you know its structure.
            this.createdAt = null;
        }
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Unified getters for use in UI
    public String getPickupLocation() {
        if (newPickupLocation != null && !newPickupLocation.isEmpty()) return newPickupLocation;
        if (oldPickupLocation != null && !oldPickupLocation.isEmpty()) return oldPickupLocation;
        return "";
    }
    public String getDropoffLocation() {
        if (newDropoffLocation != null && !newDropoffLocation.isEmpty()) return newDropoffLocation;
        if (oldDropoffLocation != null && !oldDropoffLocation.isEmpty()) return oldDropoffLocation;
        return "";
    }
    public String getPickupTime() {
        if (newPickupTime != null && !newPickupTime.isEmpty()) return newPickupTime;
        if (oldPickupTime != null && !oldPickupTime.isEmpty()) return oldPickupTime;
        return "";
    }
    public String getDropoffTime() {
        if (newDropoffTime != null && !newDropoffTime.isEmpty()) return newDropoffTime;
        if (oldDropoffTime != null && !oldDropoffTime.isEmpty()) return oldDropoffTime;
        return "";
    }

    // New methods for vehicle image and name
    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    /**
     * Gets the vehicle ID (works for both old and new format)
     * @return String representation of the vehicle ID
     */
    public String getVehicleId() {
        if (carId != null && !carId.isEmpty()) {
            return carId;
        }
        return String.valueOf(vehicle_id);
    }

    /**
     * Format the final price as currency
     */
    public String getFormattedFinalPrice() {
        if (totalAmount != null && !totalAmount.isEmpty()) {
            try {
                double amount = Double.parseDouble(totalAmount);
                return String.format("₫%.0f", amount);
            } catch (NumberFormatException e) {
                return totalAmount;
            }
        }
        return String.format("₫%.0f", total_price);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "vehicle_id=" + vehicle_id +
                ", carId='" + carId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
