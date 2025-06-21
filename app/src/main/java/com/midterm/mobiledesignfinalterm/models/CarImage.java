package com.midterm.mobiledesignfinalterm.models;

import com.google.gson.annotations.SerializedName;

public class CarImage {
    @SerializedName("id")
    private int id;

    @SerializedName("url")
    private String url;

    @SerializedName("is_primary")
    private boolean isPrimary;

    @SerializedName("display_order")
    private int displayOrder;

    // Constructors
    public CarImage() {
    }

    public CarImage(int id, String url, boolean isPrimary, int displayOrder) {
        this.id = id;
        this.url = url;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
