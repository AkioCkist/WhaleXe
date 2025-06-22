package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.midterm.mobiledesignfinalterm.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String TAG = "BOOKING_DEBUG";

    private String userId;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingHistoryAdapter adapter;
    private List<BookingDetail> bookingDetailList;
    private TextView textViewNoHistory;
    private Handler mainHandler;

    // Factory method to create a new instance of this fragment with user ID
    public static DashboardFragment newInstance(String userId) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        Log.d("BOOKING_DEBUG", "Creating DashboardFragment with userId: " + userId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());

        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            Log.d(TAG, "Got userId from arguments: " + userId);
        } else {
            Log.e(TAG, "No arguments found!");
            userId = "";
        }

        db = FirebaseFirestore.getInstance();
        bookingDetailList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBookingHistory);
        textViewNoHistory = view.findViewById(R.id.textViewNoHistory);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingHistoryAdapter(getContext(), bookingDetailList);
        recyclerView.setAdapter(adapter);

        // Initial notification to confirm fragment loaded - with delay to ensure it shows
        showToastSafely("Dashboard Fragment Loaded with userId: " + userId, Toast.LENGTH_LONG);

        // Fetch booking data
        if (userId != null && !userId.isEmpty()) {
            // Short delay to ensure UI is ready
            mainHandler.postDelayed(() -> fetchBookings(), 500);
        } else {
            // Try getting userId from parent activity
            if (getActivity() instanceof UserDashboard) {
                userId = ((UserDashboard) getActivity()).getUserId();

                // Show prominent toast about getting userId from parent
                showToastSafely("⭐ IMPORTANT: Got userId from parent: " + userId, Toast.LENGTH_LONG);

                if (userId != null && !userId.isEmpty()) {
                    // Short delay to ensure UI is ready
                    mainHandler.postDelayed(() -> fetchBookings(), 500);
                } else {
                    showToastSafely("⚠️ ERROR: No valid userId found", Toast.LENGTH_LONG);
                    showNoBookingsMessage();
                }
            } else {
                showToastSafely("⚠️ ERROR: Parent activity is not UserDashboard", Toast.LENGTH_LONG);
                showNoBookingsMessage();
            }
        }

        return view;
    }

    /**
     * Helper method to safely show toasts, avoiding common issues with context and threading
     */
    private void showToastSafely(final String message, final int duration) {
        if (getContext() == null) return;

        Context context = getContext();
        mainHandler.post(() -> {
            if (getContext() != null) { // Double-check context is still valid
                Toast.makeText(context, message, duration).show();
                Log.d(TAG, "TOAST: " + message); // Also log the message
            }
        });
    }

    private void fetchBookings() {
        showToastSafely("Attempting to fetch bookings for user: " + userId, Toast.LENGTH_SHORT);
        Log.d(TAG, "Fetching bookings for user: " + userId);

        // Query for new booking format (using userId field)
        Task<QuerySnapshot> newFormatTask = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    if (queryDocuments.isEmpty()) {
                        showToastSafely("No bookings found with userId field", Toast.LENGTH_SHORT);
                    } else {
                        showToastSafely("Found " + queryDocuments.size() + " bookings with userId", Toast.LENGTH_SHORT);
                    }
                })
                .addOnFailureListener(e -> {
                    showToastSafely("Error querying with userId: " + e.getMessage(), Toast.LENGTH_SHORT);
                });

        // Query for old booking format (using renter_id field as number)
        Task<QuerySnapshot> oldFormatTask = null;
        final Task<QuerySnapshot> finalOldFormatTask;

        try {
            long userIdNum = Long.parseLong(userId);
            oldFormatTask = db.collection("bookings")
                    .whereEqualTo("renter_id", userIdNum)
                    .get()
                    .addOnSuccessListener(queryDocuments -> {
                        if (queryDocuments.isEmpty()) {
                            showToastSafely("No bookings found with renter_id field", Toast.LENGTH_SHORT);
                        } else {
                            showToastSafely("Found " + queryDocuments.size() + " bookings with renter_id", Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToastSafely("Error querying with renter_id: " + e.getMessage(), Toast.LENGTH_SHORT);
                    });
        } catch (NumberFormatException e) {
            showToastSafely("userId is not a number, skipping renter_id query", Toast.LENGTH_SHORT);
            Log.d(TAG, "userId is not a number, skipping renter_id query");
        }

        finalOldFormatTask = oldFormatTask;

        // Check if bookings collection exists
        db.collection("bookings").limit(1).get()
            .addOnSuccessListener(documents -> {
                if (documents.isEmpty()) {
                    showToastSafely("Bookings collection is empty or doesn't exist!", Toast.LENGTH_LONG);
                } else {
                    showToastSafely("Bookings collection exists", Toast.LENGTH_SHORT);
                }
            });

        // Process results from both queries
        if (finalOldFormatTask != null) {
            Tasks.whenAllComplete(newFormatTask, finalOldFormatTask)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToastSafely("Both queries completed", Toast.LENGTH_SHORT);
                        } else {
                            showToastSafely("Some queries failed", Toast.LENGTH_SHORT);
                        }

                        if (newFormatTask.isSuccessful()) {
                            processBookingResults(newFormatTask.getResult());
                        } else {
                            showToastSafely("New format task failed: " +
                                (newFormatTask.getException() != null ? newFormatTask.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT);
                        }

                        if (finalOldFormatTask.isSuccessful()) {
                            processBookingResults(finalOldFormatTask.getResult());
                        } else {
                            showToastSafely("Old format task failed: " +
                                (finalOldFormatTask.getException() != null ? finalOldFormatTask.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT);
                        }

                        // Update UI after processing all bookings
                        updateUI();
                    });
        } else {
            // Just handle the new format task
            newFormatTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    processBookingResults(task.getResult());
                } else {
                    showToastSafely("New format task failed: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT);
                }
                updateUI();
            });
        }
    }

    private void processBookingResults(QuerySnapshot querySnapshot) {
        if (querySnapshot == null) {
            showToastSafely("QuerySnapshot is null", Toast.LENGTH_SHORT);
            return;
        }

        if (querySnapshot.isEmpty()) {
            showToastSafely("No bookings found in query results", Toast.LENGTH_SHORT);
            return;
        }

        showToastSafely("Processing " + querySnapshot.size() + " booking results", Toast.LENGTH_SHORT);

        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Log document contents to help debug
            Log.d(TAG, "Document ID: " + document.getId() + ", Data: " + document.getData());

            try {
                // Convert document to Booking object
                Booking booking = document.toObject(Booking.class);
                if (booking == null) {
                    showToastSafely("Failed to convert document to Booking object: " + document.getId(), Toast.LENGTH_SHORT);
                    continue;
                }

                // Determine vehicle/car ID
                String vehicleId = null;

                // Check for old format (vehicle_id as number)
                if (booking.getVehicle_id() > 0) {
                    vehicleId = String.valueOf(booking.getVehicle_id());
                }
                // Check for new format (carId as string)
                else if (booking.getCarId() != null && !booking.getCarId().isEmpty()) {
                    vehicleId = booking.getCarId();
                }
                // Try to get carId directly from document
                else if (document.getString("carId") != null) {
                    vehicleId = document.getString("carId");
                }

                if (vehicleId == null || vehicleId.isEmpty()) {
                    showToastSafely("No vehicle ID found for booking: " + document.getId(), Toast.LENGTH_SHORT);
                    continue;
                }

                // Show toast with vehicle ID being fetched
                final String finalVehicleId = vehicleId;
                showToastSafely("Fetching vehicle data for ID: " + finalVehicleId, Toast.LENGTH_SHORT);

                // Fetch vehicle data
                db.collection("vehicles").document(vehicleId)
                        .get()
                        .addOnSuccessListener(vehicleDoc -> {
                            if (vehicleDoc.exists()) {
                                showToastSafely("Vehicle found: " + finalVehicleId, Toast.LENGTH_SHORT);
                                Vehicle vehicle = vehicleDoc.toObject(Vehicle.class);
                                if (vehicle != null) {
                                    // Add booking and vehicle to list
                                    bookingDetailList.add(new BookingDetail(booking, vehicle));

                                    // Notify adapter of new data
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        showToastSafely("Added booking for: " + vehicle.getName(), Toast.LENGTH_SHORT);
                                    }

                                    updateUI();
                                } else {
                                    showToastSafely("Failed to convert vehicle document to Vehicle object", Toast.LENGTH_SHORT);
                                }
                            } else {
                                showToastSafely("Vehicle not found with ID: " + finalVehicleId, Toast.LENGTH_SHORT);
                            }
                        })
                        .addOnFailureListener(e -> {
                            showToastSafely("Failed to fetch vehicle: " + e.getMessage(), Toast.LENGTH_SHORT);
                        });
            } catch (Exception e) {
                showToastSafely("Error processing booking: " + e.getMessage(), Toast.LENGTH_SHORT);
                Log.e(TAG, "Error processing booking", e);
            }
        }

        // Update UI immediately if no processing started
        if (bookingDetailList.isEmpty()) {
            showToastSafely("Booking list is still empty after processing", Toast.LENGTH_SHORT);
            updateUI();
        }
    }

    private void updateUI() {
        mainHandler.post(() -> {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null");
                return;
            }

            if (bookingDetailList.isEmpty()) {
                showToastSafely("No booking history to display", Toast.LENGTH_SHORT);
                showNoBookingsMessage();
            } else {
                showToastSafely("Displaying " + bookingDetailList.size() + " bookings", Toast.LENGTH_SHORT);
                recyclerView.setVisibility(View.VISIBLE);
                textViewNoHistory.setVisibility(View.GONE);
            }
        });
    }

    private void showNoBookingsMessage() {
        mainHandler.post(() -> {
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (textViewNoHistory != null) textViewNoHistory.setVisibility(View.VISIBLE);
        });
    }
}
