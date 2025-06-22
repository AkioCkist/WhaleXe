package com.midterm.mobiledesignfinalterm.UserDashboard;

import com.google.firebase.firestore.PropertyName;

public class Image {
    @PropertyName("image_url")
    private String imageUrl;
    @PropertyName("is_primary")
    private boolean isPrimary;

    // Getters and Setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
}

