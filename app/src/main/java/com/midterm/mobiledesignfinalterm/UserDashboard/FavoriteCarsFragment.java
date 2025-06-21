package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.midterm.mobiledesignfinalterm.CarDetail.Amenity;
import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.api.RetrofitClient;
import com.midterm.mobiledesignfinalterm.firebase.FirestoreManager;
import com.midterm.mobiledesignfinalterm.models.FavoriteCar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteCarsFragment extends Fragment {

    private static final String TAG = "FavoriteCarsFragment";
    private RecyclerView recyclerViewFavoriteCars;
    private TextView textViewNoFavorites;
    private TextView textViewFavoritesHeader;
    private TextView textViewFavoritesSubheader;
    private FavoriteCarNewAdapter favoriteCarAdapter;
    private final List<FavoriteCar> favoriteCarsList = new ArrayList<>();
    private UserDashboard parentActivity;
    private String userId = "0";

    // Empty state view and browse cars button
    private LinearLayout emptyStateView;
    private Button btnBrowseCars;

    // Firestore Manager
    private FirestoreManager firestoreManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserDashboard) {
            parentActivity = (UserDashboard) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_cars, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupBrowseCarsButton();

        // Initialize FirestoreManager
        firestoreManager = FirestoreManager.getInstance();

        if (parentActivity != null) {
            try {
                userId = parentActivity.getUserId();
                Log.d(TAG, "User ID from activity: " + userId);
                if (userId == null || userId.isEmpty()) {
                    userId = "0";
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user ID: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Parent activity is null");
        }

        loadFavoriteCars();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewFavoriteCars = view.findViewById(R.id.recyclerViewFavoriteCars);
        textViewNoFavorites = view.findViewById(R.id.textViewNoFavorites);
        textViewFavoritesHeader = view.findViewById(R.id.textViewFavoritesHeader);
        textViewFavoritesSubheader = view.findViewById(R.id.textViewFavoritesSubheader);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        btnBrowseCars = view.findViewById(R.id.btnBrowseCars);

        // Fix text color if needed
        textViewFavoritesHeader.setTextColor(getResources().getColor(R.color.white, null));
        textViewFavoritesSubheader.setTextColor(getResources().getColor(R.color.text_secondary, null));
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        // Make sure we have a proper layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewFavoriteCars.setLayoutManager(layoutManager);

        // Create adapter with empty list initially
        favoriteCarAdapter = new FavoriteCarNewAdapter(favoriteCarsList, getContext(), this::toggleFavorite);

        // Important - set these for better performance and stability
        recyclerViewFavoriteCars.setHasFixedSize(true);

        // Set adapter to the RecyclerView
        recyclerViewFavoriteCars.setAdapter(favoriteCarAdapter);

        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupBrowseCarsButton() {
        btnBrowseCars.setOnClickListener(v -> {
            // Navigate to CarListingFragment
            if (parentActivity != null) {
                parentActivity.showCarListingFragment();
                Log.d(TAG, "Navigating to Car Listing Fragment");
            }
        });
    }

    public void loadFavoriteCars() {
        Log.d(TAG, "Loading favorite cars for user ID: " + userId);
        if ("0".equals(userId)) {
            showNoFavorites("User ID not available");
            return;
        }

        // Show the loading state
        recyclerViewFavoriteCars.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        textViewNoFavorites.setText("Loading favorites...");

        // Use FirestoreManager to load favorites
        firestoreManager.fetchUserFavoriteCars(userId, new FirestoreManager.FavoriteCarsCallback() {
            @Override
            public void onFavoriteCarsLoaded(List<FavoriteCar> favoriteCars) {
                favoriteCarsList.clear();
                favoriteCarsList.addAll(favoriteCars);

                Log.d(TAG, "Loaded " + favoriteCarsList.size() + " favorite cars from Firestore");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!favoriteCarsList.isEmpty()) {
                            favoriteCarAdapter.notifyDataSetChanged();
                            showContent();
                        } else {
                            showNoFavorites("You don't have any favorite cars yet");
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading favorites: " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> showNoFavorites("Error: " + errorMessage));
                }
            }
        });

        // Keep the existing API method as fallback (will remove later)
        // loadFavoriteCarsFromApi();
    }

    // Original API method renamed and kept as fallback
    private void loadFavoriteCarsFromApi() {
        Log.d(TAG, "Loading favorite cars from API for user ID: " + userId);
        if ("0".equals(userId)) {
            showNoFavorites("User ID not available");
            return;
        }

        // Show the loading state (we no longer have a progress bar, so just show empty view for now)
        recyclerViewFavoriteCars.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        textViewNoFavorites.setText("Loading favorites...");

        RetrofitClient.getFavoriteApiService().getUserFavorites(Integer.parseInt(userId))
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        Log.d(TAG, "API Response: " + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> result = response.body();
                            Log.d(TAG, "Response body: " + new Gson().toJson(result));

                            if (result.containsKey("status") && result.get("status").equals("success")) {
                                handleSuccessResponse(result);
                            } else {
                                String message = result.containsKey("message") ? (String) result.get("message") : "Error loading favorites";
                                Log.e(TAG, "API error: " + message);
                                showNoFavorites(message);
                            }
                        } else {
                            Log.e(TAG, "API error code: " + response.code());
                            showNoFavorites("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "API failure: " + t.getMessage());
                        showNoFavorites("Network error: " + t.getMessage());
                    }
                });
    }

    private void showContent() {
        Log.d(TAG, "showContent called - making RecyclerView visible");

        // Check if we have data
        if (favoriteCarsList.isEmpty()) {
            // If no data, show empty state view
            recyclerViewFavoriteCars.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            Log.d(TAG, "No favorite cars found, showing empty state");
        } else {
            // If we have data, show RecyclerView
            emptyStateView.setVisibility(View.GONE);
            recyclerViewFavoriteCars.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing " + favoriteCarsList.size() + " favorite cars");

            // Check adapter
            if (favoriteCarAdapter != null) {
                Log.d(TAG, "Adapter item count: " + favoriteCarAdapter.getItemCount());
            } else {
                Log.e(TAG, "Adapter is null!");
            }
        }
    }

    private void showNoFavorites(String message) {
        recyclerViewFavoriteCars.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        textViewNoFavorites.setText(message);
        Log.d(TAG, "Showing no favorites message: " + message);
    }

    private void handleSuccessResponse(Map<String, Object> result) {
        try {
            favoriteCarsList.clear();

            if (result.containsKey("data")) {
                // Convert data object to JSON string first
                String dataJson = new Gson().toJson(result.get("data"));
                Log.d(TAG, "Data JSON: " + dataJson);

                // Parse the JSON array manually to handle numeric type conversions properly
                JsonArray carsArray = JsonParser.parseString(dataJson).getAsJsonArray();
                for (JsonElement element : carsArray) {
                    JsonObject carObject = element.getAsJsonObject();
                    FavoriteCar car = new FavoriteCar();

                    // Set integer fields
                    car.setVehicleId(carObject.get("id").getAsInt());
                    car.setTotalTrips(carObject.get("trips").getAsInt());
                    car.setSeats(carObject.get("seats").getAsInt());
                    car.setLessorId(carObject.get("lessor_id").getAsInt());
                    car.setFavoriteId(carObject.get("favorite_id").getAsInt());

                    // Set float/double fields
                    car.setRating(carObject.get("rating").getAsFloat());
                    car.setBasePrice(carObject.get("base_price").getAsDouble());

                    // Set string fields
                    car.setName(carObject.get("name").getAsString());
                    car.setLocation(carObject.get("location").getAsString());
                    car.setTransmission(carObject.get("transmission").getAsString());
                    car.setFuelType(carObject.get("fuel").getAsString());
                    car.setPriceDisplay(carObject.get("price_display").getAsString());
                    car.setPriceFormatted(carObject.get("price_formatted").getAsString());
                    car.setVehicleType(carObject.get("vehicle_type").getAsString());
                    car.setDescription(carObject.get("description").getAsString());
                    car.setStatus(carObject.get("status").getAsString());
                    car.setFavorite(carObject.get("is_favorite").getAsBoolean());
                    car.setFavoritedAt(carObject.get("favorited_at").getAsString());
                    car.setPrimaryImage(carObject.get("primary_image").getAsString());

                    // Parse amenities if they exist
                    if (carObject.has("amenities") && !carObject.get("amenities").isJsonNull()) {
                        JsonArray amenitiesArray = carObject.get("amenities").getAsJsonArray();
                        Type amenityListType = new TypeToken<List<Amenity>>(){}.getType();
                        List<Amenity> amenities = new Gson().fromJson(amenitiesArray, amenityListType);
                        car.setAmenities(amenities);
                    }

                    favoriteCarsList.add(car);
                    Log.d(TAG, "Added car to list: " + car.getName() + ", image: " + car.getPrimaryImage());
                }

                Log.d(TAG, "Parsed " + favoriteCarsList.size() + " favorite cars");

                // Update UI based on data
                if (!favoriteCarsList.isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        favoriteCarAdapter.notifyDataSetChanged();
                        showContent();
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        showNoFavorites("You don't have any favorite cars yet");
                    });
                }
            } else {
                Log.e(TAG, "No data key in response");
                getActivity().runOnUiThread(() -> {
                    showNoFavorites("No data available");
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing data: " + e.getMessage());
            e.printStackTrace();
            getActivity().runOnUiThread(() -> {
                showNoFavorites("Error parsing data: " + e.getMessage());
            });
        }
    }

    private void toggleFavorite(FavoriteCar car) {
        if ("0".equals(userId)) {
            Toast.makeText(getContext(), "Unable to update favorites: User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toggle favorite state locally first
        boolean newFavoriteState = !car.isFavorite();

        // Use FirestoreManager to update Firestore database
        String vehicleId = String.valueOf(car.getVehicleId());

        firestoreManager.toggleFavorite(userId, vehicleId, newFavoriteState, new FirestoreManager.ToggleFavoriteCallback() {
            @Override
            public void onSuccess(boolean isFavorite) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update UI after successful database update
                        String message = isFavorite ?
                                car.getName() + " added to favorites" :
                                car.getName() + " removed from favorites";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                        // Reload favorites to reflect the changes
                        loadFavoriteCars();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload favorite cars when coming back to this fragment
        loadFavoriteCars();
    }
}
