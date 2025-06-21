package com.midterm.mobiledesignfinalterm.admin.models;

public class CarStatus {
    private int vehicleId;
    private String vehicleName;
    private String brand;
    private String model;
    private String rentedBy;
    private String rentalDate;
    private String returnDate;
    private String status; // RENTED, AVAILABLE, MAINTENANCE

    public CarStatus() {}

    public CarStatus(int vehicleId, String vehicleName, String brand, String model, 
                    String rentedBy, String rentalDate, String returnDate, String status) {
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.brand = brand;
        this.model = model;
        this.rentedBy = rentedBy;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters and Setters
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getRentedBy() { return rentedBy; }
    public void setRentedBy(String rentedBy) { this.rentedBy = rentedBy; }

    public String getRentalDate() { return rentalDate; }
    public void setRentalDate(String rentalDate) { this.rentalDate = rentalDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
