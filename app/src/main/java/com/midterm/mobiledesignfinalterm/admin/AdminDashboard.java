package com.midterm.mobiledesignfinalterm.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase imports
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.admin.models.AdminStats;
import com.midterm.mobiledesignfinalterm.admin.models.CarStatus;
import com.midterm.mobiledesignfinalterm.admin.models.UserInfo;
import com.midterm.mobiledesignfinalterm.admin.adapters.CarStatusAdapter;
import com.midterm.mobiledesignfinalterm.admin.adapters.UserInfoAdapter;
import com.midterm.mobiledesignfinalterm.authentication.Login;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity {

    // Logout Button
    private ImageButton btnLogout;

    // Overview Statistics TextViews
    private TextView tvTotalBookings;
    private TextView tvTotalCars;
    private TextView tvTodayBookings;
    private TextView tvWeekBookings;
    private TextView tvMonthBookings;
    private TextView tvCancelRate;
    private TextView tvSuccessRate;

    // Car Status TextViews
    private TextView tvRentedCars;
    private TextView tvAvailableCars;
    private TextView tvMaintenanceCars;

    // RecyclerViews for lists
    private RecyclerView recyclerViewRentedCars;
    private RecyclerView recyclerViewAvailableCars;
    private RecyclerView recyclerViewMaintenanceCars;
    private RecyclerView recyclerViewNewUsers;

    // User Statistics TextViews
    private TextView tvTotalUsers;
    private TextView tvTodayNewUsers;
    private TextView tvWeekNewUsers;
    private TextView tvMonthNewUsers;

    // Adapters
    private CarStatusAdapter rentedCarsAdapter;
    private CarStatusAdapter availableCarsAdapter;
    private CarStatusAdapter maintenanceCarsAdapter;
    private UserInfoAdapter newUsersAdapter;

    // Data Lists
    private List<CarStatus> rentedCarsList = new ArrayList<>();
    private List<CarStatus> availableCarsList = new ArrayList<>();
    private List<CarStatus> maintenanceCarsList = new ArrayList<>();
    private List<UserInfo> newUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Make status bar transparent and content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        initializeViews();
        setupRecyclerViews();
        setupLogoutButton();
        loadAdminData();
    }

    private void initializeViews() {
        // Logout Button
        btnLogout = findViewById(R.id.btn_logout);

        // Overview Statistics
        tvTotalBookings = findViewById(R.id.tv_total_bookings);
        tvTotalCars = findViewById(R.id.tv_total_cars);
        tvTodayBookings = findViewById(R.id.tv_today_bookings);
        tvWeekBookings = findViewById(R.id.tv_week_bookings);
        tvMonthBookings = findViewById(R.id.tv_month_bookings);
        tvCancelRate = findViewById(R.id.tv_cancel_rate);
        tvSuccessRate = findViewById(R.id.tv_success_rate);

        // Car Status
        tvRentedCars = findViewById(R.id.tv_rented_cars);
        tvAvailableCars = findViewById(R.id.tv_available_cars);
        tvMaintenanceCars = findViewById(R.id.tv_maintenance_cars);

        // RecyclerViews
        recyclerViewRentedCars = findViewById(R.id.recycler_rented_cars);
        recyclerViewAvailableCars = findViewById(R.id.recycler_available_cars);
        recyclerViewMaintenanceCars = findViewById(R.id.recycler_maintenance_cars);
        recyclerViewNewUsers = findViewById(R.id.recycler_new_users);

        // User Statistics
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTodayNewUsers = findViewById(R.id.tv_today_new_users);
        tvWeekNewUsers = findViewById(R.id.tv_week_new_users);
        tvMonthNewUsers = findViewById(R.id.tv_month_new_users);
    }

    private void setupRecyclerViews() {
        // Setup Rented Cars RecyclerView
        rentedCarsAdapter = new CarStatusAdapter(rentedCarsList, this);
        recyclerViewRentedCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRentedCars.setAdapter(rentedCarsAdapter);

        // Setup Available Cars RecyclerView
        availableCarsAdapter = new CarStatusAdapter(availableCarsList, this);
        recyclerViewAvailableCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAvailableCars.setAdapter(availableCarsAdapter);

        // Setup Maintenance Cars RecyclerView
        maintenanceCarsAdapter = new CarStatusAdapter(maintenanceCarsList, this);
        recyclerViewMaintenanceCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMaintenanceCars.setAdapter(maintenanceCarsAdapter);

        // Setup New Users RecyclerView
        newUsersAdapter = new UserInfoAdapter(newUsersList, this);
        recyclerViewNewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNewUsers.setAdapter(newUsersAdapter);
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            // Clear user session data
            clearUserSession();
            
            // Show logout confirmation
            Toast.makeText(AdminDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate back to login screen
            Intent intent = new Intent(AdminDashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void clearUserSession() {
        // Clear SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        // Clear any other session data if needed
        // For example, if you have a singleton user manager, clear it here
    }

    private void loadAdminData() {
        // Initialize Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load all data from Firestore
        loadOverviewStats(db);
        loadCarStatusData(db);
        loadUserData(db);
    }

    private void loadOverviewStats(FirebaseFirestore db) {
        db.collection("admin_stats").document("overview")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Update UI with the retrieved data
                        tvTotalBookings.setText(String.valueOf(document.getLong("total_bookings")));
                        tvTotalCars.setText(String.valueOf(document.getLong("total_cars")));
                        tvTodayBookings.setText(String.valueOf(document.getLong("today_bookings")));
                        tvWeekBookings.setText(String.valueOf(document.getLong("week_bookings")));
                        tvMonthBookings.setText(String.valueOf(document.getLong("month_bookings")));
                        tvCancelRate.setText(String.format("%.1f%%", document.getDouble("cancel_rate")));
                        tvSuccessRate.setText(String.format("%.1f%%", document.getDouble("success_rate")));
                    } else {
                        Toast.makeText(AdminDashboard.this, "No data found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load statistics", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdminDashboard.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadCarStatusData(FirebaseFirestore db) {
        // First try direct car_status collection
        db.collection("car_status").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    processCarData(task.getResult());
                } else {
                    // If car_status collection doesn't exist or is empty, try vehicles collection
                    loadCarDataFromVehicles(db);
                }
            })
            .addOnFailureListener(e -> {
                // If car_status doesn't exist, try vehicles collection
                loadCarDataFromVehicles(db);
            });
    }

    private void loadCarDataFromVehicles(FirebaseFirestore db) {
        db.collection("vehicles").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    processCarData(task.getResult());
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load car data", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdminDashboard.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void processCarData(QuerySnapshot querySnapshot) {
        // Clear existing lists
        rentedCarsList.clear();
        availableCarsList.clear();
        maintenanceCarsList.clear();

        int rentedCount = 0;
        int availableCount = 0;
        int maintenanceCount = 0;

        for (QueryDocumentSnapshot document : querySnapshot) {
            try {
                // Extract vehicle id with null safety
                int vehicleId;
                if (document.contains("vehicle_id")) {
                    Long idLong = document.getLong("vehicle_id");
                    vehicleId = (idLong != null) ? idLong.intValue() : 0;
                } else if (document.contains("id")) {
                    Object idObj = document.get("id");
                    if (idObj instanceof Long) {
                        vehicleId = ((Long) idObj).intValue();
                    } else if (idObj instanceof String) {
                        try {
                            vehicleId = Integer.parseInt((String) idObj);
                        } catch (NumberFormatException e) {
                            vehicleId = document.getId().hashCode();
                        }
                    } else {
                        vehicleId = document.getId().hashCode();
                    }
                } else {
                    vehicleId = document.getId().hashCode();
                }

                // Get vehicle name with fallback
                String vehicleName = document.getString("vehicle_name");
                if (vehicleName == null) {
                    vehicleName = document.getString("name");
                    if (vehicleName == null) {
                        vehicleName = "Car #" + vehicleId;
                    }
                }

                // Get brand and model with fallbacks
                String brand = document.getString("brand");
                if (brand == null) brand = "";

                String model = document.getString("model");
                if (model == null) model = "";

                // Get status with fallbacks - try different possible field names
                String status = document.getString("status");
                if (status == null) {
                    status = "AVAILABLE"; // Default status
                }

                // Convert status to uppercase for consistency and normalize values
                status = status.toUpperCase();
                if (status.equals("RENTED") || status.equals("OCCUPIED") || status.equals("IN_USE")) {
                    status = "RENTED";
                } else if (status.equals("AVAILABLE") || status.equals("READY") || status.equals("FREE")) {
                    status = "AVAILABLE";
                } else if (status.equals("MAINTENANCE") || status.equals("REPAIR") || status.equals("UNDER_MAINTENANCE")) {
                    status = "MAINTENANCE";
                } else {
                    status = "AVAILABLE"; // Default fallback
                }

                // Additional optional fields - set empty if not available
                String rentedBy = document.getString("rented_by");
                if (rentedBy == null) rentedBy = "";

                String rentalDate = document.getString("rental_date");
                if (rentalDate == null) rentalDate = "";

                String returnDate = document.getString("return_date");
                if (returnDate == null) returnDate = "";

                // Create car status object with all the data
                CarStatus carStatus = new CarStatus(
                    vehicleId,
                    vehicleName,
                    brand,
                    model,
                    rentedBy,
                    rentalDate,
                    returnDate,
                    status
                );

                // Add to appropriate list based on status
                switch (status) {
                    case "RENTED":
                        rentedCarsList.add(carStatus);
                        rentedCount++;
                        break;
                    case "AVAILABLE":
                        availableCarsList.add(carStatus);
                        availableCount++;
                        break;
                    case "MAINTENANCE":
                        maintenanceCarsList.add(carStatus);
                        maintenanceCount++;
                        break;
                }
            } catch (Exception e) {
                Log.e("AdminDashboard", "Error processing car document: " + e.getMessage());
            }
        }

        // Update counters UI
        tvRentedCars.setText(String.valueOf(rentedCount));
        tvAvailableCars.setText(String.valueOf(availableCount));
        tvMaintenanceCars.setText(String.valueOf(maintenanceCount));

        // Notify adapters
        rentedCarsAdapter.notifyDataSetChanged();
        availableCarsAdapter.notifyDataSetChanged();
        maintenanceCarsAdapter.notifyDataSetChanged();
    }

    private void loadUserData(FirebaseFirestore db) {
        db.collection("users").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    newUsersList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Safely handle account_id which might be stored as Long, String, or might be null
                        int accountId = 0;
                        if (document.contains("account_id")) {
                            // Try to get as Long first
                            Long accountIdLong = document.getLong("account_id");
                            if (accountIdLong != null) {
                                accountId = accountIdLong.intValue();
                            } else {
                                // Try as String and parse
                                String accountIdStr = document.getString("account_id");
                                if (accountIdStr != null) {
                                    try {
                                        accountId = Integer.parseInt(accountIdStr);
                                    } catch (NumberFormatException e) {
                                        // Use document ID as fallback
                                        accountId = document.getId().hashCode();
                                    }
                                }
                            }
                        } else if (document.contains("id")) {
                            // Try "id" field if "account_id" doesn't exist
                            Object idObj = document.get("id");
                            if (idObj instanceof Long) {
                                accountId = ((Long) idObj).intValue();
                            } else if (idObj instanceof String) {
                                try {
                                    accountId = Integer.parseInt((String) idObj);
                                } catch (NumberFormatException e) {
                                    accountId = idObj.toString().hashCode();
                                }
                            }
                        } else {
                            // Use document ID as fallback
                            accountId = document.getId().hashCode();
                        }

                        // Handle other fields with null safety
                        String name = document.getString("username");
                        if (name == null) {
                            name = document.getString("name");
                            if (name == null) {
                                name = "User " + accountId;
                            }
                        }

                        String phoneNumber = document.getString("phone_number");
                        if (phoneNumber == null) {
                            phoneNumber = "Not provided";
                        }

                        String email = document.getString("email");
                        if (email == null) {
                            email = "Not provided";
                        }

                        // Handle created_at which could be a Timestamp or String
                        String createdAt = "Unknown date";
                        if (document.contains("created_at")) {
                            Object dateObj = document.get("created_at");
                            if (dateObj instanceof Timestamp) {
                                Timestamp timestamp = (Timestamp) dateObj;
                                createdAt = android.text.format.DateFormat.format("dd/MM/yyyy", timestamp.toDate()).toString();
                            } else if (dateObj instanceof String) {
                                createdAt = (String) dateObj;
                            }
                        }

                        UserInfo userInfo = new UserInfo(
                            accountId,
                            name,
                            phoneNumber,
                            email,
                            createdAt
                        );
                        newUsersList.add(userInfo);
                    }

                    // Update user statistics
                    int totalUsers = newUsersList.size();
                    tvTotalUsers.setText(String.valueOf(totalUsers));

                    // Basic calculation for date filtering
                    Calendar calendar = Calendar.getInstance();
                    Date now = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    Date yesterday = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_YEAR, -6); // 7 days ago from now
                    Date weekAgo = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_YEAR, -23); // 30 days ago from now
                    Date monthAgo = calendar.getTime();

                    // For now, just use placeholders
                    tvTodayNewUsers.setText("0");
                    tvWeekNewUsers.setText("0");
                    tvMonthNewUsers.setText("0");

                    newUsersAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdminDashboard.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
