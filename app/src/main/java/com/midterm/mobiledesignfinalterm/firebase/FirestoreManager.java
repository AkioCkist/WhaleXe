package com.midterm.mobiledesignfinalterm.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.midterm.mobiledesignfinalterm.BookingCar.BookingData;
import com.midterm.mobiledesignfinalterm.CarDetail.Amenity;
import com.midterm.mobiledesignfinalterm.models.Booking;
import com.midterm.mobiledesignfinalterm.models.FavoriteCar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private final FirebaseFirestore db;

    // Singleton pattern
    private static FirestoreManager instance;

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    /**
     * Interface for favorite cars callback
     */
    public interface FavoriteCarsCallback {
        void onFavoriteCarsLoaded(List<FavoriteCar> favoriteCars);
        void onError(String errorMessage);
    }

    /**
     * Interface for bookings callback
     */
    public interface BookingsCallback {
        void onBookingsLoaded(List<Booking> bookings);
        void onError(String errorMessage);
    }

    /**
     * Interface for toggle favorite callback
     */
    public interface ToggleFavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String errorMessage);
    }

    /**
     * Interface for vehicle status update callback
     */
    public interface VehicleStatusUpdateCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Fetch user's favorite cars from Firestore
     * @param userId the user ID
     * @param callback callback to handle success or error
     */
    public void fetchUserFavoriteCars(String userId, FavoriteCarsCallback callback) {
        Log.d(TAG, "Fetching favorite cars for user: " + userId);

        // Check if the user has favorites
        db.collection("favorites").document(userId)
            .get()
            .addOnSuccessListener(favoritesDocument -> {
                if (favoritesDocument.exists() && favoritesDocument.getData() != null) {
                    Map<String, Object> favorites = favoritesDocument.getData();
                    List<String> vehicleIds = new ArrayList<>();

                    // Extract vehicle IDs where value is true
                    for (Map.Entry<String, Object> entry : favorites.entrySet()) {
                        if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                            vehicleIds.add(entry.getKey());
                        }
                    }

                    if (vehicleIds.isEmpty()) {
                        callback.onFavoriteCarsLoaded(new ArrayList<>());
                        return;
                    }

                    // Fetch vehicle details for each favorite
                    fetchVehiclesDetails(vehicleIds, callback);
                } else {
                    // No favorites found
                    callback.onFavoriteCarsLoaded(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching favorites", e);
                callback.onError("Failed to load favorites: " + e.getMessage());
            });
    }

    /**
     * Helper method to fetch vehicle details for the provided vehicle IDs
     */
    private void fetchVehiclesDetails(List<String> vehicleIds, FavoriteCarsCallback callback) {
        List<FavoriteCar> favoriteCars = new ArrayList<>();
        final int[] completedCount = {0};
        final boolean[] hasError = {false};

        for (String vehicleId : vehicleIds) {
            db.collection("vehicles").document(vehicleId)
                .get()
                .addOnSuccessListener(vehicleDoc -> {
                    if (vehicleDoc.exists()) {
                        try {
                            FavoriteCar car = parseVehicleToFavoriteCar(vehicleDoc);
                            car.setFavorite(true);
                            favoriteCars.add(car);

                            // Fetch amenities
                            fetchAmenitiesForVehicle(car, vehicleDoc);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing vehicle", e);
                        }
                    }

                    // Check if we've processed all vehicles
                    completedCount[0]++;
                    if (completedCount[0] == vehicleIds.size() && !hasError[0]) {
                        callback.onFavoriteCarsLoaded(favoriteCars);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching vehicle: " + vehicleId, e);
                    hasError[0] = true;
                    callback.onError("Failed to load vehicle details: " + e.getMessage());
                });
        }
    }

    /**
     * Helper method to parse a vehicle document to FavoriteCar object
     */
    private FavoriteCar parseVehicleToFavoriteCar(DocumentSnapshot vehicleDoc) {
        FavoriteCar car = new FavoriteCar();

        // Set integer fields
        car.setVehicleId(Integer.parseInt(vehicleDoc.getId()));
        car.setTotalTrips(vehicleDoc.getLong("total_trips").intValue());
        car.setSeats(vehicleDoc.getLong("seats").intValue());
        car.setLessorId(vehicleDoc.getLong("lessor_id").intValue());

        // Set float/double fields
        car.setRating(vehicleDoc.getDouble("rating").floatValue());
        car.setBasePrice(vehicleDoc.getDouble("base_price"));

        // Set string fields
        car.setName(vehicleDoc.getString("name"));
        car.setLocation(vehicleDoc.getString("location"));
        car.setTransmission(vehicleDoc.getString("transmission"));
        car.setFuelType(vehicleDoc.getString("fuel_type"));
        car.setVehicleType(vehicleDoc.getString("vehicle_type"));
        car.setDescription(vehicleDoc.getString("description"));
        car.setStatus(vehicleDoc.getString("status"));

        // Format price for display
        double basePrice = vehicleDoc.getDouble("base_price");
        car.setPriceFormatted(String.format(Locale.US, "%.2f", basePrice));
        car.setPriceDisplay("$" + car.getPriceFormatted() + "/day");

        // Find primary image
        Map<String, Object> images = (Map<String, Object>) vehicleDoc.get("images");
        if (images != null) {
            for (Map.Entry<String, Object> entry : images.entrySet()) {
                Map<String, Object> imageInfo = (Map<String, Object>) entry.getValue();
                if ((Boolean) imageInfo.get("is_primary")) {
                    car.setPrimaryImage((String) imageInfo.get("image_url"));
                    break;
                }
            }
        }

        return car;
    }

    /**
     * Helper method to fetch amenities for a vehicle
     */
    private void fetchAmenitiesForVehicle(FavoriteCar car, DocumentSnapshot vehicleDoc) {
        Map<String, Object> amenitiesMap = (Map<String, Object>) vehicleDoc.get("amenities");
        if (amenitiesMap != null) {
            List<Amenity> amenities = new ArrayList<>();

            for (Map.Entry<String, Object> entry : amenitiesMap.entrySet()) {
                if ((Boolean) entry.getValue()) {
                    String amenityId = entry.getKey();

                    // Fetch amenity details
                    db.collection("vehicle_amenities").document(amenityId)
                        .get()
                        .addOnSuccessListener(amenityDoc -> {
                            if (amenityDoc.exists()) {
                                try {
                                    // Convert amenityId from String to Integer
                                    int amenityIdInt = Integer.parseInt(amenityId);

                                    Amenity amenity = new Amenity(
                                            amenityIdInt,
                                            amenityDoc.getString("amenity_name"),
                                            amenityDoc.getString("amenity_icon"),
                                            amenityDoc.getString("description")
                                    );
                                    amenities.add(amenity);
                                    car.setAmenities(amenities);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing amenity ID to integer: " + amenityId, e);
                                }
                            }
                        });
                }
            }
        }
    }

    /**
     * Fetch user's recent bookings from Firestore
     * @param userId the user ID
     * @param callback callback to handle success or error
     */
    public void fetchUserBookings(String userId, BookingsCallback callback) {
        Log.d(TAG, "Fetching bookings for user: " + userId);

        db.collection("bookings")
            .whereEqualTo("renter_id", Integer.parseInt(userId))
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Booking> bookings = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Booking booking = parseBooking(document);
                    bookings.add(booking);
                }

                // Fetch vehicle details for each booking
                fetchVehicleDetailsForBookings(bookings, callback);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching bookings", e);
                callback.onError("Failed to load bookings: " + e.getMessage());
            });
    }

    /**
     * Parse a booking document to Booking object
     */
    private Booking parseBooking(DocumentSnapshot document) {
        Booking booking = new Booking();

        booking.setId(document.getId());
        booking.setVehicleId(document.getLong("vehicle_id").intValue());
        booking.setRenterId(document.getLong("renter_id").intValue());

        // Parse dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

        try {
            Date pickupDate = dateFormat.parse(document.getString("pickup_date"));
            Date returnDate = dateFormat.parse(document.getString("return_date"));

            booking.setPickupDate(pickupDate);
            booking.setReturnDate(returnDate);

            booking.setPickupTime(document.getString("pickup_time"));
            booking.setReturnTime(document.getString("return_time"));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates", e);
        }

        booking.setPickupLocation(document.getString("pickup_location"));
        booking.setReturnLocation(document.getString("return_location"));
        booking.setTotalPrice(document.getDouble("total_price"));
        booking.setDiscountApplied(document.getDouble("discount_applied"));
        booking.setFinalPrice(document.getDouble("final_price"));
        booking.setStatus(document.getString("status"));

        // Parse timestamps
        try {
            SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

            Date createdAt = timestampFormat.parse(document.getString("created_at"));
            Date updatedAt = timestampFormat.parse(document.getString("updated_at"));

            booking.setCreatedAt(createdAt);
            booking.setUpdatedAt(updatedAt);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing timestamps", e);
        }

        return booking;
    }

    /**
     * Fetch vehicle details for bookings
     */
    private void fetchVehicleDetailsForBookings(List<Booking> bookings, BookingsCallback callback) {
        if (bookings.isEmpty()) {
            callback.onBookingsLoaded(bookings);
            return;
        }

        final int[] completedCount = {0};

        for (Booking booking : bookings) {
            String vehicleId = String.valueOf(booking.getVehicleId());

            db.collection("vehicles").document(vehicleId)
                .get()
                .addOnSuccessListener(vehicleDoc -> {
                    if (vehicleDoc.exists()) {
                        booking.setVehicleName(vehicleDoc.getString("name"));

                        // Get primary image
                        Map<String, Object> images = (Map<String, Object>) vehicleDoc.get("images");
                        if (images != null) {
                            for (Map.Entry<String, Object> entry : images.entrySet()) {
                                Map<String, Object> imageInfo = (Map<String, Object>) entry.getValue();
                                if ((Boolean) imageInfo.get("is_primary")) {
                                    booking.setVehicleImage((String) imageInfo.get("image_url"));
                                    break;
                                }
                            }
                        }
                    }

                    completedCount[0]++;
                    if (completedCount[0] == bookings.size()) {
                        callback.onBookingsLoaded(bookings);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching vehicle for booking", e);
                    completedCount[0]++;
                    if (completedCount[0] == bookings.size()) {
                        callback.onBookingsLoaded(bookings);
                    }
                });
        }
    }

    /**
     * Toggle favorite status for a vehicle
     * @param userId the user ID
     * @param vehicleId the vehicle ID
     * @param isFavorite whether to add (true) or remove (false) from favorites
     * @param callback callback to handle success or error
     */
    public void toggleFavorite(String userId, String vehicleId, boolean isFavorite, ToggleFavoriteCallback callback) {
        Log.d(TAG, "Toggling favorite for user: " + userId + ", vehicle: " + vehicleId + ", isFavorite: " + isFavorite);

        if (userId == null || userId.isEmpty() || vehicleId == null || vehicleId.isEmpty()) {
            callback.onError("Invalid user ID or vehicle ID");
            return;
        }

        DocumentReference userFavoritesRef = db.collection("favorites").document(userId);

        if (isFavorite) {
            // Adding to favorites
            userFavoritesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put(vehicleId, true);

                    if (document != null && document.exists()) {
                        // Document exists, update it
                        userFavoritesRef.update(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Car added to favorites");
                                    callback.onSuccess(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding car to favorites", e);
                                    callback.onError("Failed to add to favorites: " + e.getMessage());
                                });
                    } else {
                        // Document doesn't exist, create it
                        userFavoritesRef.set(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Created favorites document and added car");
                                    callback.onSuccess(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating favorites document", e);
                                    callback.onError("Failed to create favorites: " + e.getMessage());
                                });
                    }
                } else {
                    callback.onError("Failed to check favorites document: " + task.getException().getMessage());
                }
            });
        } else {
            // Removing from favorites
            Map<String, Object> updateData = new HashMap<>();
            updateData.put(vehicleId, FieldValue.delete());

            userFavoritesRef.update(updateData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Car removed from favorites");
                        callback.onSuccess(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing car from favorites", e);
                        callback.onError("Failed to remove from favorites: " + e.getMessage());
                    });
        }
    }

    /**
     * Update vehicle status in Firestore
     * @param vehicleId the vehicle ID
     * @param status the new status (e.g., "rented", "available", "maintenance")
     * @param callback callback to handle success or error
     */
    public void updateVehicleStatus(String vehicleId, String status, VehicleStatusUpdateCallback callback) {
        Log.d(TAG, "Updating status for vehicle: " + vehicleId + " to: " + status);

        if (vehicleId == null || vehicleId.isEmpty() || status == null || status.isEmpty()) {
            callback.onError("Invalid vehicle ID or status");
            return;
        }

        DocumentReference vehicleRef = db.collection("vehicles").document(vehicleId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updated_at", getCurrentTimestamp());

        vehicleRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Vehicle status successfully updated to: " + status);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating vehicle status", e);
                    callback.onError("Failed to update vehicle status: " + e.getMessage());
                });
    }

    /**
     * Get current timestamp in ISO format
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    /**
     * Create a new booking in Firestore
     * @param bookingData the booking data
     * @param callback callback to handle success or error
     */
    public void createBooking(BookingData bookingData, String vehicleId, String userId, VehicleStatusUpdateCallback callback) {
        Log.d(TAG, "Creating new booking in Firestore");

        if (bookingData == null || userId == null || userId.isEmpty() || vehicleId == null || vehicleId.isEmpty()) {
            callback.onError("Invalid booking data, user ID, or vehicle ID");
            return;
        }

        // Create a new document in the "bookings" collection with an auto-generated ID
        DocumentReference newBookingRef = db.collection("bookings").document();

        // Build booking data for Firestore
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("vehicle_id", Integer.parseInt(vehicleId));
        bookingMap.put("renter_id", Integer.parseInt(userId));
        bookingMap.put("pickup_date", bookingData.getPickupDate());
        bookingMap.put("pickup_time", bookingData.getPickupTime());
        bookingMap.put("return_date", bookingData.getDropoffDate());
        bookingMap.put("return_time", bookingData.getDropoffTime());
        bookingMap.put("pickup_location", bookingData.getPickupLocation());
        bookingMap.put("return_location", bookingData.getDropoffLocation());
        bookingMap.put("total_price", bookingData.getTotalAmount());
        bookingMap.put("discount_applied", 0.0); // Default discount
        bookingMap.put("final_price", bookingData.getTotalAmount());
        bookingMap.put("status", "confirmed");
        bookingMap.put("created_at", getCurrentTimestamp());
        bookingMap.put("updated_at", getCurrentTimestamp());

        // Create the booking document in Firestore
        newBookingRef.set(bookingMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Booking created successfully with ID: " + newBookingRef.getId());

                    // Now update the vehicle status to "rented"
                    updateVehicleStatus(vehicleId, "rented", new VehicleStatusUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Vehicle status updated to 'rented'");
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Error updating vehicle status: " + errorMessage);
                            callback.onError(errorMessage);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating booking", e);
                    callback.onError("Failed to create booking: " + e.getMessage());
                });
    }
}
