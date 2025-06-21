package com.midterm.mobiledesignfinalterm.CarListing;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.models.Car;

import java.util.List;

public class CarListingAdapter extends RecyclerView.Adapter<CarListingAdapter.CarViewHolder> {

    private List<Car> carList;
    private OnCarItemClickListener listener;
    private int lastAnimatedPosition = -1;
    private boolean animationsEnabled = true;
    private boolean isScrollingUp = false;

    public interface OnCarItemClickListener {
        void onRentalClick(Car car);
        void onFavoriteClick(Car car, int position);
    }

    public CarListingAdapter(List<Car> carList, OnCarItemClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car_listing, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car, position);

        // Only animate new items in view (not already animated or during fast scroll)
        if (animationsEnabled && position > lastAnimatedPosition) {
            // Use pull-up animation when scrolling up
            if (isScrollingUp) {
                animatePullUp(holder.itemView, position);
            } else {
                animateItemEntrance(holder.itemView, position);
            }
            lastAnimatedPosition = position;
        } else {
            // Reset view properties for recycled views without animation
            clearAnimationState(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private void clearAnimationState(View view) {
        view.setAlpha(1.0f);
        view.setTranslationY(0f);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
    }

    private void animateItemEntrance(View view, int position) {
        // Calculate shorter delay based on position but with a maximum cap
        long delay = Math.min(position * 50, 200);

        // Setup initial state for pull-up animation
        view.setAlpha(0.6f);
        view.setTranslationY(120f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);

        // Create and configure animation set for pull-up effect
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 120f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.0f);

        // Combine all animations to create a smooth pull-up effect
        animatorSet.playTogether(translateY, alpha, scaleX, scaleY);
        animatorSet.setInterpolator(new DecelerateInterpolator(1.2f));
        animatorSet.setStartDelay(delay);
        animatorSet.setDuration(350);
        animatorSet.start();
    }

    private void animatePullUp(View view, int position) {
        // Pull-up animation for new items when scrolling up
        long delay = Math.min(position * 30, 150); // Faster animation for pull-up

        // Setup initial state for pull-up animation
        view.setAlpha(0.6f);
        view.setTranslationY(150f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);

        // Create and configure animation set for pull-up effect
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 150f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.0f);

        // Combine all animations to create a smooth pull-up effect
        animatorSet.playTogether(translateY, alpha, scaleX, scaleY);
        animatorSet.setInterpolator(new DecelerateInterpolator(1.2f));
        animatorSet.setStartDelay(delay);
        animatorSet.setDuration(300); // Slightly faster duration
        animatorSet.start();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewCarName;
        private TextView textViewCarType;
        private TextView textViewFuelType;
        private TextView textViewTransmission;
        private TextView textViewSeats;
        private TextView textViewConsumption;
        private TextView textViewPrice;
        private TextView textViewOriginalPrice;
        private ImageView imageViewCar;
        private ImageView imageViewFavorite;
        private Button buttonRentalNow;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewCarName = itemView.findViewById(R.id.tv_cardCarName);
            textViewCarType = itemView.findViewById(R.id.tv_cardCarType);
            textViewFuelType = itemView.findViewById(R.id.tv_CardFuelType);
            textViewTransmission = itemView.findViewById(R.id.tv_CardTransmission);
            textViewSeats = itemView.findViewById(R.id.tv_Seats);
            textViewConsumption = itemView.findViewById(R.id.tv_cardConsumption);
            textViewPrice = itemView.findViewById(R.id.tv_BasePrice);
            //textViewOriginalPrice = itemView.findViewById(R.id.textViewOriginalPrice);
            imageViewCar = itemView.findViewById(R.id.iv_primaryImageCar);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
            buttonRentalNow = itemView.findViewById(R.id.btn_rentalNow);
        }

        public void bind(Car car, int position) {
            // Set car data
            textViewCarName.setText(car.getName());
            textViewCarType.setText(car.getVehicleType());
            textViewFuelType.setText(car.getFuelType());
            textViewTransmission.setText(car.getTransmission());
            textViewSeats.setText(car.getFormattedSeats());
            textViewConsumption.setText(car.getFormattedConsumption());
            textViewPrice.setText(car.getPriceFormatted() != null ? car.getPriceFormatted() : car.getBasePrice() + "VND");

            // Load image from URL if available, otherwise use resource
            if (car.getPrimaryImage() != null && !car.getPrimaryImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(car.getPrimaryImage())
                        .placeholder(R.drawable.loading_spinner)
                        .error(R.drawable.car_placeholder)
                        .into(imageViewCar);
            } else {
                imageViewCar.setImageResource(R.drawable.car_placeholder);
            }
            // Set favorite state
            updateFavoriteIcon(car.isFavorite());



            // Set click listeners
            imageViewFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFavoriteClick(v);
                    if (listener != null) {
                        listener.onFavoriteClick(car, position);
                    }
                }
            });

            buttonRentalNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateRentalButtonClick(v);
                    if (listener != null) {
                        listener.onRentalClick(car);
                    }
                }
            });

            // Add click animation for the entire card
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateCardClick(v);
                    if (listener != null) {
                        listener.onRentalClick(car);
                    }
                }
            });
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                imageViewFavorite.setImageResource(R.drawable.ic_heart_filled);
                imageViewFavorite.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
            } else {
                imageViewFavorite.setImageResource(R.drawable.ic_heart_outline);
                imageViewFavorite.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.white));
            }
        }

        private void animateFavoriteClick(View view) {
            // Heart animation chỉ scale và xoay, không thay đổi elevation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, rotation);
            animatorSet.setDuration(600);
            animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
            animatorSet.start();
        }

        private void animateRentalButtonClick(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1.1f, 1f);
            ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", 4f, 12f, 4f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, elevation);
            animatorSet.setDuration(400);
            animatorSet.start();
        }

        private void animateCardClick(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.02f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.02f, 1f);
            ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", 4f, 8f, 4f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, elevation);
            animatorSet.setDuration(200);
            animatorSet.start();
        }
    }

    // Methods to control animations based on scroll state
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    // Call this when a new batch of data is loaded or when scrolling stops
    public void resetAnimationState() {
        lastAnimatedPosition = -1;
    }

    // Method to update the list (for search/filter functionality)
    public void updateList(List<Car> newList) {
        this.carList = newList;
        resetAnimationState(); // Reset animation state when data changes
        notifyDataSetChanged();
    }

    // Method to add new items (for pagination)
    public void addItems(List<Car> newItems) {
        int startPosition = carList.size();
        carList.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    // Method to clear all items
    public void clearItems() {
        carList.clear();
        resetAnimationState(); // Reset animation state when clearing items
        notifyDataSetChanged();
    }

    // Method to set scroll direction for animations
    public void setScrollingUp(boolean scrollingUp) {
        this.isScrollingUp = scrollingUp;
    }
}
