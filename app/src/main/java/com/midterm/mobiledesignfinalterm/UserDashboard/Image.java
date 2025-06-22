package com.midterm.mobiledesignfinalterm.UserDashboard;

import com.google.firebase.firestore.Exclude;

/**
 * Corrected Image class using public fields for reliable Firestore deserialization.
 */
public class Image {

    // --- Public fields that EXACTLY match the names in your Firestore JSON ---
    public String image_url;
    public boolean is_primary;

    // Firestore requires a public, no-argument constructor.
    public Image() {}

    // --- Convenience Getters ---
    // We add @Exclude so Firestore doesn't try to treat these as properties again.
    // These methods allow the rest of your code (in the Fragment and Adapter)
    // to remain completely unchanged.

    @Exclude
    public String getImageUrl() {
        return image_url;
    }

    @Exclude
    public boolean isPrimary() {
        return is_primary;
    }
}