package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.midterm.mobiledesignfinalterm.R;

public class ProfileFragment extends Fragment {
    private EditText editUsername;
    private EditText editPhone;
    private EditText editPassword;
    private EditText editConfirmPassword;
    private Button btnSave;
    private LinearLayout editProfileForm;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        editUsername = view.findViewById(R.id.edit_username_fragment);
        editPhone = view.findViewById(R.id.edit_phone_fragment);
        editPassword = view.findViewById(R.id.edit_password_fragment);
        editConfirmPassword = view.findViewById(R.id.edit_confirm_password_fragment);
        btnSave = view.findViewById(R.id.btn_save_fragment);
        editProfileForm = view.findViewById(R.id.edit_profile_form_fragment);

        // Get current user data from parent activity
        UserDashboard activity = (UserDashboard) getActivity();
        if (activity != null) {
            editUsername.setText(activity.getUserName());
            editPhone.setText(activity.getUserPhone());

            // Show profile form with animation
            activity.showEditProfileForm(editProfileForm);
        }

        // Set up click listeners
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        // Validate inputs
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        UserDashboard activity = (UserDashboard) getActivity();
        if (activity != null) {
            // Use activity's method to validate and save profile
            activity.saveProfileChanges(
                username, phone, password, confirmPassword,
                editProfileForm, editUsername, editPhone, editConfirmPassword
            );
        } else {
            // Fallback if activity is not available
            if (username.isEmpty()) {
                editUsername.setError("Username cannot be empty");
                return;
            }

            if (phone.isEmpty()) {
                editPhone.setError("Phone number cannot be empty");
                return;
            }

            // Check if password fields are filled and match
            if (!password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    editConfirmPassword.setError("Passwords do not match");
                    return;
                }
            }

            // Show a success message as fallback
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelEdit() {
        UserDashboard activity = (UserDashboard) getActivity();
        if (activity != null) {
            // Get original user data to restore fields
            editUsername.setText(activity.getUserName());
            editPhone.setText(activity.getUserPhone());
            editPassword.setText("");
            editConfirmPassword.setText("");

            // Navigate back if needed
            getParentFragmentManager().popBackStack();
        } else {
            // Clear all fields as fallback
            editUsername.setText("");
            editPhone.setText("");
            editPassword.setText("");
            editConfirmPassword.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UserDashboard activity = (UserDashboard) getActivity();
        if (activity != null && editProfileForm != null && editProfileForm.getVisibility() == View.VISIBLE) {
            // Hide form with animation when fragment is destroyed/navigated away from
            activity.hideEditProfileForm(editProfileForm);
        }
    }
}
