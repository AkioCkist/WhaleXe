package com.midterm.mobiledesignfinalterm.models;

import com.midterm.mobiledesignfinalterm.CarDetail.Amenity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Car implements Serializable {
    private String id;
    private String name;
    private double rating;
    private int total_trips;
    private String location;
    private String transmission;
    private int seats;
    private String fuel_type;
    private double base_price;
    private String vehicle_type;
    private String description;
    private String status;
    private String primaryImage; // To store the primary image URL
    private boolean isFavorite; // New field for favorite state
    private String fuel_consumption;
    private List<CarImage> images;
    private List<Amenity> amenities;

    public Car() {
        // Required empty public constructor for Firebase
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotal_trips() {
        return total_trips;
    }

    public void setTotal_trips(int total_trips) {
        this.total_trips = total_trips;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getFormattedSeats() {
        return seats + " seats";
    }

    public String getFuelType() {
        return fuel_type;
    }

    public void setFuelType(String fuel_type) {
        this.fuel_type = fuel_type;
    }

    public double getBasePrice() {
        return base_price;
    }

    public void setBasePrice(double base_price) {
        this.base_price = base_price;
    }

    public String getPriceFormatted() {
        return String.format("%,.0f VND", base_price);
    }

    public String getVehicleType() {
        return vehicle_type;
    }

    public void setVehicleType(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrimaryImage() {
        return primaryImage;
    }

    public void setPrimaryImage(String primaryImage) {
        this.primaryImage = primaryImage;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    // You might want to add a consumption field based on the adapter
    private String consumption;

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public String getFormattedConsumption() {
        return consumption + " km/L"; // Assuming consumption is in km/L
    }

    public String getFuel_consumption() {
        return fuel_consumption;
    }

    public void setFuel_consumption(String fuel_consumption) {
        this.fuel_consumption = fuel_consumption;
    }

    public List<CarImage> getImages() {
        return images != null ? images : new ArrayList<>();
    }

    public void setImages(List<CarImage> images) {
        this.images = images;
    }

    public List<Amenity> getAmenities() {
        return amenities != null ? amenities : new ArrayList<>();
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenities = amenities;
    }
}