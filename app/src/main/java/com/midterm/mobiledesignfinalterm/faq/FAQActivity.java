package com.midterm.mobiledesignfinalterm.faq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.midterm.mobiledesignfinalterm.R;

public class FAQActivity extends AppCompatActivity {

    // FAQ Views Arrays for easier management
    private CardView[] faqCards = new CardView[14];
    private TextView[] faqAnswers = new TextView[14];
    private ImageView[] faqArrows = new ImageView[14];

    private ImageView backArrow;
    private Button btnContactSupport;

    // Track currently expanded FAQ item
    private int currentlyExpandedItem = -1;
    private final int EXPAND_DURATION = 350; // Longer for smoother feel
    private final int COLLAPSE_DURATION = 250; // Slightly faster collapse
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Make status bar transparent
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Back arrow
        backArrow = findViewById(R.id.back_arrow);

        // Contact support button
        btnContactSupport = findViewById(R.id.btn_contact_support);

        // Initialize FAQ cards, answers, and arrows
        for (int i = 0; i < 14; i++) {
            int cardId = getResources().getIdentifier("faq_card_" + (i + 1), "id", getPackageName());
            int answerId = getResources().getIdentifier("faq_answer_" + (i + 1), "id", getPackageName());
            int arrowId = getResources().getIdentifier("faq_arrow_" + (i + 1), "id", getPackageName());

            faqCards[i] = findViewById(cardId);
            faqAnswers[i] = findViewById(answerId);
            faqArrows[i] = findViewById(arrowId);

            // Initialize answers properly
            if (faqAnswers[i] != null) {
                faqAnswers[i].setVisibility(View.GONE);
                faqAnswers[i].setAlpha(0f);
                faqAnswers[i].setScaleY(0f);
                faqAnswers[i].setTranslationY(-20f);
            }
        }
    }

    private void setupClickListeners() {
        // Back arrow click listener
        backArrow.setOnClickListener(v -> onBackPressed());

        // FAQ cards click listeners
        for (int i = 0; i < 14; i++) {
            final int index = i;
            if (faqCards[i] != null) {
                faqCards[i].setOnClickListener(v -> toggleFaqAnswer(index));
            }
        }

        // Contact support button click listener
        btnContactSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+840236373899"));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone app not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFaqAnswer(int index) {
        TextView answer = faqAnswers[index];
        ImageView arrow = faqArrows[index];

        if (answer == null || arrow == null || isAnimating) return;

        // If clicking on currently expanded item, collapse it
        if (currentlyExpandedItem == index) {
            collapseItemSmooth(index);
            currentlyExpandedItem = -1;
            return;
        }

        // If another item is expanded, collapse it first
        if (currentlyExpandedItem != -1) {
            collapseItemSmooth(currentlyExpandedItem);
        }

        // Expand the clicked item
        expandItemSmooth(index);
        currentlyExpandedItem = index;
    }

    private void expandItemSmooth(int index) {
        TextView answer = faqAnswers[index];
        ImageView arrow = faqArrows[index];

        isAnimating = true;

        // Enable hardware acceleration for smoother animations
        answer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        arrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Prepare the view for measurement
        answer.setVisibility(View.VISIBLE);
        answer.setAlpha(0f);
        answer.setScaleY(0f);
        answer.setTranslationY(-20f);

        // Measure target height
        answer.measure(
                View.MeasureSpec.makeMeasureSpec(((View) answer.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int targetHeight = answer.getMeasuredHeight();

        // Set initial height to 0
        answer.getLayoutParams().height = 0;
        answer.requestLayout();

        // Create smooth height animation
        ValueAnimator heightAnimator = ValueAnimator.ofInt(0, targetHeight);
        heightAnimator.setDuration(EXPAND_DURATION);
        heightAnimator.setInterpolator(new DecelerateInterpolator(1.2f));

        heightAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            answer.getLayoutParams().height = value;
            answer.requestLayout();
        });

        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                answer.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                answer.requestLayout();
                answer.setLayerType(View.LAYER_TYPE_NONE, null);
                arrow.setLayerType(View.LAYER_TYPE_NONE, null);
                isAnimating = false;
            }
        });

        // Create coordinated animations for smooth visual effect
        AnimatorSet animatorSet = new AnimatorSet();

        // Fade in animation
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(answer, "alpha", 0f, 1f);
        fadeIn.setDuration(EXPAND_DURATION);
        fadeIn.setStartDelay(100); // Start slightly after height animation
        fadeIn.setInterpolator(new DecelerateInterpolator());

        // Scale animation for smooth appearance
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(answer, "scaleY", 0f, 1f);
        scaleY.setDuration(EXPAND_DURATION);
        scaleY.setStartDelay(50);
        scaleY.setInterpolator(new DecelerateInterpolator(1.5f));

        // Slide down animation
        ObjectAnimator slideDown = ObjectAnimator.ofFloat(answer, "translationY", -20f, 0f);
        slideDown.setDuration(EXPAND_DURATION);
        slideDown.setStartDelay(80);
        slideDown.setInterpolator(new DecelerateInterpolator(1.2f));

        // Arrow rotation with smooth bounce
        ObjectAnimator arrowRotation = ObjectAnimator.ofFloat(arrow, "rotation", 0f, 180f);
        arrowRotation.setDuration(EXPAND_DURATION);
        arrowRotation.setInterpolator(new DecelerateInterpolator(1.5f));

        // Card elevation animation for depth
        ObjectAnimator cardElevation = ObjectAnimator.ofFloat(faqCards[index], "cardElevation",
                faqCards[index].getCardElevation(), faqCards[index].getCardElevation() + 4f);
        cardElevation.setDuration(EXPAND_DURATION / 2);
        cardElevation.setInterpolator(new DecelerateInterpolator());

        // Start all animations together
        animatorSet.playTogether(fadeIn, scaleY, slideDown, arrowRotation, cardElevation);
        animatorSet.start();
        heightAnimator.start();
    }

    private void collapseItemSmooth(int index) {
        TextView answer = faqAnswers[index];
        ImageView arrow = faqArrows[index];

        isAnimating = true;

        // Enable hardware acceleration
        answer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        arrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        int initialHeight = answer.getHeight();

        // Create smooth height collapse animation
        ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, 0);
        heightAnimator.setDuration(COLLAPSE_DURATION);
        heightAnimator.setInterpolator(new AccelerateInterpolator(1.2f));

        heightAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            if (answer.getLayoutParams() != null) {
                answer.getLayoutParams().height = value;
                answer.requestLayout();
            }
        });

        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                answer.setVisibility(View.GONE);
                answer.setAlpha(0f);
                answer.setScaleY(0f);
                answer.setTranslationY(-20f);
                answer.setLayerType(View.LAYER_TYPE_NONE, null);
                arrow.setLayerType(View.LAYER_TYPE_NONE, null);
                isAnimating = false;
            }
        });

        // Create coordinated collapse animations
        AnimatorSet animatorSet = new AnimatorSet();

        // Fade out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(answer, "alpha", 1f, 0f);
        fadeOut.setDuration(COLLAPSE_DURATION - 50);
        fadeOut.setInterpolator(new AccelerateInterpolator());

        // Scale animation for smooth disappearance
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(answer, "scaleY", 1f, 0f);
        scaleY.setDuration(COLLAPSE_DURATION);
        scaleY.setInterpolator(new AccelerateInterpolator(1.5f));

        // Slide up animation
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(answer, "translationY", 0f, -20f);
        slideUp.setDuration(COLLAPSE_DURATION);
        slideUp.setInterpolator(new AccelerateInterpolator(1.2f));

        // Arrow rotation back to original position
        ObjectAnimator arrowRotation = ObjectAnimator.ofFloat(arrow, "rotation", 180f, 0f);
        arrowRotation.setDuration(COLLAPSE_DURATION);
        arrowRotation.setInterpolator(new AccelerateInterpolator(1.2f));

        // Card elevation back to normal
        ObjectAnimator cardElevation = ObjectAnimator.ofFloat(faqCards[index], "cardElevation",
                faqCards[index].getCardElevation(), faqCards[index].getCardElevation() - 4f);
        cardElevation.setDuration(COLLAPSE_DURATION / 2);
        cardElevation.setInterpolator(new AccelerateInterpolator());

        // Start all animations together
        animatorSet.playTogether(fadeOut, scaleY, slideUp, arrowRotation, cardElevation);
        animatorSet.start();
        heightAnimator.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}