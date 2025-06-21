package com.midterm.mobiledesignfinalterm.homepage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.midterm.mobiledesignfinalterm.R;

import java.util.List;

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

        holder.itemView.setOnClickListener(v -> {
            animateItemClick(v);
            // Handle car item click (navigate to car details)
        });
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

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCar = itemView.findViewById(R.id.imageViewCar);
            textViewCarName = itemView.findViewById(R.id.textViewCarName);
            textViewAvailability = itemView.findViewById(R.id.textViewAvailability);
            textViewSeats = itemView.findViewById(R.id.textViewSeats);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
        }

        public void bind(Homepage.Car car) {
            imageViewCar.setImageResource(car.getImageResource());
            textViewCarName.setText(car.getName());
            textViewAvailability.setText(car.getAvailability());
            textViewSeats.setText(car.getSeats());
            textViewPrice.setText(car.getPrice());
            textViewRating.setText(car.getRating());

            // Handle favorite button click
            imageViewFavorite.setOnClickListener(v -> {
                animateFavoriteClick(v);
            });
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
