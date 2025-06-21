package com.midterm.mobiledesignfinalterm.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.List;

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
        new Thread(() -> {
            try {
                // Load overview statistics
                loadOverviewStats();
                
                // Load car status data
                loadCarStatusData();
                
                // Load user data
                loadUserData();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AdminDashboard.this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void loadOverviewStats() throws Exception {
        URL url = new URL("http://10.0.2.2/myapi/admin_stats.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000); // 10 seconds
        conn.setReadTimeout(10000); // 10 seconds

        int responseCode = conn.getResponseCode();
        System.out.println("Admin Stats API Response Code: " + responseCode);

        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        }
        
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }

        System.out.println("Admin Stats API Response: " + response.toString());
        
        JSONObject result = new JSONObject(response.toString());

        runOnUiThread(() -> {
            try {
                if (result.getBoolean("success")) {
                    JSONObject stats = result.getJSONObject("stats");
                    
                    tvTotalBookings.setText(String.valueOf(stats.getInt("total_bookings")));
                    tvTotalCars.setText(String.valueOf(stats.getInt("total_cars")));
                    tvTodayBookings.setText(String.valueOf(stats.getInt("today_bookings")));
                    tvWeekBookings.setText(String.valueOf(stats.getInt("week_bookings")));
                    tvMonthBookings.setText(String.valueOf(stats.getInt("month_bookings")));
                    tvCancelRate.setText(String.format("%.1f%%", stats.getDouble("cancel_rate")));
                    tvSuccessRate.setText(String.format("%.1f%%", stats.getDouble("success_rate")));
                    
                    // Show debug info if available
                    if (stats.has("debug_error")) {
                        Toast.makeText(AdminDashboard.this, "Debug: " + stats.getString("debug_error"), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load statistics", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AdminDashboard.this, "Lỗi xử lý dữ liệu thống kê: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCarStatusData() throws Exception {
        URL url = new URL("http://10.0.2.2/myapi/admin_car_status.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000); // 10 seconds
        conn.setReadTimeout(10000); // 10 seconds

        int responseCode = conn.getResponseCode();
        System.out.println("Car Status API Response Code: " + responseCode);

        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        }
        
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }

        System.out.println("Car Status API Response: " + response.toString());
        
        JSONObject result = new JSONObject(response.toString());

        runOnUiThread(() -> {
            try {
                if (result.getBoolean("success")) {
                    JSONObject data = result.getJSONObject("data");
                    
                    // Update counters
                    tvRentedCars.setText(String.valueOf(data.getInt("rented_count")));
                    tvAvailableCars.setText(String.valueOf(data.getInt("available_count")));
                    tvMaintenanceCars.setText(String.valueOf(data.getInt("maintenance_count")));

                    // Update lists
                    updateCarStatusLists(data);
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load car status data", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AdminDashboard.this, "Lỗi xử lý dữ liệu xe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCarStatusLists(JSONObject data) throws JSONException {
        // Clear existing lists
        rentedCarsList.clear();
        availableCarsList.clear();
        maintenanceCarsList.clear();

        // Parse rented cars
        if (data.has("rented_cars")) {
            JSONArray rentedArray = data.getJSONArray("rented_cars");
            for (int i = 0; i < rentedArray.length(); i++) {
                JSONObject carObj = rentedArray.getJSONObject(i);
                CarStatus carStatus = new CarStatus(
                    carObj.getInt("vehicle_id"),
                    carObj.getString("vehicle_name"),
                    carObj.getString("brand"),
                    carObj.getString("model"),
                    carObj.optString("rented_by", ""),
                    carObj.optString("rental_date", ""),
                    carObj.optString("return_date", ""),
                    "RENTED"
                );
                rentedCarsList.add(carStatus);
            }
        }

        // Parse available cars
        if (data.has("available_cars")) {
            JSONArray availableArray = data.getJSONArray("available_cars");
            for (int i = 0; i < availableArray.length(); i++) {
                JSONObject carObj = availableArray.getJSONObject(i);
                CarStatus carStatus = new CarStatus(
                    carObj.getInt("vehicle_id"),
                    carObj.getString("vehicle_name"),
                    carObj.getString("brand"),
                    carObj.getString("model"),
                    "",
                    "",
                    "",
                    "AVAILABLE"
                );
                availableCarsList.add(carStatus);
            }
        }

        // Parse maintenance cars
        if (data.has("maintenance_cars")) {
            JSONArray maintenanceArray = data.getJSONArray("maintenance_cars");
            for (int i = 0; i < maintenanceArray.length(); i++) {
                JSONObject carObj = maintenanceArray.getJSONObject(i);
                CarStatus carStatus = new CarStatus(
                    carObj.getInt("vehicle_id"),
                    carObj.getString("vehicle_name"),
                    carObj.getString("brand"),
                    carObj.getString("model"),
                    "",
                    carObj.optString("maintenance_date", ""),
                    carObj.optString("expected_completion", ""),
                    "MAINTENANCE"
                );
                maintenanceCarsList.add(carStatus);
            }
        }

        // Notify adapters
        rentedCarsAdapter.notifyDataSetChanged();
        availableCarsAdapter.notifyDataSetChanged();
        maintenanceCarsAdapter.notifyDataSetChanged();
    }

    private void loadUserData() throws Exception {
        URL url = new URL("http://10.0.2.2/myapi/admin_users.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000); // 10 seconds
        conn.setReadTimeout(10000); // 10 seconds

        int responseCode = conn.getResponseCode();
        System.out.println("User Data API Response Code: " + responseCode);

        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        }
        
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }

        System.out.println("User Data API Response: " + response.toString());
        
        JSONObject result = new JSONObject(response.toString());

        runOnUiThread(() -> {
            try {
                if (result.getBoolean("success")) {
                    JSONObject data = result.getJSONObject("data");
                    
                    // Update user counters
                    tvTotalUsers.setText(String.valueOf(data.getInt("total_users")));
                    tvTodayNewUsers.setText(String.valueOf(data.getInt("today_new_users")));
                    tvWeekNewUsers.setText(String.valueOf(data.getInt("week_new_users")));
                    tvMonthNewUsers.setText(String.valueOf(data.getInt("month_new_users")));

                    // Update new users list
                    newUsersList.clear();
                    if (data.has("new_users")) {
                        JSONArray newUsersArray = data.getJSONArray("new_users");
                        for (int i = 0; i < newUsersArray.length(); i++) {
                            JSONObject userObj = newUsersArray.getJSONObject(i);
                            UserInfo userInfo = new UserInfo(
                                userObj.getInt("account_id"),
                                userObj.optString("name", ""),
                                userObj.getString("phone_number"),
                                userObj.optString("email", ""),
                                userObj.getString("created_at")
                            );
                            newUsersList.add(userInfo);
                        }
                    }
                    newUsersAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminDashboard.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AdminDashboard.this, "Lỗi xử lý dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
