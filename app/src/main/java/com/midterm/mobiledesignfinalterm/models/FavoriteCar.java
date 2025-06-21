package com.midterm.mobiledesignfinalterm.models;

import com.google.gson.annotations.SerializedName;
import com.midterm.mobiledesignfinalterm.CarDetail.Amenity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteCar {
    @SerializedName("id")
    private int vehicleId;

    @SerializedName("name")
    private String name;

    @SerializedName("rating")
    private float rating;

    @SerializedName("trips")
    private int totalTrips;

    @SerializedName("location")
    private String location;

    @SerializedName("transmission")
    private String transmission;

    @SerializedName("seats")
    private int seats;

    @SerializedName("fuel")
    private String fuelType;

    @SerializedName("base_price")
    private double basePrice;

    @SerializedName("price_display")
    private String priceDisplay;

    @SerializedName("price_formatted")
    private String priceFormatted;

    @SerializedName("vehicle_type")
    private String vehicleType;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    @SerializedName("lessor_id")
    private int lessorId;

    @SerializedName("favorite_id")
    private int favoriteId;

    @SerializedName("favorited_at")
    private String favoritedAt;

    @SerializedName("primary_image")
    private String primaryImage;

    @SerializedName("fuel_consumption")
    private String fuelConsumption = "5L/100km"; // Default value

    @SerializedName("amenities")
    private List<Amenity> amenities = new ArrayList<>();

    // Getters and setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTransmission() {
        return transmission != null ? transmission : "N/A";
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

    public String getFuelType() {
        return fuelType != null ? fuelType : "N/A";
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public String getPriceDisplay() {
        return priceDisplay != null ? priceDisplay : "";
    }

    public void setPriceDisplay(String priceDisplay) {
        this.priceDisplay = priceDisplay;
    }

    public String getPriceFormatted() {
        return priceFormatted != null ? priceFormatted : "";
    }

    public void setPriceFormatted(String priceFormatted) {
        this.priceFormatted = priceFormatted;
    }

    public String getVehicleType() {
        return vehicleType != null ? vehicleType : "";
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getLessorId() {
        return lessorId;
    }

    public void setLessorId(int lessorId) {
        this.lessorId = lessorId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getFavoritedAt() {
        return favoritedAt != null ? favoritedAt : "";
    }

    public void setFavoritedAt(String favoritedAt) {
        this.favoritedAt = favoritedAt;
    }

    public String getPrimaryImage() {
        return primaryImage != null ? primaryImage : "";
    }

    public void setPrimaryImage(String primaryImage) {
        this.primaryImage = primaryImage;
    }

    public String getFuelConsumption() {
        return fuelConsumption != null ? fuelConsumption : "5L/100km";
    }

    public void setFuelConsumption(String fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public List<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenities = amenities;
    }

    // Helper methods to format data for display
    public String getFormattedSeats() {
        return seats + " People";
    }

    public String getFormattedConsumption() {
        return getFuelConsumption();
    }
}
