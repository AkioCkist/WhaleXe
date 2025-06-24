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
        // Calculate Total Bookings
        db.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalBookings = queryDocumentSnapshots.size();
                    tvTotalBookings.setText(String.valueOf(totalBookings));

                    // Calculate bookings for today, this week, and this month
                    int todayBookings = 0;
                    int weekBookings = 0;
                    int monthBookings = 0;
                    long cancelledBookings = 0;

                    Calendar cal = Calendar.getInstance();
                    int currentDay = cal.get(Calendar.DAY_OF_YEAR);
                    int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
                    int currentMonth = cal.get(Calendar.MONTH);
                    int currentYear = cal.get(Calendar.YEAR);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp createdAtTimestamp = document.getTimestamp("createdAt");
                        if (createdAtTimestamp != null) {
                            Date bookingDate = createdAtTimestamp.toDate();
                            Calendar bookingCal = Calendar.getInstance();
                            bookingCal.setTime(bookingDate);

                            if (bookingCal.get(Calendar.YEAR) == currentYear) {
                                if (bookingCal.get(Calendar.MONTH) == currentMonth) {
                                    monthBookings++;
                                }
                                if (bookingCal.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                                    weekBookings++;
                                }
                                if (bookingCal.get(Calendar.DAY_OF_YEAR) == currentDay) {
                                    todayBookings++;
                                }
                            }
                        }
                        if ("CANCELLED".equalsIgnoreCase(document.getString("status"))) {
                            cancelledBookings++;
                        }
                    }

                    tvTodayBookings.setText(String.valueOf(todayBookings));
                    tvWeekBookings.setText(String.valueOf(weekBookings));
                    tvMonthBookings.setText(String.valueOf(monthBookings));

                    if (totalBookings > 0) {
                        double successRate = ((double) (totalBookings - cancelledBookings) / totalBookings) * 100;
                        double cancelRate = ((double) cancelledBookings / totalBookings) * 100;
                        tvSuccessRate.setText(String.format("%.1f%%", successRate));
                        tvCancelRate.setText(String.format("%.1f%%", cancelRate));
                    } else {
                        tvSuccessRate.setText("0.0%");
                        tvCancelRate.setText("0.0%");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboard.this, "Failed to load booking statistics", Toast.LENGTH_SHORT).show();
                });

        // Calculate Total Cars
        db.collection("vehicles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvTotalCars.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboard.this, "Failed to load total cars", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCarStatusData(FirebaseFirestore db) {
        db.collection("bookings").whereEqualTo("status", "CONFIRMED").get()
                .addOnCompleteListener(bookingTask -> {
                    if (bookingTask.isSuccessful()) {
                        List<String> rentedCarIds = new ArrayList<>();
                        for (QueryDocumentSnapshot bookingDoc : bookingTask.getResult()) {
                            String carId = bookingDoc.getString("carId");
                            if (carId != null) {
                                rentedCarIds.add(carId);
                            }
                        }

                        db.collection("vehicles").get().addOnCompleteListener(vehicleTask -> {
                            if (vehicleTask.isSuccessful()) {
                                processCarData(vehicleTask.getResult(), rentedCarIds, bookingTask.getResult());
                            } else {
                                Toast.makeText(AdminDashboard.this, "Failed to load car data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(AdminDashboard.this, "Failed to load booking data for car status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processCarData(QuerySnapshot vehicleSnapshot, List<String> rentedCarIds, QuerySnapshot bookingSnapshot) {
        rentedCarsList.clear();
        availableCarsList.clear();
        maintenanceCarsList.clear();

        for (QueryDocumentSnapshot document : vehicleSnapshot) {
            String vehicleId = document.getId();
            String vehicleName = document.getString("name");
            String status;
            String rentedBy = "";
            String rentalDate = "";
            String returnDate = "";

            String[] nameParts = vehicleName != null ? vehicleName.split(" ", 2) : new String[]{"Unknown", "Vehicle"};
            String brand = nameParts[0];
            String model = nameParts.length > 1 ? nameParts[1] : "";


            if (rentedCarIds.contains(vehicleId)) {
                status = "RENTED";
                for (QueryDocumentSnapshot bookingDoc : bookingSnapshot) {
                    if (vehicleId.equals(bookingDoc.getString("carId"))) {
                        rentedBy = bookingDoc.getString("userName");
                        rentalDate = bookingDoc.getString("pickupDate");
                        returnDate = bookingDoc.getString("dropoffDate");
                        break;
                    }
                }
            } else if ("maintenance".equalsIgnoreCase(document.getString("status"))) {
                status = "MAINTENANCE";
            } else {
                status = "AVAILABLE";
            }

            CarStatus carStatus = new CarStatus(
                    Integer.parseInt(document.getId()),
                    vehicleName,
                    brand,
                    model,
                    rentedBy,
                    rentalDate,
                    returnDate,
                    status
            );

            switch (status) {
                case "RENTED":
                    rentedCarsList.add(carStatus);
                    break;
                case "AVAILABLE":
                    availableCarsList.add(carStatus);
                    break;
                case "MAINTENANCE":
                    maintenanceCarsList.add(carStatus);
                    break;
            }
        }

        tvRentedCars.setText(String.valueOf(rentedCarsList.size()));
        tvAvailableCars.setText(String.valueOf(availableCarsList.size()));
        tvMaintenanceCars.setText(String.valueOf(maintenanceCarsList.size()));

        rentedCarsAdapter.notifyDataSetChanged();
        availableCarsAdapter.notifyDataSetChanged();
        maintenanceCarsAdapter.notifyDataSetChanged();
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
                        int todayNewUsers = 0;
                        int weekNewUsers = 0;
                        int monthNewUsers = 0;

                        Calendar cal = Calendar.getInstance();
                        int currentDay = cal.get(Calendar.DAY_OF_YEAR);
                        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
                        int currentMonth = cal.get(Calendar.MONTH);
                        int currentYear = cal.get(Calendar.YEAR);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("username");
                            String phoneNumber = document.getString("phone_number");
                            String email = "Not provided"; // No email in your user data

                            Date creationDate = null;
                            if (document.get("created_at") instanceof Timestamp) {
                                creationDate = document.getTimestamp("created_at").toDate();
                            } else if (document.get("created_at") instanceof String) {
                                // Add robust date parsing if needed
                            }

                            String createdAtString = "Unknown date";
                            if (creationDate != null) {
                                createdAtString = android.text.format.DateFormat.format("dd/MM/yyyy", creationDate).toString();
                                Calendar userCal = Calendar.getInstance();
                                userCal.setTime(creationDate);

                                if (userCal.get(Calendar.YEAR) == currentYear) {
                                    if (userCal.get(Calendar.MONTH) == currentMonth) {
                                        monthNewUsers++;
                                    }
                                    if (userCal.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                                        weekNewUsers++;
                                    }
                                    if (userCal.get(Calendar.DAY_OF_YEAR) == currentDay) {
                                        todayNewUsers++;
                                    }
                                }
                            }

                            UserInfo userInfo = new UserInfo(
                                    document.getId(),
                                    name,
                                    phoneNumber,
                                    email,
                                    createdAtString
                            );
                            newUsersList.add(userInfo);
                        }

                        tvTotalUsers.setText(String.valueOf(newUsersList.size()));
                        tvTodayNewUsers.setText(String.valueOf(todayNewUsers));
                        tvWeekNewUsers.setText(String.valueOf(weekNewUsers));
                        tvMonthNewUsers.setText(String.valueOf(monthNewUsers));

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
