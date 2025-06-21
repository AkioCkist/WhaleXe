package com.midterm.mobiledesignfinalterm.models;

import android.content.Context;

import java.io.Serializable;

public class Amenity implements Serializable {
    private String name;
    private String icon;
    private transient int iconResource; // This field won't be serialized

    public Amenity() {
        // Required empty constructor for Firebase
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void initializeIconResource(Context context) {
        if (icon != null && !icon.isEmpty()) {
            String resourceName = icon.toLowerCase().replace(" ", "_").replace("-", "_");
            iconResource = context.getResources().getIdentifier(
                resourceName, "drawable", context.getPackageName());
        }
    }
}
