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

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    // Interface for brand click events
    public interface OnBrandClickListener {
        void onBrandClick(Homepage.Brand brand);
    }

    private List<Homepage.Brand> brandList;
    private int selectedPosition = 0; // Default to "All" selected
    private OnBrandClickListener listener;

    public BrandAdapter(List<Homepage.Brand> brandList) {
        this.brandList = brandList;
    }

    // Constructor with listener
    public BrandAdapter(List<Homepage.Brand> brandList, OnBrandClickListener listener) {
        this.brandList = brandList;
        this.listener = listener;
    }

    // Method to set listener after creation
    public void setOnBrandClickListener(OnBrandClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Homepage.Brand brand = brandList.get(position);
        holder.bind(brand, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Homepage.Brand clickedBrand = brandList.get(currentPosition);

                // Set the clicked position as selected
                int previousSelected = selectedPosition;
                selectedPosition = currentPosition;

                // Update UI to show selected state
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                // Trigger click animation
                animateItemClick(v);

                // Notify listener about the click
                if (listener != null) {
                    listener.onBrandClick(clickedBrand);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return brandList.size();
    }

    // Method to get currently selected brand
    public Homepage.Brand getSelectedBrand() {
        if (selectedPosition >= 0 && selectedPosition < brandList.size()) {
            return brandList.get(selectedPosition);
        }
        return null;
    }

    private void animateItemClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }

    static class BrandViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewBrand;
        private TextView textViewBrandName;
        private View containerBrand;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBrand = itemView.findViewById(R.id.imageViewBrand);
            textViewBrandName = itemView.findViewById(R.id.textViewBrandName);
            containerBrand = itemView.findViewById(R.id.containerBrand);
        }

        public void bind(Homepage.Brand brand, boolean isSelected) {
            imageViewBrand.setImageResource(brand.getIconResource());
            textViewBrandName.setText(brand.getName());

            // Update selection state
            if (isSelected) {
                containerBrand.setBackgroundResource(R.drawable.brand_selected_background);
                textViewBrandName.setTextColor(itemView.getContext().getColor(R.color.black));
            } else {
                containerBrand.setBackgroundResource(R.drawable.brand_background);
                textViewBrandName.setTextColor(itemView.getContext().getColor(R.color.white));
            }
        }
    }
}
