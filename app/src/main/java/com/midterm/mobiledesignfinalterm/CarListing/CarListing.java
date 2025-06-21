package com.midterm.mobiledesignfinalterm.CarListing; // Replace with your actual package

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.midterm.mobiledesignfinalterm.CarListing.CarListingAdapter;
import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.models.Car;
import com.midterm.mobiledesignfinalterm.CarDetail.Amenity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarListing extends AppCompatActivity implements CarListingAdapter.OnCarItemClickListener {

    private static final String TAG = "CarListingActivity";
    private RecyclerView recyclerView;
    private CarListingAdapter carListingAdapter;
    private List<Car> carList;
    private List<Car> filteredCarList; // Added filteredCarList variable
    private FirebaseFirestore db;

    private String currentBrandFilter = null;
    private String currentVehicleTypeFilter = null;
    private String currentFuelTypeFilter = null;
    private Integer currentSeatsFilter = null;

    private String pickupLocation = null;
    private String returnLocation = null;
    private Calendar pickupDateTime = null;
    private Calendar returnDateTime = null;

    // Add user information variables
    private String userId;
    private String userName;
    private String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_listing);

        recyclerView = findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carList = new ArrayList<>();
        filteredCarList = new ArrayList<>(); // Initialize filteredCarList
        carListingAdapter = new CarListingAdapter(filteredCarList, this); // Use filteredCarList for the adapter
        recyclerView.setAdapter(carListingAdapter);

        db = FirebaseFirestore.getInstance();

        // Retrieve date/time/location data from intent
        getDataFromIntent();

        fetchCarData();
        setupUIControls();  // Add new method to setup click listeners
    }

    /**
     * Retrieves data passed from Homepage in the intent
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Get location data
            pickupLocation = intent.getStringExtra("pickup_location");
            returnLocation = intent.getStringExtra("dropoff_location");

            // Get date and time data
            String pickupDate = intent.getStringExtra("pickup_date");
            String pickupTime = intent.getStringExtra("pickup_time");
            String returnDate = intent.getStringExtra("dropoff_date");
            String returnTime = intent.getStringExtra("dropoff_time");

            // Parse date and time into Calendar objects if they exist
            if (pickupDate != null && pickupTime != null) {
                pickupDateTime = Calendar.getInstance();
                try {
                    // Parse date in format "dd MMMM yyyy"
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    pickupDateTime.setTime(dateFormat.parse(pickupDate));

                    // Parse and set time in format "HH:mm"
                    String[] timeParts = pickupTime.split(":");
                    if (timeParts.length == 2) {
                        pickupDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                        pickupDateTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing pickup date/time", e);
                    pickupDateTime = Calendar.getInstance(); // Use current time as fallback
                }
            }

            if (returnDate != null && returnTime != null) {
                returnDateTime = Calendar.getInstance();
                try {
                    // Parse date in format "dd MMMM yyyy"
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    returnDateTime.setTime(dateFormat.parse(returnDate));

                    // Parse and set time in format "HH:mm"
                    String[] timeParts = returnTime.split(":");
                    if (timeParts.length == 2) {
                        returnDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                        returnDateTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing return date/time", e);

                    // Use current time + 2 hours as fallback
                    returnDateTime = Calendar.getInstance();
                    returnDateTime.add(Calendar.HOUR_OF_DAY, 2);
                }
            }

            // Update the buttonDateTime text if locations and dates are available
            updateDateTimeLocationButton();

            // Get user information from intent
            userId = intent.getStringExtra("user_id");
            userName = intent.getStringExtra("user_name");
            userPhone = intent.getStringExtra("user_phone");
        }
    }

    /**
     * Updates the text on the buttonDateTime with the pickup/return information
     */
    private void updateDateTimeLocationButton() {
        Button buttonDateTime = findViewById(R.id.buttonDateTime);

        if (pickupLocation != null && returnLocation != null &&
            pickupDateTime != null && returnDateTime != null) {

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String buttonText = pickupLocation + " --- " + returnLocation + "\n" +
                dateTimeFormat.format(pickupDateTime.getTime()) + " --- " +
                dateTimeFormat.format(returnDateTime.getTime());

            buttonDateTime.setText(buttonText);
        }
    }

    // New method to handle UI controls and click listeners
    private void setupUIControls() {
        // Setup search functionality
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(editTextSearch.getText().toString());
                return true;
            }
            return false;
        });

        // Setup back button
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Setup date/time/location button
        Button buttonDateTime = findViewById(R.id.buttonDateTime);
        buttonDateTime.setOnClickListener(v -> showDateTimeLocationDialog());

        // Setup filter button
        ImageButton filterButton = findViewById(R.id.btn_Filter);
        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void fetchCarData() {
        db.collection("vehicles") // Collection name for vehicles
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            carList.clear(); // Clear existing data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Instead of direct conversion, manually create and populate the Car object
                                Car car = new Car();
                                car.setId(document.getId());
                                car.setName(document.getString("name"));
                                car.setRating(document.getDouble("rating") != null ? document.getDouble("rating") : 0.0);
                                car.setTotal_trips(document.getLong("total_trips") != null ? document.getLong("total_trips").intValue() : 0);
                                car.setLocation(document.getString("location"));
                                car.setTransmission(document.getString("transmission"));
                                car.setSeats(document.getLong("seats") != null ? document.getLong("seats").intValue() : 0);
                                car.setFuelType(document.getString("fuel_type"));
                                car.setBasePrice(document.getDouble("base_price") != null ? document.getDouble("base_price") : 0.0);
                                car.setVehicleType(document.getString("vehicle_type"));
                                car.setDescription(document.getString("description"));
                                car.setStatus(document.getString("status"));
                                car.setFuel_consumption(document.getString("fuel_consumption"));

                                // Skip vehicles with "rented" or "maintenanced" status
                                String status = car.getStatus();
                                if (status != null && (status.equalsIgnoreCase("rented") || status.equalsIgnoreCase("maintenanced"))) {
                                    Log.d(TAG, "Skipping vehicle with status: " + status + ", Name: " + car.getName());
                                    continue; // Skip this car and move to the next one
                                }

                                // Get primary image URL
                                Map<String, Object> images = (Map<String, Object>) document.get("images");
                                if (images != null) {
                                    for (Map.Entry<String, Object> entry : images.entrySet()) {
                                        Map<String, Object> imageData = (Map<String, Object>) entry.getValue();
                                        if (imageData != null && Boolean.TRUE.equals(imageData.get("is_primary"))) {
                                            String relativePath = (String) imageData.get("image_url");

                                            if (relativePath != null) {
                                                // Remove the leading slash if it exists
                                                if (relativePath.startsWith("/")) {
                                                    relativePath = relativePath.substring(1);
                                                }

                                                // Log the relative path for debugging
                                                Log.d(TAG, "Image relative path: " + relativePath);

                                                // Set the path for asset loading
                                                car.setPrimaryImage("file:///android_asset/" + relativePath);
                                                break;
                                            }
                                        }
                                    }
                                }

                                // Set consumption if needed
                                if (car.getConsumption() == null) {
                                    car.setConsumption("10 L/100km"); // Default value
                                }

                                // Convert amenities from HashMap to List - skip automatic deserialization
                                Map<String, Object> amenitiesMap = (Map<String, Object>) document.get("amenities");
                                if (amenitiesMap != null) {
                                    List<Amenity> amenitiesList = new ArrayList<>();
                                    for (Map.Entry<String, Object> entry : amenitiesMap.entrySet()) {
                                        Object amenityValue = entry.getValue();
                                        // Check if the value is actually a Map before casting
                                        if (amenityValue instanceof Map) {
                                            Map<String, Object> amenityData = (Map<String, Object>) amenityValue;
                                            if (amenityData != null) {
                                                String name = (String) amenityData.get("name");
                                                String icon = (String) amenityData.get("icon");
                                                String description = (String) amenityData.get("description");
                                                int id = entry.getKey().hashCode(); // Generate an ID from the key

                                                Amenity amenity = new Amenity(id, name, icon, description);
                                                amenitiesList.add(amenity);
                                            }
                                        } else {
                                            // Handle the case where amenity value is a boolean or other non-map type
                                            Log.w(TAG, "Amenity value for key " + entry.getKey() + " is not a Map, it's a " +
                                                    (amenityValue != null ? amenityValue.getClass().getSimpleName() : "null"));
                                        }
                                    }
                                    car.setAmenities(amenitiesList);
                                }

                                carList.add(car);
                            }

                            // Apply location filter after loading data
                            handleFilterByLocation();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(CarListing.this, "Error loading cars.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            fetchCarData(); // Reset to all data if search is empty
            return;
        }

        List<Car> filteredList = new ArrayList<>();
        for (Car car : carList) {
            if (car.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (car.getLocation() != null && car.getLocation().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(car);
            }
        }

        carListingAdapter = new CarListingAdapter(filteredList, this);
        recyclerView.setAdapter(carListingAdapter);
        Toast.makeText(this, "Found " + filteredList.size() + " cars", Toast.LENGTH_SHORT).show();
    }

    private void showDateTimeLocationDialog() {
        DateTimeLocationDialog dialog = new DateTimeLocationDialog(this);

        // Set initial values if they exist
        dialog.setInitialValues(
                pickupLocation,
                returnLocation,
                pickupDateTime,
                returnDateTime
        );

        // Set the listener to handle selected date/time/location
        dialog.setListener((pickupLoc, returnLoc, pickupTime, returnTime) -> {
            // Update current values
            pickupLocation = pickupLoc;
            returnLocation = returnLoc;
            pickupDateTime = pickupTime;
            returnDateTime = returnTime;

            // Update the button text to show the selected date/time/location
            Button buttonDateTime = findViewById(R.id.buttonDateTime);

            // Format: "Pickup Location --- Return Location\nPickup Time --- Return Time"
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String buttonText = pickupLoc + " --- " + returnLoc + "\n" +
                    dateTimeFormat.format(pickupTime.getTime()) + " --- " +
                    dateTimeFormat.format(returnTime.getTime());

            buttonDateTime.setText(buttonText);

            // Apply location filtering based on the selected pickup location
            handleFilterByLocation();

            Toast.makeText(this, "Đã cập nhật thời gian và địa điểm", Toast.LENGTH_SHORT).show();
        });

        // Show the dialog
        dialog.show();
    }

    private void showFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(this);

        // Set the initial filter values if they exist
        filterDialog.setInitialFilters(
                currentBrandFilter,
                currentVehicleTypeFilter,
                currentFuelTypeFilter,
                currentSeatsFilter
        );

        // Set the listener to handle filter application
        filterDialog.setListener((brand, vehicleType, fuelType, seats) -> {
            // Update current filters
            currentBrandFilter = brand;
            currentVehicleTypeFilter = vehicleType;
            currentFuelTypeFilter = fuelType;
            currentSeatsFilter = seats;

            // Apply filters to car list
            applyFilters();
        });

        // Show the dialog
        filterDialog.show();
    }

    private void applyFilters() {
        // If no filters are active, show all cars
        if (currentBrandFilter == null &&
                currentVehicleTypeFilter == null &&
                currentFuelTypeFilter == null &&
                currentSeatsFilter == null) {

            carListingAdapter = new CarListingAdapter(carList, this);
            recyclerView.setAdapter(carListingAdapter);
            updateCarCount(carList.size());
            return;
        }

        // Apply filters
        List<Car> filteredList = new ArrayList<>();
        for (Car car : carList) {
            boolean match = true;

            // Brand filter
            if (currentBrandFilter != null &&
                    (car.getName() == null || !car.getName().contains(currentBrandFilter))) {
                match = false;
            }

            // Vehicle type filter
            if (currentVehicleTypeFilter != null &&
                    (car.getVehicleType() == null || !car.getVehicleType().equalsIgnoreCase(currentVehicleTypeFilter))) {
                match = false;
            }

            // Fuel type filter
            if (currentFuelTypeFilter != null &&
                    (car.getFuelType() == null || !car.getFuelType().equalsIgnoreCase(currentFuelTypeFilter))) {
                match = false;
            }

            // Seats filter
            if (currentSeatsFilter != null &&
                    car.getSeats() != currentSeatsFilter) {
                match = false;
            }

            if (match) {
                filteredList.add(car);
            }
        }

        carListingAdapter = new CarListingAdapter(filteredList, this);
        recyclerView.setAdapter(carListingAdapter);
        updateCarCount(filteredList.size());
    }

    private void updateCarCount(int count) {
        TextView textViewCarCount = findViewById(R.id.textViewCarCount);
        textViewCarCount.setText("Found " + count + " cars");
    }

    @Override
    public void onRentalClick(Car car) {
        // Navigate to CarDetailActivity with the selected car data
        Intent intent = new Intent(this, com.midterm.mobiledesignfinalterm.CarDetail.CarDetailActivity.class);

        // Pass car basic information
        intent.putExtra("car_id", car.getId());

        // Pass the actual user information received from Homepage
        intent.putExtra("user_id", userId); // Use the actual userId from intent
        intent.putExtra("user_name", userName); // Use the actual userName from intent
        intent.putExtra("user_phone", userPhone); // Use the actual userPhone from intent

        // Pass booking date/time/location if available
        if (pickupLocation != null && returnLocation != null &&
            pickupDateTime != null && returnDateTime != null) {

            // Format date and time for passing to detail activity
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            intent.putExtra("pickup_time", timeFormat.format(pickupDateTime.getTime()));
            intent.putExtra("pickup_date", dateFormat.format(pickupDateTime.getTime()));
            intent.putExtra("dropoff_time", timeFormat.format(returnDateTime.getTime()));
            intent.putExtra("dropoff_date", dateFormat.format(returnDateTime.getTime()));
            intent.putExtra("pickup_location", pickupLocation);
            intent.putExtra("dropoff_location", returnLocation);
        }

        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Car car, int position) {
        // Toggle favorite state locally in the UI
        boolean newFavoriteState = !car.isFavorite();
        car.setFavorite(newFavoriteState);
        carListingAdapter.notifyItemChanged(position); // Update just this item

        // Use the actual userId from intent extras instead of hardcoded value
        if (userId != null && !userId.isEmpty()) {
            // Reference to the user's favorites document
            DocumentReference userFavoritesRef = db.collection("favorites").document(userId);

            // Vehicle ID to be toggled
            String vehicleId = String.valueOf(car.getId());

            if (newFavoriteState) {
                // Check if document exists first, then update or set accordingly
                userFavoritesRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put(vehicleId, true);

                        if (document != null && document.exists()) {
                            // Document exists, update it
                            userFavoritesRef.update(updateData)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Car added to favorites"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error adding car to favorites", e));
                        } else {
                            // Document doesn't exist, create it
                            userFavoritesRef.set(updateData)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Created favorites document and added car"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error creating favorites document", e));
                        }
                    }
                });
            } else {
                // Remove from favorites - use FieldValue.delete() to remove the field
                Map<String, Object> updateData = new HashMap<>();
                updateData.put(vehicleId, FieldValue.delete());

                userFavoritesRef.update(updateData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Car removed from favorites"))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error removing car from favorites", e);
                            // If the error is because the document doesn't exist, we can ignore it
                        });
            }

            Toast.makeText(this, car.getName() + (newFavoriteState ? " added to" : " removed from") + " favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Handle the case where userId is not available
            Toast.makeText(this, "Cannot update favorites: User ID not available", Toast.LENGTH_SHORT).show();
            // Revert UI change since we can't update the database
            car.setFavorite(!newFavoriteState);
            carListingAdapter.notifyItemChanged(position);
        }
    }

    private void handleFilterByLocation() {
        Log.d("CarListing", "DEBUG: Filtering by location: '" + pickupLocation + "'");

        // Clear previous filtered list
        filteredCarList.clear();

        // Show all cars if pickup location is not specified or is general
        if (pickupLocation == null || pickupLocation.isEmpty() ||
            "All Locations".equalsIgnoreCase(pickupLocation) ||
            "Current Location".equalsIgnoreCase(pickupLocation)) {

            filteredCarList.addAll(carList);
            Log.d("CarListing", "Showing all cars because location is empty, 'All Locations', or 'Current Location'");
        } else {
            // Otherwise, filter by known city headers
            for (Car car : carList) {
                String carLocation = car.getLocation();
                if (carLocation != null && extractCityFromHeader(carLocation).equalsIgnoreCase(pickupLocation)) {
                    filteredCarList.add(car);
                    Log.d("CarListing", "Added car with location: " + carLocation);
                }
            }
        }

        // Update RecyclerView and car count
        carListingAdapter.notifyDataSetChanged();
        updateCarCount(filteredCarList.size());
    }

    // Helper method to extract the city name prefix from car location header
    private String extractCityFromHeader(String carLocation) {
        if (carLocation.startsWith("TP.HCM - ")) return "TP.HCM";
        if (carLocation.startsWith("Hà Nội - ")) return "Hà Nội";
        if (carLocation.startsWith("Đà Nẵng - ")) return "Đà Nẵng";
        return carLocation; // fallback if no known prefix
    }
}
