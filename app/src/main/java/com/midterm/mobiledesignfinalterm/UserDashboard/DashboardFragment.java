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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp; // Import Timestamp
import com.midterm.mobiledesignfinalterm.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String TAG = "BOOKING_DEBUG"; // Use this tag to filter logs

    private String userId;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingHistoryAdapter adapter;
    private List<Booking> bookingList;
    private TextView textViewNoHistory;
    private Handler mainHandler;

    public static DashboardFragment newInstance(String userId) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());

        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        } else if (getActivity() instanceof UserDashboard) {
            userId = ((UserDashboard) getActivity()).getUserId();
        }
        if (userId == null) userId = "";

        db = FirebaseFirestore.getInstance();
        bookingList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewBookingHistory);
        textViewNoHistory = view.findViewById(R.id.textViewNoHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingHistoryAdapter(getContext(), bookingList);
        recyclerView.setAdapter(adapter);

        if (!userId.isEmpty()) {
            fetchBookings();
        } else {
            Log.e(TAG, "No valid userId found. Cannot fetch bookings.");
            showToastSafely("⚠️ ERROR: No user ID found", Toast.LENGTH_LONG);
            showNoBookingsMessage();
        }
        return view;
    }

    private void fetchBookings() {
        Log.d(TAG, "Fetching bookings for user: " + userId);
        textViewNoHistory.setText("Loading bookings...");
        textViewNoHistory.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Task<QuerySnapshot> newFormatTask = db.collection("bookings").whereEqualTo("userId", userId).get();
        Task<QuerySnapshot> oldFormatTask = null;
        try {
            oldFormatTask = db.collection("bookings").whereEqualTo("renter_id", Long.parseLong(userId)).get();
        } catch (NumberFormatException e) {
            Log.d(TAG, "userId is not a number, skipping renter_id query.");
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(newFormatTask);
        if (oldFormatTask != null) tasks.add(oldFormatTask);

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            List<Booking> fetchedBookings = new ArrayList<>();
            for (Object snapshot : results) {
                for (DocumentSnapshot doc : ((QuerySnapshot) snapshot).getDocuments()) {
                    try {
                        Booking booking = new Booking();
                        booking.setId(doc.getId());

                        // Safely get fields
                        if (doc.contains("userId")) {
                            booking.setUserId(doc.getString("userId"));
                        }
                        if (doc.contains("carId")) {
                            booking.setCarId(doc.getString("carId"));
                        }
                        if (doc.contains("pickupDate")) {
                            booking.setPickupDate(doc.getString("pickupDate"));
                        }
                        if (doc.contains("dropoffDate")) {
                            booking.setDropoffDate(doc.getString("dropoffDate"));
                        }
                        if (doc.contains("bookingId")) {
                            booking.setBookingId(doc.getString("bookingId"));
                        }
                        if (doc.contains("totalAmount")) {
                            booking.setTotalAmount(doc.getString("totalAmount"));
                        }
                        if (doc.contains("status")) {
                            booking.setStatus(doc.getString("status"));
                        }

                        // Handle the createdAt field carefully
                        if (doc.contains("createdAt")) {
                            Object createdAtData = doc.get("createdAt");
                            if (createdAtData instanceof Timestamp) {
                                booking.setCreatedAt((Timestamp) createdAtData);
                            }
                        }

                        // Also handle old format fields if necessary
                        if (doc.contains("renter_id")) {
                            booking.setRenter_id(doc.getLong("renter_id"));
                        }
                        if (doc.contains("vehicle_id")) {
                            booking.setVehicle_id(doc.getLong("vehicle_id"));
                        }


                        fetchedBookings.add(booking);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing booking document: " + doc.getId(), e);
                    }
                }
            }
            fetchVehicleDetailsForBookings(fetchedBookings);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching bookings", e);
            updateUI();
        });
    }

    private void fetchVehicleDetailsForBookings(List<Booking> fetchedBookings) {
        if (fetchedBookings.isEmpty()) {
            Log.d(TAG, "No booking documents found for user.");
            updateUI();
            return;
        }

        Map<String, Task<DocumentSnapshot>> vehicleTasks = new HashMap<>();
        for (Booking booking : fetchedBookings) {
            String vehicleId = booking.getVehicleId();
            if (vehicleId != null && !vehicleId.isEmpty() && !vehicleId.equals("0")) {
                if (!vehicleTasks.containsKey(vehicleId)) {
                    vehicleTasks.put(vehicleId, db.collection("vehicles").document(vehicleId).get());
                }
            }
        }

        if (vehicleTasks.isEmpty()) {
            Log.w(TAG, "No valid vehicle IDs found in any bookings.");
            updateUI();
            return;
        }

        Tasks.whenAllSuccess(vehicleTasks.values()).addOnSuccessListener(vehicleSnapshots -> {
            Map<String, Vehicle> vehicleMap = new HashMap<>();
            for (Object snapshot : vehicleSnapshots) {
                DocumentSnapshot vehicleDoc = (DocumentSnapshot) snapshot;
                if (vehicleDoc.exists()) {
                    vehicleMap.put(vehicleDoc.getId(), vehicleDoc.toObject(Vehicle.class));
                }
            }
            populateFinalList(fetchedBookings, vehicleMap);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch some vehicle details.", e);
            populateFinalList(fetchedBookings, new HashMap<>());
        });
    }

    /**
     * ADDED LOGS HERE: Merges vehicle data into Booking objects and updates UI.
     */
    private void populateFinalList(List<Booking> fetchedBookings, Map<String, Vehicle> vehicleMap) {
        bookingList.clear();
        Log.d(TAG, "--- Starting to Populate Final List ---");

        for (Booking booking : fetchedBookings) {
            String vehicleId = booking.getVehicleId();
            Vehicle vehicle = vehicleMap.get(vehicleId);

            Log.d(TAG, "Processing Booking ID: " + booking.getId() + " for Vehicle ID: " + vehicleId);

            if (vehicle != null) {
                Log.d(TAG, "  -> Vehicle FOUND: " + vehicle.getName());
                booking.setVehicleName(vehicle.getName());

                String primaryImageUrl = "";
                Map<String, Image> images = vehicle.getImages();
                if (images != null && !images.isEmpty()) {
                    Log.d(TAG, "  -> Found " + images.size() + " images for this vehicle.");
                    // Iterate through the images to find the primary one
                    for (Map.Entry<String, Image> entry : images.entrySet()) {
                        Image img = entry.getValue();
                        Log.d(TAG, "  -> Checking image key '" + entry.getKey() + "': isPrimary=" + img.isPrimary() + ", URL=" + img.getImageUrl());
                        if (img.isPrimary()) {
                            primaryImageUrl = img.getImageUrl();
                            Log.d(TAG, "  -> *** PRIMARY IMAGE FOUND: " + primaryImageUrl);
                            break; // Exit loop once primary is found
                        }
                    }
                } else {
                    Log.w(TAG, "  -> !!! Vehicle has no images map or it is empty.");
                }

                if (primaryImageUrl.isEmpty()) {
                    Log.w(TAG, "  -> !!! Could not find a primary image for vehicle ID: " + vehicleId);
                }

                booking.setVehicleImage(primaryImageUrl);
                bookingList.add(booking);
            } else {
                Log.e(TAG, "  -> !!! Vehicle NOT FOUND in map for vehicle ID: " + vehicleId);
            }
        }

        bookingList.sort((b1, b2) -> {
            Timestamp t1 = b1.getCreatedAt();
            Timestamp t2 = b2.getCreatedAt();
            return (t1 != null && t2 != null) ? t2.compareTo(t1) : 0;
        });

        Log.d(TAG, "--- Finished Populating. Final list size: " + bookingList.size() + " ---");
        updateUI();
    }

    private void updateUI() {
        mainHandler.post(() -> {
            if (getContext() == null) return;
            if (bookingList.isEmpty()) showNoBookingsMessage();
            else {
                recyclerView.setVisibility(View.VISIBLE);
                textViewNoHistory.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showNoBookingsMessage() {
        if (textViewNoHistory != null) {
            textViewNoHistory.setText("You have no booking history");
            textViewNoHistory.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
    }

    private void showToastSafely(final String message, final int duration) {
        if (getContext() == null) return;
        mainHandler.post(() -> Toast.makeText(getContext(), message, duration).show());
    }
}