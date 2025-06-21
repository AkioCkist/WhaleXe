package com.midterm.mobiledesignfinalterm.aboutUs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.midterm.mobiledesignfinalterm.R;

public class AboutUs extends AppCompatActivity {

    private ImageView logoImage;
    private TextView tagline1, tagline2;
    private CardView missionCard, visionCard, statsCard, contactCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initializeViews();
        setupClickListeners();
        startEntryAnimations();
    }

    private void initializeViews() {
        logoImage = findViewById(R.id.logoImage);
        tagline1 = findViewById(R.id.tagline1);
        tagline2 = findViewById(R.id.tagline2);
        missionCard = findViewById(R.id.missionCard);
        visionCard = findViewById(R.id.visionCard);
        statsCard = findViewById(R.id.statsCard);
        contactCard = findViewById(R.id.contactCard);

        // Initialize back arrow
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void startEntryAnimations() {
        logoImage.setScaleX(0.5f);
        logoImage.setScaleY(0.5f);
        logoImage.setAlpha(0f);

        tagline1.setAlpha(0f);
        tagline2.setAlpha(0f);

        final View[] cardViews = new View[]{missionCard, visionCard, statsCard, contactCard};
        for (View card : cardViews) {
            card.setTranslationY(500f);
            card.setAlpha(0f);
        }

        // Logo Animator: scales and fades in
        PropertyValuesHolder logoScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f);
        PropertyValuesHolder logoScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f);
        PropertyValuesHolder logoAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f);
        ObjectAnimator logoAnimator = ObjectAnimator.ofPropertyValuesHolder(logoImage, logoScaleX, logoScaleY, logoAlpha);
        logoAnimator.setDuration(600);
        logoAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator tagline1Animator = ObjectAnimator.ofFloat(tagline1, "alpha", 0f, 1f);
        tagline1Animator.setDuration(500);

        ObjectAnimator tagline2Animator = ObjectAnimator.ofFloat(tagline2, "alpha", 0f, 1f);
        tagline2Animator.setDuration(500);

        // --- Animation Orchestration with AnimatorSet ---
        AnimatorSet heroTextAnimatorSet = new AnimatorSet();
        heroTextAnimatorSet.playSequentially(tagline1Animator, tagline2Animator);

        AnimatorSet heroSectionAnimatorSet = new AnimatorSet();
        heroSectionAnimatorSet.playTogether(logoAnimator, heroTextAnimatorSet);

        AnimatorSet cardAnimatorSet = new AnimatorSet();
        Animator[] cardAnimators = new Animator[cardViews.length];
        long delay = 0;
        for (int i = 0; i < cardViews.length; i++) {
            PropertyValuesHolder cardTranslateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f);
            PropertyValuesHolder cardAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f);
            ObjectAnimator cardAnimator = ObjectAnimator.ofPropertyValuesHolder(cardViews[i], cardTranslateY, cardAlpha);
            cardAnimator.setDuration(700);
            cardAnimator.setStartDelay(delay);
            cardAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            cardAnimators[i] = cardAnimator;
            delay += 150;
        }
        cardAnimatorSet.playTogether(cardAnimators);

        // --- Final Sequence ---
        AnimatorSet finalAnimatorSet = new AnimatorSet();
        finalAnimatorSet.playSequentially(heroSectionAnimatorSet, cardAnimatorSet);
        finalAnimatorSet.start();
    }

    private void setupClickListeners() {
        setupContactClickListeners();
    }

    private void setupContactClickListeners() {
        findViewById(R.id.phoneSection).setOnClickListener(v -> makePhoneCall("+8402363738399"));
        findViewById(R.id.emailSection).setOnClickListener(v -> sendEmail("contact@whalexe.com"));
        findViewById(R.id.addressSection).setOnClickListener(v -> openMap("156A Le Loi, Hai Chau, Da Nang, Vietnam"));
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to make phone call", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String emailAddress) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about Whale Xe");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Whale Xe team,\n\nI would like to inquire about...");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMap(String address) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                mapIntent.setPackage(null);
                startActivity(mapIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "No map app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static void startActivity(android.content.Context context) {
        Intent intent = new Intent(context, AboutUs.class);
        context.startActivity(intent);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).overridePendingTransition(
                    R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}

