package com.midterm.mobiledesignfinalterm.homepage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.midterm.mobiledesignfinalterm.R;
import com.bumptech.glide.Glide;
import com.midterm.mobiledesignfinalterm.UserDashboard.UserDashboard;
import com.midterm.mobiledesignfinalterm.aboutUs.AboutUs;
import com.midterm.mobiledesignfinalterm.authentication.Login;
import com.midterm.mobiledesignfinalterm.faq.FAQActivity;

import android.widget.Toast;
import android.Manifest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

public class Homepage extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    private TextView textViewLocation;
    private TextView textViewPoints;
    private TextView textViewTopBrands;
    private TextView textViewViewAllBrands;
    private TextView textViewTopRatedCars;
    private TextView textViewViewAllCars;
    private TextView textViewWelcome;
    private TextView textViewPointsLabel;
    private RecyclerView recyclerViewBrands;
    private RecyclerView recyclerViewCars;
    private ImageView imageViewProfile;
    private ImageView imageViewPoints;
    private LinearLayout profileSection;
    private LinearLayout dropdownMenu;
    private LinearLayout layoutLocationHeader;
    private LinearLayout layoutBookingSection;
    private LinearLayout layoutTopBrandsSection;
    private LinearLayout layoutTopCarsSection;
    private Button btnMyProfile;
    private Button btnAboutUs; // Changed from btnMyBooking to match layout
    private Button btnFAQ;
    private Button btnSignOut;

    // FAQ section elements
    private LinearLayout faqItem1, faqItem2, faqItem3, faqItem4, faqItem5;
    private TextView faqAnswer1, faqAnswer2, faqAnswer3, faqAnswer4, faqAnswer5;
    private ImageView faqArrow1, faqArrow2, faqArrow3, faqArrow4, faqArrow5;
    private Button btnViewAllFAQ;

    // Pickup/Drop-off elements
    private TextView textViewPickupLocation;
    private TextView textViewPickupDate;
    private TextView textViewPickupTime;
    private TextView textViewDropoffLocation;
    private TextView textViewDropoffDate;
    private TextView textViewDropoffTime;

    // User information from login
    private String userPhone;
    private String userName;
    private String userId; // Added userId field
    private String userRawData; // Added raw user data field
    private List<String> userRoles;
    private boolean isDropdownVisible = false;
    private LinearLayout layoutPickupLocation;
    private LinearLayout layoutPickupDate;
    private LinearLayout layoutPickupTime;
    private LinearLayout layoutDropoffLocation;
    private LinearLayout layoutDropoffDate;
    private LinearLayout layoutDropoffTime;
    private ImageView imageViewSwapLocations;

    // Location related
    private LocationManager locationManager;
    private boolean isLocationEnabled = false;
    private boolean canGetLocation = false;

    // Calendar instances for date/time pickers
    private Calendar pickupCalendar;
    private Calendar dropoffCalendar;

    // Additional fields for user email and photo URI
    private String userEmail;
    private String userPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize views first
        initializeViews();
        handleUserIntent();

        // Make status bar transparent and content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        // Get user information from intent
        getUserInfoFromIntent();

        // Show Toast confirmation
        if (userName != null && !userName.isEmpty()) {
            Toast.makeText(this, "Welcome, " + userName + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Welcome! (No username received)", Toast.LENGTH_SHORT).show();
        }

        setupClickListeners();
        setupRecyclerViews();

        // Initialize dropdown as hidden
        if (dropdownMenu != null) {
            dropdownMenu.setVisibility(View.GONE);
            dropdownMenu.setAlpha(0f);
            dropdownMenu.setTranslationY(-20f);
        }

        // Initialize default dates and times for pickers
        initializeDefaultDateTime();

        // Initialize location services
        initializeLocation();

        // Initial entrance animation - moved to after location setup
        animateInitialEntrance();
    }

    private void getUserInfoFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userPhone = intent.getStringExtra("user_phone");
            userName = intent.getStringExtra("user_name");
            userId = intent.getStringExtra("user_id");
            userRawData = intent.getStringExtra("user_data");
            userRoles = intent.getStringArrayListExtra("user_roles");

            // After getting basic user info, fetch complete user info from API
            if (userPhone != null && !userPhone.isEmpty()) {
                fetchUserInfoFromApi(userPhone);
            }
        }
    }

    /**
     * Fetch complete user information from the API
     * This will get the user's role and other details that might not be included in the login response
     */
    private void fetchUserInfoFromApi(String phoneNumber) {
        // Show loading indicator if needed
        // loadingIndicator.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Create URL for the user_info API endpoint
                URL url = new URL("http://10.0.2.2/myapi/user_info.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // We need to send the phone number to get user info
                // In a real app, you might want to send a token instead
                String jsonInputString = "{\"phone_number\":\"" + phoneNumber + "\", \"password\":\"\"}";

                // Send data
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Debug: Print the full response
                System.out.println("User Info API Response: " + response.toString());

                // Parse JSON result
                JSONObject result = new JSONObject(response.toString());

                runOnUiThread(() -> {
                    try {
                        if (result.has("status") && result.getString("status").equals("success")) {
                            // Extract user details
                            if (userName == null || userName.isEmpty()) {
                                userName = result.optString("full_name", "");
                            }

                            // Extract user ID if we don't have it yet
                            if (userId == null || userId.isEmpty()) {
                                userId = String.valueOf(result.optInt("id", 0));
                            }

                            // Update roles with the actual role from the API
                            if (result.has("role_name")) {
                                String roleName = result.getString("role_name");
                                userRoles = new ArrayList<>();
                                userRoles.add(roleName);
                                System.out.println("Updated role from API: " + roleName);
                            }

                            // Store the complete user data
                            userRawData = result.toString();

                            // Update any UI components that display user info
                            updateUserInfoUI();
                        } else {
                            // Handle error
                            String errorMsg = result.optString("message", "Failed to get user info");
                            System.out.println("User Info Error: " + errorMsg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("JSON Parsing Error: " + e.getMessage());
                    } finally {
                        // Hide loading indicator if needed
                        // loadingIndicator.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Network Error: " + e.getMessage());

                runOnUiThread(() -> {
                    // Hide loading indicator if needed
                    // loadingIndicator.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    /**
     * Update any UI components that display user info
     */
    private void updateUserInfoUI() {
        // Update welcome message if it exists
        if (textViewWelcome != null) {
            if (userName != null && !userName.isEmpty()) {
                textViewWelcome.setText("Welcome, " + userName + "!");
            } else {
                textViewWelcome.setText("Welcome!");
            }
        }

        // You can update other UI components here as needed
    }

    private void initializeViews() {
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewTopBrands = findViewById(R.id.textViewTopBrands);
        textViewViewAllBrands = findViewById(R.id.textViewViewAllBrands);
        textViewTopRatedCars = findViewById(R.id.textViewTopRatedCars);
        textViewViewAllCars = findViewById(R.id.textViewViewAllCars);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        recyclerViewBrands = findViewById(R.id.recyclerViewBrands);
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        profileSection = findViewById(R.id.profileSection);
        dropdownMenu = findViewById(R.id.dropdownMenu);
        layoutLocationHeader = findViewById(R.id.layoutLocationHeader);
        layoutBookingSection = findViewById(R.id.layoutBookingSection);
        layoutTopBrandsSection = findViewById(R.id.layoutTopBrandsSection);
        layoutTopCarsSection = findViewById(R.id.layoutTopCarsSection);
        btnMyProfile = findViewById(R.id.btnMyProfile);
        btnAboutUs = findViewById(R.id.btnAboutUs);
        btnFAQ = findViewById(R.id.btnFAQ);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Initialize FAQ section elements
        faqItem1 = findViewById(R.id.faqItem1);
        faqItem2 = findViewById(R.id.faqItem2);
        faqItem3 = findViewById(R.id.faqItem3);
        faqItem4 = findViewById(R.id.faqItem4);
        faqItem5 = findViewById(R.id.faqItem5);
        faqAnswer1 = findViewById(R.id.faqAnswer1);
        faqAnswer2 = findViewById(R.id.faqAnswer2);
        faqAnswer3 = findViewById(R.id.faqAnswer3);
        faqAnswer4 = findViewById(R.id.faqAnswer4);
        faqAnswer5 = findViewById(R.id.faqAnswer5);
        faqArrow1 = findViewById(R.id.faqArrow1);
        faqArrow2 = findViewById(R.id.faqArrow2);
        faqArrow3 = findViewById(R.id.faqArrow3);
        faqArrow4 = findViewById(R.id.faqArrow4);
        faqArrow5 = findViewById(R.id.faqArrow5);
        btnViewAllFAQ = findViewById(R.id.btnViewAllFAQ);

        // Initialize pickup/drop-off elements
        layoutPickupLocation = findViewById(R.id.layoutPickupLocation);
        layoutPickupDate = findViewById(R.id.layoutPickupDate);
        layoutPickupTime = findViewById(R.id.layoutPickupTime);
        layoutDropoffLocation = findViewById(R.id.layoutDropoffLocation);
        layoutDropoffDate = findViewById(R.id.layoutDropoffDate);
        layoutDropoffTime = findViewById(R.id.layoutDropoffTime);
        imageViewSwapLocations = findViewById(R.id.imageViewSwapLocations);

        // TextViews for data display
        textViewPickupLocation = findViewById(R.id.textViewPickupLocation);
        textViewPickupDate = findViewById(R.id.textViewPickupDate);
        textViewPickupTime = findViewById(R.id.textViewPickupTime);
        textViewDropoffLocation = findViewById(R.id.textViewDropoffLocation);
        textViewDropoffDate = findViewById(R.id.textViewDropoffDate);
        textViewDropoffTime = findViewById(R.id.textViewDropoffTime);
    }
    private void handleUserIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.userName = extras.getString("user_name");
            this.userEmail = extras.getString("user_email");
            this.userId = extras.getString("user_id");
            this.userPhotoUri = extras.getString("user_photo_uri");

             // Use Glide to load the profile picture from the URL
            if (this.userPhotoUri != null && !this.userPhotoUri.isEmpty()) {
                 Glide.with(this)
                        .load(this.userPhotoUri)
                        .circleCrop() // Make the image circular
                        .placeholder(R.drawable.ic_profile) // Add a placeholder drawable
                        .error(R.drawable.ic_profile) // Add an error drawable
                        .into(imageViewProfile);
            }

            // You can log the other details or use them elsewhere
            Log.d("Homepage", "User Logged In: ID=" + userId + ", Email=" + userEmail);

        } else {
            // Handle case where no data is passed (e.g., direct launch for testing)
            Log.d("Homepage", "No intent extras found.");
        }
    }

    private void initializeDefaultDateTime() {
        pickupCalendar = Calendar.getInstance();
        dropoffCalendar = Calendar.getInstance();
        dropoffCalendar.add(Calendar.DAY_OF_YEAR, 1); // Default drop-off to next day

        // Set initial text for date and time fields
        updateDateTextView(textViewPickupDate, pickupCalendar);
        updateTimeTextView(textViewPickupTime, pickupCalendar);
        updateDateTextView(textViewDropoffDate, dropoffCalendar);
        updateTimeTextView(textViewDropoffTime, dropoffCalendar);
    }

    private void initializeLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, get location
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocation();
            } else {
                // Permission denied
                textViewLocation.setText("Location permission denied");
                Toast.makeText(this, "Location permission is required for better experience",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Check if GPS is enabled
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // Check if Network is enabled
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                showLocationSettingsDialog();
            } else {
                this.canGetLocation = true;

                // Check permissions again before requesting location updates
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                    // Get location from Network Provider
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    }

                    // Get location from GPS Provider
                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            textViewLocation.setText("Error getting location");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Get address from coordinates
            getAddressFromLocation(latitude, longitude);

            // Stop location updates after getting the location
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                String countryName = address.getCountryName();

                // Check if it's a Vietnamese city
                if (countryName != null && countryName.toLowerCase().contains("vietnam")) {
                    if (cityName != null && !cityName.isEmpty()) {
                        textViewLocation.setText(cityName + ", Vietnam");
                    } else {
                        // If city is null, try to get administrative area or other location info
                        String adminArea = address.getAdminArea();
                        if (adminArea != null && !adminArea.isEmpty()) {
                            textViewLocation.setText(adminArea + ", Vietnam");
                        } else {
                            textViewLocation.setText("Vietnam");
                        }
                    }
                } else {
                    // Not in Vietnam, show general location
                    if (cityName != null && countryName != null) {
                        textViewLocation.setText(cityName + ", " + countryName);
                    } else if (countryName != null) {
                        textViewLocation.setText(countryName);
                    } else {
                        textViewLocation.setText("Location detected");
                    }
                }
            } else {
                textViewLocation.setText("Unable to get address");
            }
        } catch (IOException e) {
            e.printStackTrace();
            textViewLocation.setText("Error getting address");
        }
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Location Settings");
        alertDialog.setMessage("Location services are not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            textViewLocation.setText("Location services disabled");
        });

        alertDialog.show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    private void setupClickListeners() {
        // Location header click listener
        layoutLocationHeader.setOnClickListener(v -> {
            animateTextClick(v);
            // Refresh location
            getLocation();
            Toast.makeText(Homepage.this, "Refreshing location...", Toast.LENGTH_SHORT).show();
        });

        textViewViewAllBrands.setOnClickListener(v -> {
            animateTextClick(v);
            handleViewAllBrands();
        });

        textViewViewAllCars.setOnClickListener(v -> {
            animateTextClick(v);
            handleViewAllCars();
        });

        profileSection.setOnClickListener(v -> {
            animateProfileClick(v);
            toggleDropdownMenu();
        });

        // Dropdown menu item listeners
        btnMyProfile.setOnClickListener(v -> {
            animateMenuItemClick(v);
            hideDropdownMenu();
            handleMyProfile();
        });

        btnAboutUs.setOnClickListener(v -> {
            animateMenuItemClick(v);
            hideDropdownMenu();
            Intent intent = new Intent(Homepage.this, AboutUs.class);
            startActivity(intent);
        });

        btnFAQ.setOnClickListener(v -> {
            animateMenuItemClick(v);
            hideDropdownMenu();
            Intent intent = new Intent(Homepage.this, FAQActivity.class);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> {
            animateMenuItemClick(v);
            hideDropdownMenu();
            handleSignOut();
        });

        // Pickup/Drop-off click listeners
        layoutPickupLocation.setOnClickListener(v -> {
            animateTextClick(v);
            showLocationSelectionDialog(textViewPickupLocation);
        });

        layoutPickupDate.setOnClickListener(v -> {
            animateTextClick(v);
            showDatePicker(textViewPickupDate, pickupCalendar);
        });

        layoutPickupTime.setOnClickListener(v -> {
            animateTextClick(v);
            showTimePicker(textViewPickupTime, pickupCalendar);
        });

        layoutDropoffLocation.setOnClickListener(v -> {
            animateTextClick(v);
            showLocationSelectionDialog(textViewDropoffLocation);
        });

        layoutDropoffDate.setOnClickListener(v -> {
            animateTextClick(v);
            showDatePicker(textViewDropoffDate, dropoffCalendar);
        });

        layoutDropoffTime.setOnClickListener(v -> {
            animateTextClick(v);
            showTimePicker(textViewDropoffTime, dropoffCalendar);
        });

        // Search/proceed button (central arrow)
        imageViewSwapLocations.setOnClickListener(v -> {
            animateButtonClick(v);
            // Start CarListing activity and pass user info and pickup/drop-off data
            Intent intent = new Intent(Homepage.this, com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);
            intent.putExtra("user_phone", userPhone);
            intent.putExtra("user_name", userName);
            intent.putExtra("user_id", userId); // Pass userId to CarListing
            intent.putExtra("user_data", userRawData); // Pass raw user data to CarListing
            if (userRoles != null) {
                intent.putStringArrayListExtra("user_roles", new ArrayList<>(userRoles));
            }
            intent.putExtra("pickup_location", textViewPickupLocation.getText().toString());
            intent.putExtra("dropoff_location", textViewDropoffLocation.getText().toString());
            intent.putExtra("pickup_date", textViewPickupDate.getText().toString());
            intent.putExtra("pickup_time", textViewPickupTime.getText().toString());
            intent.putExtra("dropoff_date", textViewDropoffDate.getText().toString());
            intent.putExtra("dropoff_time", textViewDropoffTime.getText().toString());
            startActivity(intent);
        });

        // FAQ section click listeners
        faqItem1.setOnClickListener(v -> toggleFAQ(faqAnswer1, faqArrow1));
        faqItem2.setOnClickListener(v -> toggleFAQ(faqAnswer2, faqArrow2));
        faqItem3.setOnClickListener(v -> toggleFAQ(faqAnswer3, faqArrow3));
        faqItem4.setOnClickListener(v -> toggleFAQ(faqAnswer4, faqArrow4));
        faqItem5.setOnClickListener(v -> toggleFAQ(faqAnswer5, faqArrow5));
        
        btnViewAllFAQ.setOnClickListener(v -> {
            animateButtonClick(v);
            Intent intent = new Intent(Homepage.this, FAQActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        // Setup brands RecyclerView
        recyclerViewBrands.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Brand> brandList = getBrandsList();
        BrandAdapter brandAdapter = new BrandAdapter(brandList, new BrandAdapter.OnBrandClickListener() {
            @Override
            public void onBrandClick(Brand brand) {
                handleBrandClick(brand);
            }
        });
        recyclerViewBrands.setAdapter(brandAdapter);

        // Setup cars RecyclerView with dynamic data
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Car> topCars = getTopRatedCarsFromJSON(); // Using the new method to get top rated cars
        CarAdapter carAdapter = new CarAdapter(topCars);
        recyclerViewCars.setAdapter(carAdapter);
    }

    /**
     * Handle click on a car brand
     * @param brand The selected car brand
     */
    private void handleBrandClick(Brand brand) {
        // Create intent for CarListing activity
        Intent intent = new Intent(Homepage.this, com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);

        // Pass user information
        intent.putExtra("user_phone", userPhone);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_data", userRawData);
        if (userRoles != null) {
            intent.putStringArrayListExtra("user_roles", new ArrayList<>(userRoles));
        }

        // Pass booking details if they're set
        if (textViewPickupLocation != null) {
            intent.putExtra("pickup_location", textViewPickupLocation.getText().toString());
            intent.putExtra("dropoff_location", textViewDropoffLocation.getText().toString());
            intent.putExtra("pickup_date", textViewPickupDate.getText().toString());
            intent.putExtra("pickup_time", textViewPickupTime.getText().toString());
            intent.putExtra("dropoff_date", textViewDropoffDate.getText().toString());
            intent.putExtra("dropoff_time", textViewDropoffTime.getText().toString());
        }

        // Only pass the selected brand name as a filter if it's not "All"
        if (!"All".equals(brand.getName())) {
            intent.putExtra("selected_brand", brand.getName());
            Toast.makeText(this, "Selected brand: " + brand.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Showing all brands", Toast.LENGTH_SHORT).show();
        }

        // Start the activity
        startActivity(intent);
    }

    private void toggleDropdownMenu() {
        if (isDropdownVisible) {
            hideDropdownMenu();
        } else {
            showDropdownMenu();
        }
    }

    private void showDropdownMenu() {
        isDropdownVisible = true;
        dropdownMenu.bringToFront();
        dropdownMenu.setVisibility(View.VISIBLE);

        dropdownMenu.setAlpha(0f);
        dropdownMenu.setTranslationY(-20f);
        dropdownMenu.setScaleX(0.8f);
        dropdownMenu.setScaleY(0.8f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(dropdownMenu, "alpha", 0f, 1f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(dropdownMenu, "translationY", -20f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dropdownMenu, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dropdownMenu, "scaleY", 0.8f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, translationY, scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }

    private void hideDropdownMenu() {
        isDropdownVisible = false;

        ObjectAnimator alpha = ObjectAnimator.ofFloat(dropdownMenu, "alpha", 1f, 0f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(dropdownMenu, "translationY", 0f, -20f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dropdownMenu, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dropdownMenu, "scaleY", 1f, 0.8f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, translationY, scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                dropdownMenu.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    /**
     * Shows a custom animated dropdown for selecting a city from a predefined list.
     * @param locationTextView The TextView to update with the selected location.
     */
    private void showLocationSelectionDialog(final TextView locationTextView) {
        final String[] cities = {"Hà Nội", "Đà Nẵng", "TP.HCM"};

        // Create AlertDialog with custom theme
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Dark);
        builder.setTitle("Select City");

        // Use custom array adapter to ensure text displays correctly
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                cities) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.white, null));
                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedCity = cities[which];
            locationTextView.setText(selectedCity);
            Toast.makeText(Homepage.this, "Selected: " + selectedCity, Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dropdown_background);
        dialog.show();
    }

    /**
     * Shows a modern Material Design DatePicker for selecting a date.
     * @param dateTextView The TextView to update with the selected date.
     * @param calendar The Calendar instance to use for initial date and to update after selection.
     */
    private void showDatePicker(final TextView dateTextView, final Calendar calendar) {
        // Create constraints to limit date selection
        // Setting minimum date to today to prevent selecting past dates
        Calendar minDate = Calendar.getInstance();

        // Create the date picker builder
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Date");
        builder.setSelection(calendar.getTimeInMillis());
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate.getTimeInMillis()))
                .build());

        // Apply custom dark theme
        builder.setTheme(R.style.CustomMaterialCalendar);

        // Create and customize the date picker
        MaterialDatePicker<Long> datePicker = builder.build();

        // Set up the positive button click listener
        datePicker.addOnPositiveButtonClickListener(selection -> {
            calendar.setTimeInMillis(selection);
            updateDateTextView(dateTextView, calendar);

            // Check if pickup date is after dropoff date and adjust if necessary
            if (dateTextView == textViewPickupDate &&
                calendar.getTimeInMillis() > dropoffCalendar.getTimeInMillis()) {
                // Set dropoff date to be at least one day after pickup
                dropoffCalendar.setTimeInMillis(selection);
                dropoffCalendar.add(Calendar.DAY_OF_YEAR, 1);
                updateDateTextView(textViewDropoffDate, dropoffCalendar);
                Toast.makeText(Homepage.this, "Return date adjusted to day after pickup", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the date picker with a subtle animation
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Updates the given TextView with the formatted date from the Calendar instance.
     * @param dateTextView The TextView to update.
     * @param calendar The Calendar instance containing the date.
     */
    private void updateDateTextView(TextView dateTextView, Calendar calendar) {
        String dateFormat = "dd MMMM yyyy"; // e.g., 20 July 2022
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        dateTextView.setText(sdf.format(calendar.getTime()));
    }

    /**
     * Shows a modern Material Design TimePicker for selecting a time.
     * @param timeTextView The TextView to update with the selected time.
     * @param calendar The Calendar instance to use for initial time and to update after selection.
     */
    private void showTimePicker(final TextView timeTextView, final Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create the Material TimePicker
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select Time")
                .setTheme(R.style.CustomMaterialTimePicker); // Apply custom dark theme

        final MaterialTimePicker timePicker = builder.build();

        // Set up listeners for the time picker
        timePicker.addOnPositiveButtonClickListener(view -> {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            updateTimeTextView(timeTextView, calendar);

            // If pickup time is selected and dropoff is on the same day, validate the times
            if (timeTextView == textViewPickupTime &&
                isSameDay(pickupCalendar, dropoffCalendar) &&
                pickupCalendar.after(dropoffCalendar)) {

                // Set dropoff time to be 1 hour after pickup if on the same day
                dropoffCalendar.setTimeInMillis(pickupCalendar.getTimeInMillis());
                dropoffCalendar.add(Calendar.HOUR_OF_DAY, 1);
                updateTimeTextView(textViewDropoffTime, dropoffCalendar);
                Toast.makeText(Homepage.this,
                    "Return time adjusted to be after pickup time",
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Show the time picker
        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    /**
     * Checks if two Calendar instances represent the same day.
     * @param cal1 First Calendar instance
     * @param cal2 Second Calendar instance
     * @return true if both calendars represent the same day
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Updates the given TextView with the formatted time from the Calendar instance.
     * @param timeTextView The TextView to update.
     * @param calendar The Calendar instance containing the time.
     */
    private void updateTimeTextView(TextView timeTextView, Calendar calendar) {
        String timeFormat = "HH:mm"; // e.g., 07:00
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        timeTextView.setText(sdf.format(calendar.getTime()));
    }

    private List<Brand> getBrandsList() {
        List<Brand> brands = new ArrayList<>();
        brands.add(new Brand("All", R.drawable.ic_all_brands));
        brands.add(new Brand("Toyota", R.drawable.toyota_logo));
        brands.add(new Brand("Honda", R.drawable.ic_honda));
        brands.add(new Brand("Kia", R.drawable.ic_kia));
        brands.add(new Brand("Mazda", R.drawable.ic_mazda));
        brands.add(new Brand("Ford", R.drawable.ford_logo));
        brands.add(new Brand("Lamborghini", R.drawable.lamborghini_logo));
        brands.add(new Brand("Porsche", R.drawable.porsche_logo));
        brands.add(new Brand("Maserati", R.drawable.maserati_logo));
        brands.add(new Brand("McLaren", R.drawable.ic_mclaren));
        brands.add(new Brand("Aston Martin", R.drawable.aston_martin_logo));
        brands.add(new Brand("Bentley", R.drawable.bentley_logo));
        brands.add(new Brand("Ferrari", R.drawable.ferrari_logo));
        brands.add(new Brand("BMW", R.drawable.bmw_logo));

        return brands;
    }

    /**
     * Helper method to load JSON data from assets folder
     * @return String containing the JSON data
     */
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("vehicles.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Get top rated cars from Firestore database
     * @return List of Car objects sorted by rating
     */
    private List<Car> getTopRatedCarsFromJSON() {
        List<Car> cars = new ArrayList<>();

        // Show temporary data while loading
        showLoading(true);

        // Use Firebase Firestore to get vehicles
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehicles")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                showLoading(false);

                // Process results
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String name = document.getString("name");
                    String description = document.getString("description");
                    int seats = document.getLong("seats") != null ? document.getLong("seats").intValue() : 0;
                    double basePrice = document.getDouble("base_price") != null ? document.getDouble("base_price") : 0.0;
                    double rating = document.getDouble("rating") != null ? document.getDouble("rating") : 0.0;
                    int totalTrips = document.getLong("total_trips") != null ? document.getLong("total_trips").intValue() : 0;

                    // Get primary image URL
                    String imageUrl = "";
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

                                    imageUrl = "file:///android_asset/" + relativePath;
                                    break;
                                }
                            }
                        }
                    }

                    // Create car using the constructor that matches our Car class definition
                    Car car = new Car(name, description, seats, basePrice, rating, imageUrl, totalTrips);
                    cars.add(car);
                }

                // Sort by rating (highest first) and then by total trips if ratings are equal
                Collections.sort(cars, (c1, c2) -> {
                    int ratingCompare = Double.compare(c2.getRating(), c1.getRating());
                    if (ratingCompare == 0) {
                        return Integer.compare(c2.getTotalTrips(), c1.getTotalTrips());
                    }
                    return ratingCompare;
                });

                // Take top 3 cars
                List<Car> topCars = cars.size() > 3 ? cars.subList(0, 3) : cars;

                // Update the RecyclerView adapter on the UI thread
                runOnUiThread(() -> {
                    CarAdapter carAdapter = new CarAdapter(topCars);
                    recyclerViewCars.setAdapter(carAdapter);
                });

            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e("Homepage", "Error getting car data: " + e.getMessage(), e);
                Toast.makeText(Homepage.this, "Error loading car data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                // Provide some default data in case of failure
                List<Car> defaultCars = getDefaultCars();
                runOnUiThread(() -> {
                    CarAdapter carAdapter = new CarAdapter(defaultCars);
                    recyclerViewCars.setAdapter(carAdapter);
                });
            });

        // Return empty list initially, it will be populated asynchronously
        return cars;
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean show) {
        // Add your loading indicator logic here if needed
    }

    /**
     * Provides default cars in case Firestore fetch fails
     */
    private List<Car> getDefaultCars() {
        List<Car> defaultCars = new ArrayList<>();
        defaultCars.add(new Car("Toyota Vios 2023", "Fuel-efficient family car", 4, 900000.00, 4.8, "", 125));
        defaultCars.add(new Car("Honda CR-V 2024", "Spacious SUV for family travel", 7, 1500000.00, 4.9, "", 180));
        defaultCars.add(new Car("Kia Morning 2022", "Compact, economical city car", 4, 500000.00, 4.5, "", 200));
        return defaultCars;
    }

    // Animation Methods - Enhanced with all components
    private void animateInitialEntrance() {
        View[] views = {
                layoutLocationHeader, textViewWelcome, profileSection,
                layoutBookingSection,
                layoutTopBrandsSection, recyclerViewBrands,
                layoutTopCarsSection, recyclerViewCars
        };

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;

            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.setScaleX(0.8f);
            view.setScaleY(0.8f);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setStartDelay(i * 100)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }
    }

    private void animateButtonClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.1f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), view.getElevation() + 8f, view.getElevation());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, elevation);
        animatorSet.setDuration(400);
        animatorSet.start();
    }

    private void animateTextClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(250);
        animatorSet.setInterpolator(new OvershootInterpolator(1.3f));
        animatorSet.start();
    }

    private void animateProfileClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", 0f, 8f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, elevation);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }

    private void animateMenuItemClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

    // Click handlers
    private void handleViewAllBrands() {
        Toast.makeText(this, "Showing all car brands", Toast.LENGTH_SHORT).show();

        // Navigate to CarListing activity with no brand filter
        Intent intent = new Intent(Homepage.this, com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);

        // Pass user information
        intent.putExtra("user_phone", userPhone);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_data", userRawData);
        if (userRoles != null) {
            intent.putStringArrayListExtra("user_roles", new ArrayList<>(userRoles));
        }

        // Pass booking details if they're set
        if (textViewPickupLocation != null) {
            intent.putExtra("pickup_location", textViewPickupLocation.getText().toString());
            intent.putExtra("dropoff_location", textViewDropoffLocation.getText().toString());
            intent.putExtra("pickup_date", textViewPickupDate.getText().toString());
            intent.putExtra("pickup_time", textViewPickupTime.getText().toString());
            intent.putExtra("dropoff_date", textViewDropoffDate.getText().toString());
            intent.putExtra("dropoff_time", textViewDropoffTime.getText().toString());
        }

        // Start the activity
        startActivity(intent);
    }

    private void handleViewAllCars() {
        Toast.makeText(this, "Showing all cars", Toast.LENGTH_SHORT).show();

        // Navigate to CarListing activity with no filter
        Intent intent = new Intent(Homepage.this, com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);

        // Pass user information
        intent.putExtra("user_phone", userPhone);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_data", userRawData);
        if (userRoles != null) {
            intent.putStringArrayListExtra("user_roles", new ArrayList<>(userRoles));
        }

        // Pass booking details if they're set
        if (textViewPickupLocation != null) {
            intent.putExtra("pickup_location", textViewPickupLocation.getText().toString());
            intent.putExtra("dropoff_location", textViewDropoffLocation.getText().toString());
            intent.putExtra("pickup_date", textViewPickupDate.getText().toString());
            intent.putExtra("pickup_time", textViewPickupTime.getText().toString());
            intent.putExtra("dropoff_date", textViewDropoffDate.getText().toString());
            intent.putExtra("dropoff_time", textViewDropoffTime.getText().toString());
        }

        // Start the activity
        startActivity(intent);
    }

    private void handleMyProfile() {
        // Navigate to profile screen with user info
         Intent intent = new Intent(Homepage.this, UserDashboard.class);
         intent.putExtra("user_phone", userPhone);
         intent.putExtra("user_name", userName);
         intent.putExtra("user_id", userId); // Pass userId to UserDashboard
         intent.putExtra("user_data", userRawData); // Pass raw user data to UserDashboard
         intent.putStringArrayListExtra("user_roles", (ArrayList<String>) userRoles);
         intent.putExtra("user_email", userEmail);
         intent.putExtra("user_photo_uri", userPhotoUri);
         startActivity(intent);
    }

    private void handleSettings() {
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        // Navigate to settings screen
        // Intent intent = new Intent(Homepage.this, SettingsActivity.class);
        // startActivity(intent);
    }

    private void handleSignOut() {
        // Sign out from Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();
        // Sign out from Firebase (if used)
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            // Ignore if Firebase is not used
        }
        // Clear user session and navigate to login
        Intent intent = new Intent(Homepage.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Pickup/Drop-off handlers (Removed individual handlers as they now call the generic picker methods)
    // The logic to call the pickers is directly in setupClickListeners

    private void handleProceedToSearch() {
        String pickup = textViewPickupLocation.getText().toString();
        String dropoff = textViewDropoffLocation.getText().toString();
        Toast.makeText(this, "Searching cars from " + pickup + " to " + dropoff, Toast.LENGTH_SHORT).show();
        // Add navigation logic here if needed
    }
    /**
     * Handle back button press to exit app and log out user
     */
    @Override
    public void onBackPressed() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Exit Application")
                .setMessage("Are you sure you want to exit and log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Log out the user from Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Sign out from Google if applicable
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                    googleSignInClient.signOut();

                    // Show logout toast
                    Toast.makeText(Homepage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to login screen
                    Intent intent = new Intent(Homepage.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Call super to finish activity
                    super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Data classes for RecyclerView
    public static class Brand {
        private String name;
        private int iconResource;

        public Brand(String name, int iconResource) {
            this.name = name;
            this.iconResource = iconResource;
        }

        public String getName() { return name; }
        public int getIconResource() { return iconResource; }
    }

    public static class Car {
        private String name;
        private String description; // Hoặc một trường dữ liệu phù hợp khác
        private int seats;
        private double price;
        private double rating;
        private String imageUrl;
        private int totalTrips; // Dùng để sắp xếp

        public Car(String name, String description, int seats, double price, double rating, String imageUrl, int totalTrips) {
            this.name = name;
            this.description = description;
            this.seats = seats;
            this.price = price;
            this.rating = rating;
            this.imageUrl = imageUrl;
            this.totalTrips = totalTrips;
        }

        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getSeats() { return seats; }
        public double getPrice() { return price; }
        public double getRating() { return rating; }
        public String getImageUrl() { return imageUrl; }
        public int getTotalTrips() { return totalTrips; }
    }

    /**
     * Toggle FAQ item visibility with animation
     */
    private void toggleFAQ(TextView answer, ImageView arrow) {
        if (answer.getVisibility() == View.GONE) {
            // Show answer
            answer.setVisibility(View.VISIBLE);
            answer.setAlpha(0f);
            answer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            
            // Rotate arrow
            ObjectAnimator.ofFloat(arrow, "rotation", 0f, 180f)
                    .setDuration(300)
                    .start();
        } else {
            // Hide answer
            answer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> answer.setVisibility(View.GONE))
                    .start();
            
            // Rotate arrow back
            ObjectAnimator.ofFloat(arrow, "rotation", 180f, 0f)
                    .setDuration(300)
                    .start();
        }
    }
}
