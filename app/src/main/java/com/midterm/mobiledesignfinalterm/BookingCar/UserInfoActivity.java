package com.midterm.mobiledesignfinalterm.BookingCar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.midterm.mobiledesignfinalterm.R;

public class UserInfoActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etCitizenId, etTaxId;
    private CheckBox cbTermsAccepted;
    private Button btnNextStep;
    private ImageView ivBack;

    private String pickupLocation, dropoffLocation, pickupDate, pickupTime, dropoffDate, dropoffTime;
    private String carId, carName, carPrice, userId, userPhone, userName;
    private double carPriceRaw = 0;

    // Add logging tag for consistent tracking
    private static final String TAG = "BookingSuccess";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        getIntentData();
        initViews();
        setupClickListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Get car booking details
            carId = intent.getStringExtra("car_id");
            carName = intent.getStringExtra("car_name");
            carPrice = intent.getStringExtra("car_price");
            carPriceRaw = intent.getDoubleExtra("car_price_raw", 0);

            // Get pickup/dropoff details
            pickupLocation = intent.getStringExtra("pickup_location");
            dropoffLocation = intent.getStringExtra("dropoff_location");
            pickupDate = intent.getStringExtra("pickup_date");
            pickupTime = intent.getStringExtra("pickup_time");
            dropoffDate = intent.getStringExtra("dropoff_date");
            dropoffTime = intent.getStringExtra("dropoff_time");

            // Get user information
            userId = intent.getStringExtra("user_id");
            userPhone = intent.getStringExtra("user_phone");
            userName = intent.getStringExtra("user_name");
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etCitizenId = findViewById(R.id.et_aadhar_number); // now CCCD
        etTaxId = findViewById(R.id.et_pan_number); // now MST
        cbTermsAccepted = findViewById(R.id.cb_terms_accepted);
        btnNextStep = findViewById(R.id.btn_next_step);
        ivBack = findViewById(R.id.iv_back);

        // Autofill user info if available
        if (userName != null && !userName.isEmpty()) {
            etFullName.setText(userName);
        }
        if (userPhone != null && !userPhone.isEmpty()) {
            etPhone.setText(userPhone);
        }
        // Optionally, autofill email or other fields if you have them
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnNextStep.setOnClickListener(v -> {
            if (validateInputs()) {
                // Show loading state
                btnNextStep.setEnabled(false);
                btnNextStep.setText("Verifying...");

                Intent intent = new Intent(UserInfoActivity.this, CheckoutActivity.class);
                // Pass booking details
                intent.putExtra("pickup_location", pickupLocation);
                intent.putExtra("dropoff_location", dropoffLocation);
                intent.putExtra("pickup_date", pickupDate);
                intent.putExtra("pickup_time", pickupTime);
                intent.putExtra("dropoff_date", dropoffDate);
                intent.putExtra("dropoff_time", dropoffTime);
                // Pass user info
                intent.putExtra("full_name", etFullName.getText().toString().trim());
                intent.putExtra("email", etEmail.getText().toString().trim());
                intent.putExtra("phone", etPhone.getText().toString().trim());
                intent.putExtra("citizen_id", etCitizenId.getText().toString().trim());
                intent.putExtra("tax_id", etTaxId.getText().toString().trim());
                // Pass user data for next activities
                intent.putExtra("user_id", userId);
                intent.putExtra("user_name", userName);
                intent.putExtra("user_phone", userPhone);
                // Pass car data (adding the missing car_id and car_name)
                intent.putExtra("car_id", carId);
                intent.putExtra("car_name", carName);
                intent.putExtra("car_price", carPrice);
                // Pass car price raw
                intent.putExtra("car_price_raw", carPriceRaw);
                startActivity(intent);

                // Log the car ID for tracking
                Log.d(TAG, "From UserInfoActivity - Passing Car ID: " + carId + ", Car Name: " + carName);

                // Reset button state
                btnNextStep.setEnabled(true);
                btnNextStep.setText("Verify");
            }
        });
    }

    private boolean validateInputs() {
        // Full Name validation
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return false;
        }
        if (fullName.length() < 2) {
            etFullName.setError("Name must be at least 2 characters");
            etFullName.requestFocus();
            return false;
        }

        // Email validation
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Phone validation
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Please enter your phone number");
            etPhone.requestFocus();
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        // Citizen ID (CCCD) validation
        String citizenId = etCitizenId.getText().toString().trim();
        if (TextUtils.isEmpty(citizenId)) {
            etCitizenId.setError("Please enter your Citizen ID number");
            etCitizenId.requestFocus();
            return false;
        }
        if (citizenId.length() != 12 || !citizenId.matches("\\d+")) {
            etCitizenId.setError("Citizen ID must be 12 digits");
            etCitizenId.requestFocus();
            return false;
        }

        // Tax ID (MST) validation
        String taxId = etTaxId.getText().toString().trim();
        if (TextUtils.isEmpty(taxId)) {
            etTaxId.setError("Please enter your Tax ID number");
            etTaxId.requestFocus();
            return false;
        }
        if (!(taxId.matches("\\d{10}") || taxId.matches("\\d{13}"))) {
            etTaxId.setError("Tax ID must be 10 or 13 digits");
            etTaxId.requestFocus();
            return false;
        }

        // Terms acceptance
        if (!cbTermsAccepted.isChecked()) {
            Toast.makeText(this, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset button state when returning to this activity
        btnNextStep.setEnabled(true);
        btnNextStep.setText("Verify");
    }
}