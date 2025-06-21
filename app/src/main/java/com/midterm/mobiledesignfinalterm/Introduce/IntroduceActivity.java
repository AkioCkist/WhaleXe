package com.midterm.mobiledesignfinalterm.Introduce;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.midterm.mobiledesignfinalterm.MainActivity;
import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.authentication.Login;

public class IntroduceActivity extends AppCompatActivity {

    // UI Components
    private ImageView introduceImage;
    private TextView tvTitle, tvSkip, tvSubtitle;
    private Button btnBack, btnNext, btnSignIn;
    private View indicator1, indicator2, indicator3;

    // Data
    private int currentPosition = 0;
    private final int totalPages = 3;
    private boolean isAnimating = false;

    // Image resources
    private final int[] imageResources = {
            R.drawable.intro_bg_1,
            R.drawable.intro_bg_2,
            R.drawable.intro_bg_3
    };

    // Inspiring quotes for Whale Xe rental car project
    private final String[] titleResources = {
            "Journey begins with\na single key",
            "Freedom is just\na ride away",
            "Your adventure\nawaits on wheels"
    };

    // Subtitle resources for additional context
    private final String[] subtitleResources = {
            "Discover endless possibilities with Whale Xe",
            "Unlock new destinations, create memories",
            "Choose from thousands of premium vehicles"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce);

        initViews();
        setupButtons();
        updateUI(0);

