package com.midterm.mobiledesignfinalterm.homepage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.midterm.mobiledesignfinalterm.R;

import java.util.List;
import java.util.Locale;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Homepage.Car> carList;

    public CarAdapter(List<Homepage.Car> carList) {
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Homepage.Car car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private void animateItemClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.05f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", 4f, 12f, 4f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, elevation);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new OvershootInterpolator(1.1f));
        animatorSet.start();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewCar;
        private TextView textViewCarName;
        private TextView textViewAvailability;
        private TextView textViewSeats;
        private TextView textViewPrice;
        private TextView textViewRating;
        private ImageView imageViewFavorite;
        private Button buttonMoreDetails;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCar = itemView.findViewById(R.id.imageViewCar);
            textViewCarName = itemView.findViewById(R.id.textViewCarName);
            textViewAvailability = itemView.findViewById(R.id.textViewAvailability);
            textViewSeats = itemView.findViewById(R.id.textViewSeats);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
            buttonMoreDetails = itemView.findViewById(R.id.buttonMoreDetails);
        }

        public void bind(Homepage.Car car) {
            // Configure image loading with consistent dimensions
            RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .override(300, 160) // Fixed size for consistent display
                .dontTransform();  // Prevent any automatic transformations

            // Load image with the configured options
            Glide.with(itemView.getContext())
                .load(car.getImageUrl())
                .apply(requestOptions)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_all_brands)
                .into(imageViewCar);

            // Update text fields
            textViewCarName.setText(car.getName());
            textViewAvailability.setText(car.getDescription());
            textViewSeats.setText(String.format(Locale.getDefault(), "%d Seats", car.getSeats()));
            textViewPrice.setText(String.format(Locale.getDefault(), "đ%,.0f/ngày", car.getPrice()));
            textViewRating.setText(String.format(Locale.US, "%.1f", car.getRating()));

            // Handle favorite button clicks
            imageViewFavorite.setOnClickListener(v -> {
                animateFavoriteClick(v);
            });

            // Set up the More Details button
            buttonMoreDetails.setOnClickListener(v -> {
                animateItemClick(v);
                navigateToHomepageWithFilter(car.getName());
            });
        }

        private void navigateToHomepageWithFilter(String carName) {
            Intent intent = new Intent(itemView.getContext(), com.midterm.mobiledesignfinalterm.CarListing.CarListing.class);
            intent.putExtra("selected_brand", carName); // Using the key "selected_brand" as seen in CarListing.java
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            itemView.getContext().startActivity(intent);
        }

        private void animateItemClick(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.05f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new OvershootInterpolator(1.1f));
            animatorSet.start();
        }

        private void animateFavoriteClick(View view) {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(rotation, scaleX, scaleY);
            animatorSet.setDuration(400);
            animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
            animatorSet.start();
        }
    }
}