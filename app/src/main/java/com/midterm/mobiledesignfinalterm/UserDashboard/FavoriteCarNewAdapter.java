package com.midterm.mobiledesignfinalterm.UserDashboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.midterm.mobiledesignfinalterm.CarDetail.CarDetailActivity;
import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.models.FavoriteCar;

import java.util.List;

public class FavoriteCarNewAdapter extends RecyclerView.Adapter<FavoriteCarNewAdapter.FavoriteCarViewHolder> {

    private final List<FavoriteCar> favoriteCarList;
    private final Context context;
    private final OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteCar car);
    }

    public FavoriteCarNewAdapter(List<FavoriteCar> favoriteCarList, Context context, OnFavoriteClickListener favoriteClickListener) {
        this.favoriteCarList = favoriteCarList;
        this.context = context;
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public FavoriteCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_car, parent, false);
        return new FavoriteCarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteCarViewHolder holder, int position) {
        FavoriteCar car = favoriteCarList.get(position);

        // DEBUG - Log binding activity
        Log.d("FavoriteCarAdapter", "Binding car at position " + position + ": " + car.getName());

        // Set car info
        holder.textViewCarName.setText(car.getName());
        holder.textViewCarType.setText(car.getVehicleType());

        // Make sure price is displayed correctly
        if (!car.getPriceFormatted().isEmpty()) {
            holder.textViewBasePrice.setText(car.getPriceFormatted());
            Log.d("FavoriteCarAdapter", "Price formatted: " + car.getPriceFormatted());
        } else {
            String formattedPrice = String.format("%,.0f", car.getBasePrice());
            holder.textViewBasePrice.setText(formattedPrice);
            Log.d("FavoriteCarAdapter", "Using base price: " + formattedPrice);
        }

        // Set car specifications
        holder.textViewFuelType.setText(car.getFuelType());
        holder.textViewTransmission.setText(car.getTransmission());
        holder.textViewSeats.setText(car.getFormattedSeats());
        holder.textViewConsumption.setText(car.getFormattedConsumption());

        // Always show filled heart icon for favorites
        holder.imageViewFavorite.setImageResource(R.drawable.ic_heart_filled);

        // Load car image with Glide - Make sure primaryImage isn't null or empty
        String imageUrl = car.getPrimaryImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("FavoriteCarAdapter", "Original image URL: " + imageUrl);

            // Process image URL to get local asset path
            if (imageUrl.startsWith("/cars/")) {
                // Extract the car image path from "/cars/CarModel/image.png"
                // and point to the asset in the assets/cars folder
                String assetPath = "cars" + imageUrl.substring(5); // Remove /cars and keep the rest
                imageUrl = "file:///android_asset/" + assetPath;
                Log.d("FavoriteCarAdapter", "Using asset path: " + imageUrl);
            }
            else if (imageUrl.startsWith("/")) {
                // For other URLs starting with "/", try regular API URL
                imageUrl = "http://10.0.2.2/myapi" + imageUrl;
                Log.d("FavoriteCarAdapter", "Using API URL: " + imageUrl);
            }
            else if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
                // For other relative paths without protocol
                imageUrl = "file:///android_asset/cars/" + imageUrl;
                Log.d("FavoriteCarAdapter", "Using default asset URL: " + imageUrl);
            }

            // Load with more debugging
            Log.d("FavoriteCarAdapter", "Loading final URL: " + imageUrl);

            Glide.with(context)
                 .load(imageUrl)
                 .placeholder(R.drawable.loading_spinner)
                 .error(R.drawable.car_placeholder)
                 .into(holder.imageViewCar);
        } else {
            Log.w("FavoriteCarAdapter", "No image URL for car: " + car.getName());
            holder.imageViewCar.setImageResource(R.drawable.car_placeholder);
        }

        // Set click listener for favorite icon
        holder.imageViewFavorite.setOnClickListener(v -> {
            Log.d("FavoriteCarAdapter", "Favorite icon clicked for car: " + car.getName());
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(car);
            }
        });

        // Set click listener for rental button to navigate to car details
        holder.btnRentalNow.setOnClickListener(v -> {
            Log.d("FavoriteCarAdapter", "Rental Now button clicked for car: " + car.getName());
            Intent intent = new Intent(context, CarDetailActivity.class);
            intent.putExtra("car_id", car.getVehicleId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteCarList.size();
    }

    static class FavoriteCarViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCarName;
        TextView textViewCarType;
        TextView textViewBasePrice;
        TextView textViewFuelType;
        TextView textViewTransmission;
        TextView textViewSeats;
        TextView textViewConsumption;
        ImageView imageViewCar;
        ImageView imageViewFavorite;
        Button btnRentalNow;

        public FavoriteCarViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCarName = itemView.findViewById(R.id.tv_FRcardCarName);
            textViewCarType = itemView.findViewById(R.id.tv_FRcardCarType);
            textViewBasePrice = itemView.findViewById(R.id.tv_FRBasePrice);
            textViewFuelType = itemView.findViewById(R.id.tv_FRCardFuelType);
            textViewTransmission = itemView.findViewById(R.id.tv_FRCardTransmission);
            textViewSeats = itemView.findViewById(R.id.tv_Seats);
            textViewConsumption = itemView.findViewById(R.id.tv_FRcardConsumption);
            imageViewCar = itemView.findViewById(R.id.iv_FRprimaryImageCar);
            imageViewFavorite = itemView.findViewById(R.id.iv_FRFavorite);
            btnRentalNow = itemView.findViewById(R.id.btn_FRrentalNow);
        }
    }
}
