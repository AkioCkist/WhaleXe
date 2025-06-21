package com.midterm.mobiledesignfinalterm.authentication;

import android.content.Intent; // Import for starting a new activity
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.midterm.mobiledesignfinalterm.R;
// Assuming you have a LoginActivity or similar to navigate to after login
// import com.midterm.mobiledesignfinalterm.activity.LoginActivity; // Example: Your login activity

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextPhoneNumber;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private CheckBox checkBoxTerms;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ImageView btnBack;

    private static final String PHONE_NUMBER_PATTERN = "^[0-9]{10,15}$";
    // Use 10.0.2.2 for Android Emulator to access localhost of the host machine
    private static final String REGISTER_API_URL = "http://10.0.2.2/myapi/register.php";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false); // Assuming dark icons for light status bar

        View rootView = findViewById(android.R.id.content);
        playPopupAnimation(rootView);

        initializeViews();
        setupClickListeners();

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);
    }

    private void playPopupAnimation(View view) {
        Animation popupAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_animation);
        view.startAnimation(popupAnimation);
    }

    private void animateButtonClick(View button, Runnable onComplete) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(70)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction(onComplete)
                        .start())
                .start();
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> animateButtonClick(v, this::handleRegister));

        textViewLogin.setOnClickListener(v -> handleLogin());

        btnBack.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                // Handle back button click - finish this activity to go back to previous screen
                finish();
            });
        });
    }

    private void handleRegister() {
        String name = editTextName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("Full Name is required");
            editTextName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Phone number is required");
            editTextPhoneNumber.requestFocus();
            return;
        }

        Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            editTextPhoneNumber.setError("Please enter a valid phone number (10-15 digits)");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Please confirm your password");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        buttonRegister.setEnabled(false);

        // Create JSON object for the request body
        JSONObject postData = new JSONObject();
        try {
            postData.put("username", name);
            postData.put("phone_number", phoneNumber);
            postData.put("password", password);
        } catch (JSONException e) {
            Log.e("RegisterActivity", "JSONException: " + e.getMessage());
            Toast.makeText(this, "Error creating request data.", Toast.LENGTH_SHORT).show();
            buttonRegister.setEnabled(true); // Re-enable button
            return;
        }

        // Create Volley JSON Object Request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, REGISTER_API_URL, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        buttonRegister.setEnabled(true); // Re-enable button
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONObject user = response.getJSONObject("user");
                                String accountId = user.getString("account_id");
                                String username = user.getString("username");
                                String returnedPhoneNumber = user.getString("phone_number");

                                Toast.makeText(Register.this, "Registration successful! Welcome " + username, Toast.LENGTH_LONG).show();

                                // TODO: Navigate to your main app screen or login screen
                                // For example, navigate to LoginActivity and finish this one
                                // Intent intent = new Intent(Register.this, LoginActivity.class);
                                // startActivity(intent);
                                finish(); // Close Register activity

                            } else {
                                String errorMsg = response.optString("error", "Registration failed. Please try again.");
                                Toast.makeText(Register.this, errorMsg, Toast.LENGTH_LONG).show();
                                if (errorMsg.toLowerCase().contains("phone number already exists")) {
                                    editTextPhoneNumber.setError("This phone number is already registered.");
                                    editTextPhoneNumber.requestFocus();
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("RegisterActivity", "JSONException onResponse: " + e.getMessage());
                            Toast.makeText(Register.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        buttonRegister.setEnabled(true); // Re-enable button
                        Log.e("RegisterActivity", "VolleyError: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("RegisterActivity", "VolleyError Status Code: " + error.networkResponse.statusCode);
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                String message = data.optString("error", "Unknown server error");
                                Toast.makeText(Register.this, "Server Error: " + message, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(Register.this, "Server Error: Could not parse error response.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Register.this, "Registration failed. Check your network connection.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void handleLogin() {
        // If you have a LoginActivity, start it. Otherwise, just finish this activity.
         Intent intent = new Intent(Register.this, Login.class);
         startActivity(intent);
        finish(); // Close the register activity
    }

    public void onTermsTextClicked(View view) {
        checkBoxTerms.setChecked(!checkBoxTerms.isChecked());
    }
}

