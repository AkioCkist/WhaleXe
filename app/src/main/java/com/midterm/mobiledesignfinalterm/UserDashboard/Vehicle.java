package com.midterm.mobiledesignfinalterm.UserDashboard;

import java.util.Map;

public class Vehicle {
    private String name;
    private Map<String, Image> images;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Image> getImages() { return images; }
    public void setImages(Map<String, Image> images) { this.images = images; }
}
