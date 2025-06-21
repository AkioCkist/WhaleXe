package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.midterm.mobiledesignfinalterm.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends AppCompatActivity {

    // Firebase Firestore instance
    private FirebaseFirestore db;

    // User information fields
    private String userName;
    private String userPhone;
    private String userId;
    private ArrayList<String> userRoles;
    private String userRawData;
    private ImageView profile_image;
    private String userEmail;
    private String userPhotoUri;
    private int previousSelectedItemId = -1;

    // UI Components
    private ConstraintLayout profileSection;
    private BottomNavigationView bottomNav;

    // Declare these at the activity level if they are used by fragments to update activity's view
    private TextView userNameView;
    private TextView userPhoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get user information from intent
        getUserInfoFromIntent();

        // Initialize the bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Initialize back button
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            onBackPressed(); // Go back to previous activity
        });

        // Initialize user profile section views (these are in the Activity's layout, not the fragment's)
        profileSection = findViewById(R.id.profile_section);
        userNameView = findViewById(R.id.user_name); // Keep these references
        userPhoneView = findViewById(R.id.user_phone); // Keep these references
        TextView userRoleView = findViewById(R.id.user_role);
        TextView userIdView = findViewById(R.id.user_id);
        profile_image = findViewById(R.id.profile_image);
        // Load user data from received intent
        loadUserData(userNameView, userPhoneView, userRoleView, userIdView);

        // Animate the screen on entry
        animateScreenEntry();

        // Set the default fragment without animation initially
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DashboardFragment()).commit();
            previousSelectedItemId = R.id.nav_dashboard;
        }

        handleUserIntent();
    }

    private void animateScreenEntry() {
        // Hide views initially to prepare for animation
        profileSection.setVisibility(View.INVISIBLE);
        bottomNav.setVisibility(View.INVISIBLE);

        // Use a handler to stagger the animations
        new Handler().postDelayed(() -> {
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
            profileSection.setVisibility(View.VISIBLE);
            profileSection.startAnimation(slideDown);
        }, 200);

        new Handler().postDelayed(() -> {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.startAnimation(slideUp);
        }, 400);
    }

    /**
     * Gets user information from the intent that started this activity
     */
    private void getUserInfoFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("user_name");
            userPhone = intent.getStringExtra("user_phone");
            userId = intent.getStringExtra("user_id");
            userRoles = intent.getStringArrayListExtra("user_roles");
            userRawData = intent.getStringExtra("user_data");
            userEmail = intent.getStringExtra("user_email");
            userPhotoUri = intent.getStringExtra("user_photo_uri");

            if (userName != null && userPhone != null) {
                Log.d("UserDashboard", "Received user: " + userName + ", " + userPhone +
                        (userId != null ? ", ID: " + userId : ""));
            } else {
                Log.d("UserDashboard", "No user data received");
            }

            if (userRawData != null && !userRawData.isEmpty()) {
                try {
                    JSONObject userData = new JSONObject(userRawData);
                    if (userId == null || userId.isEmpty()) {
                        userId = userData.optString("account_id", "");
                    }
                    if (userRoles == null) {
                        userRoles = new ArrayList<>();
                        userRoles.add("User"); // Default role
                    }
                } catch (JSONException e) {
                    Log.e("UserDashboard", "Error parsing user data JSON: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Load user data into the profile section.
     */
    private void loadUserData(TextView nameView, TextView phoneView, TextView roleView, TextView idView) {
        nameView.setText(userName != null && !userName.isEmpty() ? userName : "Guest User");
        phoneView.setText(userPhone != null && !userPhone.isEmpty() ? userPhone : "No phone number");

        if (userRoles != null && !userRoles.isEmpty()) {
            StringBuilder roles = new StringBuilder("Role: ");
            for (int i = 0; i < userRoles.size(); i++) {
                roles.append(userRoles.get(i));
                if (i < userRoles.size() - 1) {
                    roles.append(", ");
                }
            }
            roleView.setText(roles.toString());
        } else {
            roleView.setText("Role: User"); // Default role
        }

        if (userId != null && !userId.isEmpty()) {
            idView.setText("ID: " + userId);
        } else {
            if (userPhone != null && !userPhone.isEmpty()) {
                String tempId = "USR-" + userPhone.substring(Math.max(0, userPhone.length() - 4));
                idView.setText("ID: " + tempId);
            } else {
                idView.setText("ID: N/A");
            }
        }
    }

    /**
     * Shows the edit profile form with a slide-down animation.
     * This method is now used by ProfileFragment to animate its own internal form.
     */
    public void showEditProfileForm(LinearLayout form) { // Removed EditText params
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
        form.setVisibility(View.VISIBLE);
        form.startAnimation(slideDown);
    }

    /**
     * Hides the edit profile form with a slide-up animation.
     * This method is now used by ProfileFragment to animate its own internal form.
     */
    public void hideEditProfileForm(LinearLayout form) {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        slideUp.setInterpolator(this, android.R.interpolator.accelerate_decelerate);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                form.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        form.startAnimation(slideUp);
    }

    /**
     * Validates and saves profile changes using Firestore. This method is called by ProfileFragment.
     */
    public void saveProfileChanges(String newUsername, String newPhone,
                                   String newPassword, String confirmPassword,
                                   LinearLayout form, EditText usernameField,
                                   EditText phoneField, EditText confirmPasswordField) {

        if (newUsername.isEmpty()) {
            usernameField.setError("Username cannot be empty");
            return;
        }
        if (newPhone.isEmpty()) {
            phoneField.setError("Phone number cannot be empty");
            return;
        }
        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return;
        }

        // Hide the form using the fragment's animation method
        hideEditProfileForm(form);
        updateProfileInFirestore(newUsername, newPhone, newPassword);
    }

    /**
     * Updates the user profile in Firestore database
     */
    private void updateProfileInFirestore(String username, String phoneNumber, String newPassword) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is required for profile update", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Updating profile...", Toast.LENGTH_SHORT).show();

        // Reference to the user document
        DocumentReference userRef = db.collection("users").document(userId);

        // Create update data map
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phone_number", phoneNumber);

        // Add password hash if new password is provided
        if (!newPassword.isEmpty()) {
            // Hash the password using BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            updates.put("password_hash", hashedPassword);
        }

        // Perform the update
        userRef.update(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            runOnUiThread(() -> {
                                Toast.makeText(UserDashboard.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                // Update local user data
                                userName = username;
                                userPhone = phoneNumber;

                                // Update the UI elements
                                if (userNameView != null) userNameView.setText(userName);
                                if (userPhoneView != null) userPhoneView.setText(userPhone);

                                // Navigate back to the dashboard
                                if (previousSelectedItemId != R.id.nav_dashboard) {
                                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                                }

                                Log.d("UserDashboard", "Profile updated successfully in Firestore");
                            });
                        } else {
                            runOnUiThread(() -> {
                                Exception e = task.getException();
                                String errorMsg = e != null ? e.getMessage() : "Unknown error occurred";
                                Toast.makeText(UserDashboard.this, "Update failed: " + errorMsg, Toast.LENGTH_LONG).show();
                                Log.e("UserDashboard", "Error updating profile", e);
                            });
                        }
                    }
                });
    }

    /**
     * Retrieves user data from Firestore (optional method for data synchronization)
     */
    private void getUserDataFromFirestore(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Update local data with Firestore data
                        userName = document.getString("username");
                        userPhone = document.getString("phone_number");

                        // Get role information
                        Long roleId = document.getLong("role_id");
                        if (roleId != null) {
                            getRoleNameFromFirestore(roleId.intValue());
                        }

                        // Update UI
                        runOnUiThread(() -> {
                            if (userNameView != null) userNameView.setText(userName);
                            if (userPhoneView != null) userPhoneView.setText(userPhone);
                        });

                        Log.d("UserDashboard", "User data retrieved from Firestore");
                    } else {
                        Log.d("UserDashboard", "No such user document");
                    }
                } else {
                    Log.e("UserDashboard", "Error getting user document", task.getException());
                }
            }
        });
    }

    /**
     * Retrieves role name from Firestore based on role ID
     */
    private void getRoleNameFromFirestore(int roleId) {
        DocumentReference roleRef = db.collection("roles").document(String.valueOf(roleId));

        roleRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String roleName = document.getString("role_name");
                        if (roleName != null) {
                            userRoles = new ArrayList<>();
                            userRoles.add(roleName);

                            // Update UI with role information
                            runOnUiThread(() -> {
                                TextView userRoleView = findViewById(R.id.user_role);
                                if (userRoleView != null) {
                                    userRoleView.setText("Role: " + roleName);
                                }
                            });
                        }
                    }
                } else {
                    Log.e("UserDashboard", "Error getting role document", task.getException());
                }
            }
        });
    }

    // Public getters for user info, so fragments can access them
    public String getUserName() {
        return userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * Shows the car listing fragment
     * Called when user taps "Browse Cars" button from Favorites screen
     */
    public void showCarListingFragment() {
        // Create intent to open CarListing activity
        Intent intent = new Intent(this, com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);

        // Pass user information to new activity
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_phone", userPhone);

        // Launch activity
        startActivity(intent);

        Log.d("UserDashboard", "Started CarListing Activity from Favorites");
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                int newSelectedItemId = item.getItemId();
                if (newSelectedItemId == previousSelectedItemId) {
                    return false; // Don't reload the same fragment
                }

                Fragment selectedFragment = null;
                if (newSelectedItemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (newSelectedItemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (newSelectedItemId == R.id.nav_favorites) {
                    selectedFragment = new FavoriteCarsFragment();
                }

                if (selectedFragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    // Use a simple fade in/out animation for all fragment transitions
                    transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

                    transaction.replace(R.id.fragment_container, selectedFragment);
                    transaction.commit();

                    previousSelectedItemId = newSelectedItemId;
                }
                return true;
            };

    // --- Fragment classes ---

    /**
     * Placeholder Fragment for the Dashboard.
     */
    public static class DashboardFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_dashboard, container, false);
        }
    }

    /**
     * Fragment for updating user profile using Firestore.
     */
    public static class ProfileFragment extends Fragment {

        private LinearLayout editProfileForm;
        private EditText editUsername;
        private EditText editPhone;
        private EditText editPassword;
        private EditText editConfirmPassword;
        private Button btnSave;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_profile, container, false);

            UserDashboard activity = (UserDashboard) getActivity();
            if (activity == null) return view; // Return view even if activity is null, though it shouldn't be

            // Initialize views from the fragment's inflated layout
            editProfileForm = view.findViewById(R.id.edit_profile_form_fragment);
            editUsername = view.findViewById(R.id.edit_username_fragment);
            editPhone = view.findViewById(R.id.edit_phone_fragment);
            editPassword = view.findViewById(R.id.edit_password_fragment);
            editConfirmPassword = view.findViewById(R.id.edit_confirm_password_fragment);
            btnSave = view.findViewById(R.id.btn_save_fragment);

            // Set initial values from the activity's user data
            editUsername.setText(activity.getUserName());
            editPhone.setText(activity.getUserPhone());

            // Show the form with animation when the fragment is created/displayed
            activity.showEditProfileForm(editProfileForm);

            btnSave.setOnClickListener(v -> {
                String newUsername = editUsername.getText().toString().trim();
                String newPhone = editPhone.getText().toString().trim();
                String newPassword = editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText().toString().trim();

                activity.saveProfileChanges(newUsername, newPhone, newPassword, confirmPassword,
                        editProfileForm, editUsername, editPhone, editConfirmPassword);

                // Clear password fields after attempt to save
                editPassword.setText("");
                editConfirmPassword.setText("");
            });

            return view;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            UserDashboard activity = (UserDashboard) getActivity();
            if (activity == null) return;
            // Ensure the form is hidden when the fragment's view is destroyed
            if (editProfileForm != null && editProfileForm.getVisibility() == View.VISIBLE) {
                activity.hideEditProfileForm(editProfileForm);
            }
        }
    }

    private void handleUserIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String userName = extras.getString("user_name");
            String userEmail = extras.getString("user_email");
            String userId = extras.getString("user_id");
            String userPhotoUri = extras.getString("user_photo_uri");

            // Use Glide to load the profile picture from the URL
            if (userPhotoUri != null && !userPhotoUri.isEmpty()) {
                Glide.with(this)
                        .load(userPhotoUri)
                        .circleCrop() // Make the image circular
                        .placeholder(R.drawable.ic_profile) // Add a placeholder drawable
                        .error(R.drawable.ic_profile) // Add an error drawable
                        .into(profile_image);
            }

            // You can log the other details or use them elsewhere
            Log.d("Homepage", "User Logged In: ID=" + userId + ", Email=" + userEmail);

        } else {
            // Handle case where no data is passed (e.g., direct launch for testing)
            Log.d("Homepage", "No intent extras found.");
        }
    }
}