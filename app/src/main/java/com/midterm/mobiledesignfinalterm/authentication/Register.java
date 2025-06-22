package com.midterm.mobiledesignfinalterm.authentication;

import android.content.Intent;
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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.midterm.mobiledesignfinalterm.R;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        View rootView = findViewById(android.R.id.content);
        playPopupAnimation(rootView);

        initializeViews();
        setupClickListeners();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
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
        btnBack.setOnClickListener(v -> animateButtonClick(v, this::finish));
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
        buttonRegister.setText("Registering...");

        // 1. Check if the phone number already exists in the "users" collection
        db.collection("users").whereEqualTo("phone_number", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // User with this phone number already exists
                            editTextPhoneNumber.setError("This phone number is already registered.");
                            editTextPhoneNumber.requestFocus();
                            Toast.makeText(Register.this, "This phone number is already registered.", Toast.LENGTH_LONG).show();
                            buttonRegister.setEnabled(true);
                            buttonRegister.setText("Register");
                        } else {
                            // Phone number is unique, proceed with creating the new user
                            registerNewUserInFirestore(name, phoneNumber, password);
                        }
                    } else {
                        // An error occurred while checking for the user
                        Log.e("RegisterActivity", "Error checking for existing user.", task.getException());
                        Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Register");
                    }
                });
    }

    private void registerNewUserInFirestore(String name, String phoneNumber, String password) {
        // 2. Encrypt the password using BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // 3. Create a user object to save to Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("username", name);
        user.put("phone_number", phoneNumber);
        user.put("password_hash", hashedPassword);
        user.put("role_id", 1L); // Assign default role "renter" (ID 1)
        user.put("created_at", FieldValue.serverTimestamp());

        // 4. Add the new user to the "users" collection
        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Register");
                    Toast.makeText(Register.this, "Registration successful! Welcome " + name, Toast.LENGTH_LONG).show();

                    // Navigate to the Login screen after successful registration
                    Intent intent = new Intent(Register.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close the Register activity
                })
                .addOnFailureListener(e -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Register");
                    Log.e("RegisterActivity", "Error adding user to Firestore", e);
                    Toast.makeText(Register.this, "Registration failed due to a database error.", Toast.LENGTH_LONG).show();
                });
    }

    private void handleLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }

    public void onTermsTextClicked(View view) {
        checkBoxTerms.setChecked(!checkBoxTerms.isChecked());
    }
}