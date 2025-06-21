package com.midterm.mobiledesignfinalterm.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.midterm.mobiledesignfinalterm.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateNewPassword extends AppCompatActivity {

    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonCreatePassword;
    private ImageView imageViewBack;

    private String accountName;
    private String mobileNumber;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View rootView = findViewById(android.R.id.content);
        playPopupAnimation(rootView);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        // Make status bar transparent and content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        // Get data from previous activity
        Intent intent = getIntent();
        accountName = intent.getStringExtra("account_name");
        mobileNumber = intent.getStringExtra("mobile_number");
        userId = intent.getStringExtra("user_id");

        initializeViews();
        setupClickListeners();
    }

    private void animateButtonClick(View button, Runnable onComplete) {
        // Scale animation for button press feedback
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(70)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(onComplete)
                                .start();
                    }
                })
                .start();
    }

    private void animateErrorShake(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    private void playPopupAnimation(View view) {
        Animation popupAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_animation);
        view.startAnimation(popupAnimation);
    }

    private void initializeViews() {
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonCreatePassword = findViewById(R.id.buttonCreatePassword);
        imageViewBack = findViewById(R.id.imageViewBack);
    }

    private void setupClickListeners() {
        buttonCreatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonClick(v, new Runnable() {
                    @Override
                    public void run() {
                        handleCreateNewPassword();
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void handleCreateNewPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate new password
        if (newPassword.isEmpty()) {
            editTextNewPassword.setError("New password is required");
            editTextNewPassword.requestFocus();
            animateErrorShake(editTextNewPassword);
            return;
        }

        // Password strength validation
        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters long");
            editTextNewPassword.requestFocus();
            animateErrorShake(editTextNewPassword);
            return;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Please confirm your password");
            editTextConfirmPassword.requestFocus();
            animateErrorShake(editTextConfirmPassword);
            return;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            animateErrorShake(editTextConfirmPassword);
            return;
        }

        // Send new password to API
        updatePasswordWithAPI(accountName, mobileNumber, newPassword, userId);
    }

    private void updatePasswordWithAPI(String username, String phoneNumber, String newPassword, String userId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/myapi/update_password.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Set connection properties
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000); // 10 seconds
                conn.setReadTimeout(10000); // 10 seconds

                // Create JSON payload
                JSONObject jsonInput = new JSONObject();
                try {
                    jsonInput.put("username", username);
                    jsonInput.put("phone_number", phoneNumber);
                    jsonInput.put("new_password", newPassword);
                    if (userId != null && !userId.isEmpty()) {
                        jsonInput.put("user_id", userId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error creating request data", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String jsonInputString = jsonInput.toString();

                // Debug: Print what we're sending
                System.out.println("Update Password - Sending JSON: " + jsonInputString);

                // Send data
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                // Check response code
                int responseCode = conn.getResponseCode();
                System.out.println("Update Password - Response Code: " + responseCode);

                // Read response
                BufferedReader br;
                if (responseCode >= 200 && responseCode < 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                }

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();

                // Debug: Print the full response
                System.out.println("Update Password - Raw API Response: " + response.toString());

                // Parse JSON result
                try {
                    JSONObject result = new JSONObject(response.toString());

                    runOnUiThread(() -> {
                        try {
                            if (result.getBoolean("success")) {
                                // Password update successful
                                String message = result.optString("message", "Password updated successfully!");
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                                // Navigate back to login screen or show success message
                                Intent intent = new Intent(this, Login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                String errorMessage = result.optString("error", "Failed to update password");
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

                                // Debug: Print additional error info if available
                                if (result.has("received_fields")) {
                                    System.out.println("Fields received by server: " + result.get("received_fields"));
                                }
                                if (result.has("received_data")) {
                                    System.out.println("Data received by server: " + result.get("received_data"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String errorMsg = "Invalid server response: " + e.getMessage();
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            System.out.println("JSON Parsing Error: " + e.getMessage());
                            System.out.println("Raw response that caused error: " + response.toString());
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Server returned invalid response", Toast.LENGTH_LONG).show();
                        System.out.println("JSON Parse Error: " + e.getMessage());
                        System.out.println("Raw response: " + response.toString());
                    });
                }

            } catch (java.net.ConnectException e) {
                System.out.println("Connection refused: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cannot connect to server. Please check if your local server is running.", Toast.LENGTH_LONG).show();
                });
            } catch (java.net.UnknownHostException e) {
                System.out.println("Unknown host: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cannot resolve server address. Check your network connection.", Toast.LENGTH_LONG).show();
                });
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("Connection timeout: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Connection timeout. Server might be slow or unreachable.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    String networkError = "Network error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    Toast.makeText(this, networkError, Toast.LENGTH_LONG).show();
                    System.out.println("Network Error Details: " + e.getMessage());
                });
            }
        }).start();
    }

    // Example of how to call this method from your button click handler
    private void handleUpdatePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            animateErrorShake(editTextConfirmPassword);
            return;
        }

        // Validate password length
        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters long");
            editTextNewPassword.requestFocus();
            animateErrorShake(editTextNewPassword);
            return;
        }

        // Get the username and phone number from the intent extras
        String username = getIntent().getStringExtra("account_name");
        String phoneNumber = getIntent().getStringExtra("mobile_number");
        String userId = getIntent().getStringExtra("user_id");

        if (username == null || phoneNumber == null) {
            Toast.makeText(this, "Missing account information. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Call the API to update password
        updatePasswordWithAPI(username, phoneNumber, newPassword, userId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}