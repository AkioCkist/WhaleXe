package com.midterm.mobiledesignfinalterm.CarListing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;

import java.util.ArrayList;
import java.util.List;

public class FilterChipAdapter extends RecyclerView.Adapter<FilterChipAdapter.ChipViewHolder> {

    public interface OnChipClickListener {
        void onChipClick(int position, String value);
    }

    private List<String> chipValues;
    private int selectedPosition = 0; // Default to first item (typically "All")
    private OnChipClickListener listener;

    public FilterChipAdapter(List<String> chipValues, OnChipClickListener listener) {
        this.chipValues = chipValues != null ? chipValues : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        String chipValue = chipValues.get(position);
        holder.chipText.setText(chipValue);
        holder.chipText.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Update UI for previous and new selection
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            // Notify listener
            if (listener != null) {
                // Pass null for "All" position, otherwise pass the actual value
                String value = position == 0 ? null : chipValue;
                listener.onChipClick(position, value);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chipValues.size();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < chipValues.size()) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedValue() {
        if (selectedPosition == 0) {
            return null; // "All" is represented as null
        } else if (selectedPosition < chipValues.size()) {
            return chipValues.get(selectedPosition);
        }
        return null;
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView chipText;

        ChipViewHolder(View itemView) {
            super(itemView);
            chipText = (TextView) itemView;
        }
    }
}
