package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.firebase.FirestoreManager;
import com.midterm.mobiledesignfinalterm.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class RecentBookingsFragment extends Fragment {

    private static final String TAG = "RecentBookingsFragment";

    private RecyclerView recyclerViewBookings;
    private TextView textViewNoBookings;
    private LinearLayout emptyStateView;
    private Button btnBrowseCars;

    private BookingAdapter bookingAdapter;
    private final List<Booking> bookingsList = new ArrayList<>();

    private UserDashboard parentActivity;
    private String userId = "0";

    // Firestore Manager
    private FirestoreManager firestoreManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserDashboard) {
            parentActivity = (UserDashboard) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_bookings, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupBrowseCarsButton();

        // Initialize FirestoreManager
        firestoreManager = FirestoreManager.getInstance();

        if (parentActivity != null) {
            try {
                userId = parentActivity.getUserId();
                Log.d(TAG, "User ID from activity: " + userId);
                if (userId == null || userId.isEmpty()) {
                    userId = "0";
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user ID: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Parent activity is null");
        }

        loadRecentBookings();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        textViewNoBookings = view.findViewById(R.id.textViewNoBookings);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        btnBrowseCars = view.findViewById(R.id.btnBrowseCars);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewBookings.setLayoutManager(layoutManager);

        bookingAdapter = new BookingAdapter(bookingsList, getContext(), this::viewBookingDetails);

        recyclerViewBookings.setHasFixedSize(true);
        recyclerViewBookings.setAdapter(bookingAdapter);

        Log.d(TAG, "RecyclerView setup complete");
    }

    private void setupBrowseCarsButton() {
        btnBrowseCars.setOnClickListener(v -> {
            if (parentActivity != null) {
                parentActivity.showCarListingFragment();
                Log.d(TAG, "Navigating to Car Listing Fragment");
            }
        });
    }

    /**
     * Load recent bookings for the current user
     */
    private void loadRecentBookings() {
        Log.d(TAG, "Loading recent bookings for user ID: " + userId);

        if ("0".equals(userId)) {
            showEmptyState("User ID not available");
            return;
        }

        // Show loading state
        recyclerViewBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        textViewNoBookings.setText("Loading bookings...");

        // Use FirestoreManager to fetch bookings
        firestoreManager.fetchUserBookings(userId, new FirestoreManager.BookingsCallback() {
            @Override
            public void onBookingsLoaded(List<Booking> bookings) {
                bookingsList.clear();
                bookingsList.addAll(bookings);

                Log.d(TAG, "Loaded " + bookingsList.size() + " bookings from Firestore");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!bookingsList.isEmpty()) {
                            bookingAdapter.notifyDataSetChanged();
                            showContent();
                        } else {
                            showEmptyState("You don't have any bookings yet");
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading bookings: " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> showEmptyState("Error: " + errorMessage));
                }
            }
        });
    }

    /**
     * Show content when we have bookings
     */
    private void showContent() {
        Log.d(TAG, "Showing bookings list");
        emptyStateView.setVisibility(View.GONE);
        recyclerViewBookings.setVisibility(View.VISIBLE);
    }

    /**
     * Show empty state with message
     */
    private void showEmptyState(String message) {
        Log.d(TAG, "Showing empty state: " + message);
        recyclerViewBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        textViewNoBookings.setText(message);
    }

    /**
     * Handle view details button click for a booking
     */
    private void viewBookingDetails(Booking booking) {
        Log.d(TAG, "Viewing details for booking: " + booking.getId());
        // Implementation for booking details
        // To be added later when we have a BookingDetailsActivity
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload bookings when coming back to this fragment
        loadRecentBookings();
    }
}
