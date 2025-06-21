package com.midterm.mobiledesignfinalterm;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.os.Handler;
import android.os.Looper;
import com.midterm.mobiledesignfinalterm.authentication.Login;

public class MainActivity extends AppCompatActivity {

    private Button buttonGoToLogin;
    private View rootLayout;
    private boolean isFromIntroduction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if coming from introduction
        isFromIntroduction = getIntent().getBooleanExtra("from_introduction", false);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();

        // Apply entrance animation if coming from introduction
        if (isFromIntroduction) {
            applyEntranceAnimation();
        }
    }

    private void initializeViews() {
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);
        rootLayout = findViewById(android.R.id.content); // Get the root layout
    }

    private void setupClickListeners() {
        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add click animation
                animateButtonClick(v, new Runnable() {
                    @Override
                    public void run() {
                        navigateToLogin();
                    }
                });
            }
        });
    }

    private void applyEntranceAnimation() {
        // Fade in animation for the entire activity
        rootLayout.setAlpha(0f);
        rootLayout.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(100)
                .start();

        // Slide up animation for the button
        if (buttonGoToLogin != null) {
            buttonGoToLogin.setTranslationY(100f);
            buttonGoToLogin.setAlpha(0f);
            buttonGoToLogin.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(300)
                    .start();
        }
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

    private void navigateToLogin() {
        // Create exit animation before navigating
        rootLayout.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Navigate to Login activity
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);

                        // Custom transition animation
                        overridePendingTransition(
                                android.R.anim.fade_in,  // Enter animation for new activity
                                android.R.anim.fade_out  // Exit animation for current activity
                        );

                        // Optional: finish this activity if you don't want user to come back
                        // finish();
                    }
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        // Add smooth back animation
        super.onBackPressed();
        rootLayout.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.super.onBackPressed();
                        overridePendingTransition(
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                        );
                    }
                })
                .start();
    }
}