        // Add entrance animation
        startEntranceAnimation();
    }

    private void initViews() {
        introduceImage = findViewById(R.id.introduce_image);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvSkip = findViewById(R.id.tv_skip);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        btnSignIn = findViewById(R.id.btn_sign_in);
        indicator1 = findViewById(R.id.indicator_1);
        indicator2 = findViewById(R.id.indicator_2);
        indicator3 = findViewById(R.id.indicator_3);
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPosition < totalPages - 1 && !isAnimating) {
                goToNextPage();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentPosition > 0 && !isAnimating) {
                goToPreviousPage();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add button press animation
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                navigateToMain();
                                            }
                                        })
                                        .start();
                            }
                        })
                        .start();
            }
        });

        tvSkip.setOnClickListener(v -> {
            if (!isAnimating) {
                startExitAnimation(() -> {
                    Intent intent = new Intent(IntroduceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void startEntranceAnimation() {
        // Initial setup - make elements invisible
        introduceImage.setAlpha(0f);
        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(50f);
        tvSubtitle.setAlpha(0f);
        tvSubtitle.setTranslationY(30f);
        tvSkip.setAlpha(0f);
        tvSkip.setScaleX(0f);
        tvSkip.setScaleY(0f);

        // Animate image fade in with scale
        ObjectAnimator imageAlpha = ObjectAnimator.ofFloat(introduceImage, "alpha", 0f, 1f);
        ObjectAnimator imageScale = ObjectAnimator.ofFloat(introduceImage, "scaleX", 1.1f, 1f);
        ObjectAnimator imageScaleY = ObjectAnimator.ofFloat(introduceImage, "scaleY", 1.1f, 1f);

        AnimatorSet imageSet = new AnimatorSet();
        imageSet.playTogether(imageAlpha, imageScale, imageScaleY);
        imageSet.setDuration(800);
        imageSet.setInterpolator(new DecelerateInterpolator());

        // Animate title slide up and fade in
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);
        ObjectAnimator titleTranslation = ObjectAnimator.ofFloat(tvTitle, "translationY", 50f, 0f);

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleAlpha, titleTranslation);
        titleSet.setDuration(600);
        titleSet.setStartDelay(300);
        titleSet.setInterpolator(new OvershootInterpolator(1.2f));

        // Animate subtitle
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);
        ObjectAnimator subtitleTranslation = ObjectAnimator.ofFloat(tvSubtitle, "translationY", 30f, 0f);

        AnimatorSet subtitleSet = new AnimatorSet();
        subtitleSet.playTogether(subtitleAlpha, subtitleTranslation);
        subtitleSet.setDuration(500);
        subtitleSet.setStartDelay(500);
        subtitleSet.setInterpolator(new DecelerateInterpolator());

        // Animate skip button scale and fade in
        ObjectAnimator skipAlpha = ObjectAnimator.ofFloat(tvSkip, "alpha", 0f, 1f);
        ObjectAnimator skipScaleX = ObjectAnimator.ofFloat(tvSkip, "scaleX", 0f, 1f);
        ObjectAnimator skipScaleY = ObjectAnimator.ofFloat(tvSkip, "scaleY", 0f, 1f);

        AnimatorSet skipSet = new AnimatorSet();
        skipSet.playTogether(skipAlpha, skipScaleX, skipScaleY);
        skipSet.setDuration(400);
        skipSet.setStartDelay(600);
        skipSet.setInterpolator(new OvershootInterpolator(1.5f));

        // Start all animations
        imageSet.start();
        titleSet.start();
        subtitleSet.start();
        skipSet.start();
    }

    private void goToNextPage() {
        currentPosition++;
        animateToPosition(true);
    }

    private void goToPreviousPage() {
        currentPosition--;
        animateToPosition(false);
    }

    private void animateToPosition(boolean isNext) {
        if (isAnimating) return;
        isAnimating = true;

        // Disable buttons during animation
        setButtonsEnabled(false);

        // Create seamless transition with multiple effects
        createSeamlessTransition(isNext);
    }

    private void createSeamlessTransition(boolean isNext) {
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float direction = isNext ? -1 : 1;

        // Create overlay image for seamless transition
        ImageView overlayImage = new ImageView(this);
        overlayImage.setLayoutParams(introduceImage.getLayoutParams());
        overlayImage.setImageResource(imageResources[currentPosition]);
        overlayImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        overlayImage.setTranslationX(screenWidth * direction * -1);

        // Add overlay to parent
        ((androidx.constraintlayout.widget.ConstraintLayout) findViewById(R.id.main)).addView(overlayImage);

        // Animate current image out
        ObjectAnimator currentImageOut = ObjectAnimator.ofFloat(introduceImage, "translationX", 0f, screenWidth * direction);
        currentImageOut.setDuration(500);
        currentImageOut.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate overlay image in
        ObjectAnimator overlayImageIn = ObjectAnimator.ofFloat(overlayImage, "translationX", screenWidth * direction * -1, 0f);
        overlayImageIn.setDuration(500);
        overlayImageIn.setInterpolator(new AccelerateDecelerateInterpolator());

        // Title animation with 3D effect
        ObjectAnimator titleFadeOut = ObjectAnimator.ofFloat(tvTitle, "alpha", 1f, 0f);
        ObjectAnimator titleScaleOut = ObjectAnimator.ofFloat(tvTitle, "scaleX", 1f, 0.8f);
        ObjectAnimator titleScaleOutY = ObjectAnimator.ofFloat(tvTitle, "scaleY", 1f, 0.8f);
        ObjectAnimator titleRotation = ObjectAnimator.ofFloat(tvTitle, "rotationY", 0f, 90f);

        // Subtitle animation
        ObjectAnimator subtitleFadeOut = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 1f, 0f);
        ObjectAnimator subtitleTranslateOut = ObjectAnimator.ofFloat(tvSubtitle, "translationY", 0f, 20f);

        AnimatorSet titleOutSet = new AnimatorSet();
        titleOutSet.playTogether(titleFadeOut, titleScaleOut, titleScaleOutY, titleRotation, subtitleFadeOut, subtitleTranslateOut);
        titleOutSet.setDuration(250);

        // Parallax effect for background
        ObjectAnimator parallaxEffect = ObjectAnimator.ofFloat(introduceImage, "scaleX", 1f, 1.1f);
        ObjectAnimator parallaxEffectY = ObjectAnimator.ofFloat(introduceImage, "scaleY", 1f, 1.1f);

        AnimatorSet parallaxSet = new AnimatorSet();
        parallaxSet.playTogether(parallaxEffect, parallaxEffectY);
        parallaxSet.setDuration(500);

        // Start animations
        AnimatorSet mainSet = new AnimatorSet();
        mainSet.playTogether(currentImageOut, overlayImageIn, parallaxSet);
        titleOutSet.start();

        mainSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Replace current image with overlay
                introduceImage.setImageResource(imageResources[currentPosition]);
                introduceImage.setTranslationX(0f);
                introduceImage.setScaleX(1f);
                introduceImage.setScaleY(1f);

                // Remove overlay
                ((androidx.constraintlayout.widget.ConstraintLayout) findViewById(R.id.main)).removeView(overlayImage);

                // Animate title in with new content
                animateTitleIn();

                // Update page indicators
                updatePageIndicators();

                // Update UI
                updateUI(currentPosition);

                // Re-enable buttons
                new Handler().postDelayed(() -> {
                    setButtonsEnabled(true);
                    isAnimating = false;
                }, 300);
            }
        });

        mainSet.start();
    }

    private void animateTitleIn() {
        // Update title and subtitle text
        tvTitle.setText(titleResources[currentPosition]);
        tvSubtitle.setText(subtitleResources[currentPosition]);

        // Animate title in with 3D effect
        ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);
        ObjectAnimator titleScaleIn = ObjectAnimator.ofFloat(tvTitle, "scaleX", 0.8f, 1f);
        ObjectAnimator titleScaleInY = ObjectAnimator.ofFloat(tvTitle, "scaleY", 0.8f, 1f);
        ObjectAnimator titleRotation = ObjectAnimator.ofFloat(tvTitle, "rotationY", -90f, 0f);

        // Add bounce effect
        ObjectAnimator titleBounce = ObjectAnimator.ofFloat(tvTitle, "translationY", -30f, 0f);

        // Animate subtitle
        ObjectAnimator subtitleFadeIn = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);
        ObjectAnimator subtitleTranslateIn = ObjectAnimator.ofFloat(tvSubtitle, "translationY", 20f, 0f);

        AnimatorSet titleInSet = new AnimatorSet();
        titleInSet.playTogether(titleFadeIn, titleScaleIn, titleScaleInY, titleRotation, titleBounce);
        titleInSet.setDuration(400);
        titleInSet.setStartDelay(200);
        titleInSet.setInterpolator(new OvershootInterpolator(1.2f));

        AnimatorSet subtitleInSet = new AnimatorSet();
        subtitleInSet.playTogether(subtitleFadeIn, subtitleTranslateIn);
        subtitleInSet.setDuration(350);
        subtitleInSet.setStartDelay(350);
        subtitleInSet.setInterpolator(new DecelerateInterpolator());

        titleInSet.start();
        subtitleInSet.start();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnNext.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        btnSignIn.setEnabled(enabled);
        tvSkip.setEnabled(enabled);

        // Visual feedback
        float alpha = enabled ? 1f : 0.7f;
        btnNext.setAlpha(alpha);
        btnBack.setAlpha(alpha);
        btnSignIn.setAlpha(alpha);
        tvSkip.setAlpha(enabled ? 1f : 0.8f);
    }

    private void startExitAnimation(Runnable onComplete) {
        isAnimating = true;
        setButtonsEnabled(false);

        // Create exit animation with scale and fade
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(introduceImage, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(introduceImage, "scaleY", 1f, 1.2f);
        ObjectAnimator fade = ObjectAnimator.ofFloat(introduceImage, "alpha", 1f, 0f);

        ObjectAnimator titleFade = ObjectAnimator.ofFloat(tvTitle, "alpha", 1f, 0f);
        ObjectAnimator titleScale = ObjectAnimator.ofFloat(tvTitle, "scaleX", 1f, 0.5f);
        ObjectAnimator titleScaleY = ObjectAnimator.ofFloat(tvTitle, "scaleY", 1f, 0.5f);

        AnimatorSet exitSet = new AnimatorSet();
        exitSet.playTogether(scaleX, scaleY, fade, titleFade, titleScale, titleScaleY);
        exitSet.setDuration(600);
        exitSet.setInterpolator(new AccelerateDecelerateInterpolator());

        exitSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                onComplete.run();
            }
        });

        exitSet.start();
    }

    private void updateUI(int position) {
        if (position == 0) {
            // First page - only show Next
            btnBack.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);

            // Animate button appearance
            animateButtonAppearance(btnNext);

        } else if (position == totalPages - 1) {
            // Last page - show Sign In and Back
            btnBack.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);

            // Animate buttons appearance
            animateButtonAppearance(btnBack);
            animateButtonAppearance(btnSignIn);

        } else {
            // Middle pages - show Back and Next
            btnBack.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);

            // Animate buttons appearance
            animateButtonAppearance(btnBack);
            animateButtonAppearance(btnNext);
        }
    }

    private void animateButtonAppearance(Button button) {
        button.setAlpha(0f);
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1f);

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playTogether(alpha, scaleX, scaleY);
        buttonSet.setDuration(300);
        buttonSet.setInterpolator(new OvershootInterpolator(1.3f));
        buttonSet.start();
    }

    private void updatePageIndicators() {
        View[] indicators = {indicator1, indicator2, indicator3};

        for (int i = 0; i < indicators.length; i++) {
            ObjectAnimator scaleX, scaleY, alpha;

            if (i == currentPosition) {
                // Active indicator
                indicators[i].setBackgroundResource(R.drawable.page_indicator_active);
                scaleX = ObjectAnimator.ofFloat(indicators[i], "scaleX", 0.8f, 1.2f);
                scaleY = ObjectAnimator.ofFloat(indicators[i], "scaleY", 0.8f, 1.2f);
                alpha = ObjectAnimator.ofFloat(indicators[i], "alpha", 0.7f, 1f);
            } else {
                // Inactive indicator
                indicators[i].setBackgroundResource(R.drawable.page_indicator_inactive);
                scaleX = ObjectAnimator.ofFloat(indicators[i], "scaleX", 1.2f, 1f);
                scaleY = ObjectAnimator.ofFloat(indicators[i], "scaleY", 1.2f, 1f);
                alpha = ObjectAnimator.ofFloat(indicators[i], "alpha", 1f, 0.7f);
            }

            AnimatorSet indicatorSet = new AnimatorSet();
            indicatorSet.playTogether(scaleX, scaleY, alpha);
            indicatorSet.setDuration(200);
            indicatorSet.setInterpolator(new DecelerateInterpolator());
            indicatorSet.start();
        }
    }
    private void navigateToMain() {
        // Get references to all UI elements
        TextView titleText = findViewById(R.id.tv_title);
        TextView subtitleText = findViewById(R.id.tv_subtitle);
        TextView skipButton = findViewById(R.id.tv_skip);
        LinearLayout pageIndicator = findViewById(R.id.ll_page_indicator);
        LinearLayout buttonContainer = findViewById(R.id.ll_buttons);
        View rootView = findViewById(android.R.id.content);

        // Animate skip button - fade out and slide up
        if (skipButton != null) {
            skipButton.animate()
                    .alpha(0f)
                    .translationY(-30f)
                    .setDuration(250)
                    .start();
        }

        // Animate title text - fade out and slide up
        if (titleText != null) {
            titleText.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(300)
                    .setStartDelay(50)
                    .start();
        }

        // Animate subtitle text - fade out and slide down
        if (subtitleText != null) {
            subtitleText.animate()
                    .alpha(0f)
                    .translationY(30f)
                    .setDuration(350)
                    .setStartDelay(100)
                    .start();
        }

        // Animate page indicators - fade out and scale down
        if (pageIndicator != null) {
            pageIndicator.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .setStartDelay(150)
                    .start();
        }

        // Animate button container - slide down and fade out
        if (buttonContainer != null) {
            buttonContainer.animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(350)
                    .setStartDelay(100)
                    .start();
        }

        // Create smooth exit animation for the entire screen
        rootView.animate()
                .alpha(0.3f)  // Don't fade completely to 0 since elements are already fading
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(400)
                .setStartDelay(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Navigate to MainActivity
                        Intent intent = new Intent(IntroduceActivity.this, MainActivity.class);
                        intent.putExtra("from_introduction", true);
                        startActivity(intent);

                        // Custom transition animation
                        overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                        );

                        finish();
                    }
                })
                .start();
    }
    private void animatePageTransition() {
        TextView titleText = findViewById(R.id.tv_title);
        TextView subtitleText = findViewById(R.id.tv_subtitle);

        // Animate out current text
        if (titleText != null) {
            titleText.animate()
                    .alpha(0f)
                    .translationX(-100f)
                    .setDuration(200)
                    .start();
        }

        if (subtitleText != null) {
            subtitleText.animate()
                    .alpha(0f)
                    .translationX(-100f)
                    .setDuration(200)
                    .setStartDelay(50)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            // Update content here (change text, image, etc.)
                            updatePageContent();

                            // Animate in new text
                            titleText.setTranslationX(100f);
                            subtitleText.setTranslationX(100f);

                            titleText.animate()
                                    .alpha(1f)
                                    .translationX(0f)
                                    .setDuration(300)
                                    .setStartDelay(100)
                                    .start();

                            subtitleText.animate()
                                    .alpha(1f)
                                    .translationX(0f)
                                    .setDuration(300)
                                    .setStartDelay(150)
                                    .start();
                        }
                    })
                    .start();
        }
    }
    private void updatePageContent() {
        // Update your text, images, and page indicators here
        // This is where you'd change the content for different introduction pages
    }
}