package com.midterm.mobiledesignfinalterm.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.midterm.mobiledesignfinalterm.R;
import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity {

    private EditText editTextAccountName;
    private EditText editTextMobileNumber;
    private EditText editTextOTP;
    private Button buttonSend; // Will be used for "Verify Account" then "Verify OTP"
    private Button buttonSendOtp; // Button to trigger sending OTP
    private ImageView imageViewBack;
    private CountDownTimer countDownTimer;

    private FirebaseAuth mAuth;
    private String verificationId; // Stores the verification ID from Firebase
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Play popup animation when the activity starts
        View rootView = findViewById(android.R.id.content);
        playPopupAnimation(rootView);

        // Make status bar transparent and content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        initializeViews();
        setupClickListeners();
        setupFirebaseAuthCallbacks(); // Setup Firebase Phone Auth callbacks
    }

    private void animateButtonClick(View button, Runnable onComplete) {
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
        editTextAccountName = findViewById(R.id.editTextAccountName);
        editTextMobileNumber = findViewById(R.id.editTextMobileNumber);
        editTextOTP = findViewById(R.id.editTextOTP);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        imageViewBack = findViewById(R.id.imageViewBack);

        // Initially hide OTP input and set main button text
        editTextOTP.setVisibility(View.GONE);
        buttonSend.setText("Verify Account");
    }

    private void setupClickListeners() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonClick(v, new Runnable() {
                    @Override
                    public void run() {
                        // If OTP field is visible, it means we are in the OTP verification phase
                        if (editTextOTP.getVisibility() == View.VISIBLE) {
                            verifyOtpCode();
                        } else {
                            // First step: Verify Account (before sending OTP)
                            // Here you'd ideally verify accountName and mobileNumber with your backend
                            // For this example, we'll assume account verification is successful
                            // and proceed to enable OTP sending.
                            handleAccountVerification();
                        }
                    }
                });
            }
        });

        buttonSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonClick(v, new Runnable() {
                    @Override
                    public void run() {
                        sendVerificationCode();
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackPress();
            }
        });
    }

    private void handleAccountVerification() {
        String accountName = editTextAccountName.getText().toString().trim();
        String mobileNumber = editTextMobileNumber.getText().toString().trim();

        if (accountName.isEmpty()) {
            editTextAccountName.setError("Account name is required");
            editTextAccountName.requestFocus();
            animateErrorShake(editTextAccountName);
            return;
        }

        if (mobileNumber.isEmpty()) {
            editTextMobileNumber.setError("Mobile number is required");
            editTextMobileNumber.requestFocus();
            animateErrorShake(editTextMobileNumber);
            return;
        }

        String phoneRegex = "^[+]?[0-9]{10,13}$";
        if (!mobileNumber.matches(phoneRegex)) {
            editTextMobileNumber.setError("Please enter a valid mobile number");
            editTextMobileNumber.requestFocus();
            animateErrorShake(editTextMobileNumber);
            return;
        }

        // In a real application, you would send accountName and mobileNumber to your backend
        // to check if the account exists and is linked to this phone number.
        // If successful, you would then call sendVerificationCode().
        // For this example, we'll directly enable OTP related fields and allow sending OTP.
        Toast.makeText(this, "Account details appear valid. You can now send OTP.", Toast.LENGTH_SHORT).show();
        editTextOTP.setVisibility(View.VISIBLE);
        buttonSend.setText("Verify OTP");
        buttonSendOtp.setEnabled(true); // Enable send OTP button
    }


    private void setupFirebaseAuthCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This method is called in two situations:
                // 1) Instant verification if the phone number is verified automatically
                // 2) If the user enters the correct code and it is verified by Firebase
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Handle verification failures
                Toast.makeText(ForgotPassword.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                buttonSendOtp.setEnabled(true); // Re-enable button on failure
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    buttonSendOtp.setText("Resend OTP");
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationIdParam,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the user's phone
                // Save the verification ID and resending token for later use.
                verificationId = verificationIdParam;
                Toast.makeText(ForgotPassword.this, "OTP sent to your mobile number.", Toast.LENGTH_LONG).show();
                editTextOTP.setVisibility(View.VISIBLE); // Show OTP input
                buttonSend.setText("Verify OTP"); // Change main button text
                startCountdown(); // Start resend countdown
                buttonSendOtp.setEnabled(false); // Disable send OTP until countdown finishes
            }
        };
    }

    private void sendVerificationCode() {
        String phoneNumber = editTextMobileNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            editTextMobileNumber.setError("Mobile number is required");
            editTextMobileNumber.requestFocus();
            animateErrorShake(editTextMobileNumber);
            return;
        }

        // Firebase requires phone numbers to be in E.164 format (e.g., +84912345678)
        // Check if it already has a '+' prefix
        if (!phoneNumber.startsWith("+")) {
            // Assuming Vietnam numbers for example, you might need to handle country codes dynamically
            phoneNumber = "+84" + phoneNumber; // Prepend country code if missing
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtpCode() {
        String code = editTextOTP.getText().toString().trim();
        if (code.isEmpty()) {
            editTextOTP.setError("Please enter OTP");
            editTextOTP.requestFocus();
            animateErrorShake(editTextOTP);
            return;
        }

        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        } else {
            Toast.makeText(this, "OTP not sent yet or session expired. Please resend.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        // FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(ForgotPassword.this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to CreateNewPassword screen
                        Intent intent = new Intent(ForgotPassword.this, CreateNewPassword.class);
                        // You can pass the mobile number and account name if needed in the next activity
                        intent.putExtra("mobile_number", editTextMobileNumber.getText().toString().trim());
                        intent.putExtra("account_name", editTextAccountName.getText().toString().trim());
                        startActivity(intent);
                        finish(); // Finish ForgotPassword activity

                    } else {
                        // Sign in failed, display a message and update the UI
                        if (task.getException() instanceof FirebaseException) {
                            Toast.makeText(ForgotPassword.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ForgotPassword.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        animateErrorShake(editTextOTP);
                    }
                });
    }


    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        buttonSendOtp.setEnabled(false); // Disable during countdown

        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds countdown
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                buttonSendOtp.setText("Resend OTP (" + secondsRemaining + "s)");
            }

            @Override
            public void onFinish() {
                buttonSendOtp.setText("Resend OTP");
                buttonSendOtp.setEnabled(true); // Re-enable after countdown
            }
        };
        countDownTimer.start();
    }

    private void handleBackPress() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackPress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}