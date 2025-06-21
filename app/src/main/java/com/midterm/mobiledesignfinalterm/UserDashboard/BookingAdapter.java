package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.models.Booking;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<Booking> bookingList;
    private final Context context;
    private final BookingClickListener listener;

    // Interface for click events
    public interface BookingClickListener {
        void onBookingDetailsClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookingList, Context context, BookingClickListener listener) {
        this.bookingList = bookingList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Set car name
        holder.textViewCarName.setText(booking.getVehicleName());

        // Set booking status with appropriate background
        holder.textViewStatus.setText(booking.getStatus().toUpperCase(Locale.ROOT));
        setStatusBackground(holder.textViewStatus, booking.getStatus());

        // Set date range
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);
        String dateRange = dateFormat.format(booking.getPickupDate()) + " - " +
                dateFormat.format(booking.getReturnDate()) + ", " +
                new SimpleDateFormat("yyyy", Locale.US).format(booking.getReturnDate());
        holder.textViewDateRange.setText(dateRange);

        // Set location
        holder.textViewLocation.setText(booking.getPickupLocation());

        // Set price
        holder.textViewPrice.setText(booking.getFormattedFinalPrice());

        // Load image
        if (booking.getVehicleImage() != null && !booking.getVehicleImage().isEmpty()) {
            // Check if the URL is a full URL or a relative path
            String imageUrl = booking.getVehicleImage();
            if (imageUrl.startsWith("/")) {
                // Relative path, prepend with base URL
                imageUrl = "http://10.0.2.2/myapi" + imageUrl;
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.car_placeholder)
                    .error(R.drawable.car_placeholder)
                    .centerCrop()
                    .into(holder.imageViewCar);
        } else {
            holder.imageViewCar.setImageResource(R.drawable.car_placeholder);
        }

        // Set button click listener
        holder.buttonViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingDetailsClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    /**
     * Set appropriate background color for booking status
     */
    private void setStatusBackground(TextView textView, String status) {
        int backgroundResId;

        switch (status.toLowerCase()) {
            case "confirmed":
                backgroundResId = R.drawable.status_confirmed_bg;
                break;
            case "pending":
                backgroundResId = R.drawable.status_pending_bg;
                break;
            case "completed":
                backgroundResId = R.drawable.status_completed_bg;
                break;
            case "cancelled":
                backgroundResId = R.drawable.status_cancelled_bg;
                break;
            case "ongoing":
                backgroundResId = R.drawable.status_ongoing_bg;
                break;
            default:
                backgroundResId = R.drawable.status_pending_bg;
                break;
        }

        textView.setBackgroundResource(backgroundResId);
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCar;
        TextView textViewCarName;
        TextView textViewStatus;
        TextView textViewDateRange;
        TextView textViewLocation;
        TextView textViewPrice;
        Button buttonViewDetails;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCar = itemView.findViewById(R.id.imageViewCar);
            textViewCarName = itemView.findViewById(R.id.textViewCarName);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDateRange = itemView.findViewById(R.id.textViewDateRange);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonViewDetails = itemView.findViewById(R.id.buttonViewDetails);
        }
    }
}
