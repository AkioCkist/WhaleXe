package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.midterm.mobiledesignfinalterm.R;

import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private static final String TAG = "BookingAdapter_DEBUG"; // New tag for adapter logs

    public BookingHistoryAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        Log.d(TAG, "--- Binding view for Booking ID: " + booking.getId() + " ---");

        holder.carName.setText(booking.getVehicleName());
        holder.bookingId.setText("ID: " + (booking.getBookingId() != null ? booking.getBookingId() : booking.getId()));

        String dateRange = (booking.getPickupDate() != null ? booking.getPickupDate() : booking.getPickup_date())
                + " - " + (booking.getDropoffDate() != null ? booking.getDropoffDate() : booking.getReturn_date());
        holder.bookingDate.setText(dateRange);

        holder.locations.setText("From: " + booking.getPickupLocation() + "  - " + " To: " + booking.getDropoffLocation());
        holder.times.setText(booking.getPickupTime() + " - " + booking.getDropoffTime());
        holder.totalPrice.setText("Total: " + booking.getFormattedFinalPrice());
        String status = booking.getStatus();
        holder.status.setText(status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase());

        // --- ADDED LOGS FOR IMAGE LOADING ---
        String primaryImageUrl = booking.getVehicleImage();
        Log.d(TAG, "  -> Vehicle Name: " + booking.getVehicleName());
        Log.d(TAG, "  -> Received image URL from booking object: '" + primaryImageUrl + "'");

        if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
            String assetPath = "file:///android_asset" + primaryImageUrl;
            Log.d(TAG, "  -> Attempting to load path with Glide: '" + assetPath + "'");

            Glide.with(context)
                    .load(assetPath)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .listener(new RequestListener<Drawable>() { // ADDED GLIDE LISTENER
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "  -> GLIDE FAILED to load image. Path: " + model, e);
                            return false; // Return false to allow error placeholder to be shown
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "  -> GLIDE SUCCESS loading image. Path: " + model);
                            return false; // Return false to allow Glide to handle the resource
                        }
                    })
                    .into(holder.carImage);
        } else {
            Log.w(TAG, "  -> Image URL is null or empty. Setting default placeholder.");
            holder.carImage.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
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