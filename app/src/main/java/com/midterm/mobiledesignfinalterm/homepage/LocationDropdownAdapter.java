package com.midterm.mobiledesignfinalterm.homepage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;

public class LocationDropdownAdapter extends RecyclerView.Adapter<LocationDropdownAdapter.ViewHolder> {

    private final String[] locations;
    private final OnLocationSelectedListener listener;
    private int lastPosition = -1;

    public interface OnLocationSelectedListener {
        void onLocationSelected(String location);
    }

    public LocationDropdownAdapter(String[] locations, OnLocationSelectedListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_dropdown_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(locations[position]);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(locations[position]);
            }
        });

        // Apply animation to the items
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return locations.length;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the item wasn't already displayed
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
            animation.setDuration(300);
            animation.setStartOffset(position * 100);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
