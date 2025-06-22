package com.midterm.mobiledesignfinalterm.UserDashboard;

// BookingHistoryAdapter.java (Updated for local assets)
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Make sure you have this dependency
import com.midterm.mobiledesignfinalterm.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context context;
    private List<BookingDetail> bookingDetails;

    // We no longer need BASE_IMAGE_URL

    public BookingHistoryAdapter(Context context, List<BookingDetail> bookingDetails) {
        this.context = context;
        this.bookingDetails = bookingDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingDetail detail = bookingDetails.get(position);
        Booking booking = detail.getBooking();
        Vehicle vehicle = detail.getVehicle();

        holder.carName.setText(vehicle.getName());
        // Set booking ID
        holder.bookingId.setText("ID: " + booking.getBookingId());

        // Display date range
        String dateRange = booking.getPickupDate() != null && !booking.getPickupDate().isEmpty()
                ? booking.getPickupDate() + " - " + booking.getDropoffDate()
                : booking.getPickup_date() + " - " + booking.getReturn_date();
        holder.bookingDate.setText(dateRange);

        // Display locations
        String locText = "From: " + booking.getPickupLocation() + "  -" + " To: " + booking.getDropoffLocation();
        holder.locations.setText(locText);

        // Display times
        String timeText = booking.getPickupTime() + " - " + booking.getDropoffTime();
        holder.times.setText(timeText);

        // Total price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.totalPrice.setText("Total: " + currencyFormat.format(
                booking.getTotalAmount() != null
                        ? Double.parseDouble(booking.getTotalAmount())
                        : booking.getTotal_price()));

        holder.status.setText(booking.getStatus().substring(0, 1).toUpperCase() + booking.getStatus().substring(1));

        // --- MODIFIED IMAGE LOADING LOGIC ---
        String primaryImageUrl = "";
        if (vehicle.getImages() != null) {
            for (Map.Entry<String, Image> entry : vehicle.getImages().entrySet()) {
                if (entry.getValue().isPrimary()) {
                    primaryImageUrl = entry.getValue().getImageUrl();
                    break;
                }
            }
        }

        // Check if a primary image URL was found
        if (!primaryImageUrl.isEmpty()) {
            // The path from JSON is like "/cars/image.png".
            // We create a special URI for loading from the assets folder.
            String assetPath = "file:///android_asset" + primaryImageUrl;

            Glide.with(context)
                    .load(assetPath)
                    .placeholder(R.drawable.ic_profile) // Image shown while loading
                    .error(R.drawable.ic_profile)       // Image shown if path is incorrect or file is missing
                    .into(holder.carImage);
        } else {
            // If no image is found, set a default placeholder
            holder.carImage.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return bookingDetails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName, bookingId, bookingDate, locations, times, totalPrice, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.imageViewCar);
            carName = itemView.findViewById(R.id.textViewCarName);
            bookingId = itemView.findViewById(R.id.textViewBookingId);
            bookingDate = itemView.findViewById(R.id.textViewBookingDate);
            locations = itemView.findViewById(R.id.textViewLocations);
            times = itemView.findViewById(R.id.textViewTimes);
            totalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            status = itemView.findViewById(R.id.textViewStatus);
        }
    }
}