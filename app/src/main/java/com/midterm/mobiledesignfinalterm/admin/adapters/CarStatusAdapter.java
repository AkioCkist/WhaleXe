package com.midterm.mobiledesignfinalterm.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.admin.models.CarStatus;

import java.util.List;

public class CarStatusAdapter extends RecyclerView.Adapter<CarStatusAdapter.CarStatusViewHolder> {

    private List<CarStatus> carStatusList;
    private Context context;

    public CarStatusAdapter(List<CarStatus> carStatusList, Context context) {
        this.carStatusList = carStatusList;
        this.context = context;
    }

    @NonNull
    @Override
    public CarStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_car_status, parent, false);
        return new CarStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarStatusViewHolder holder, int position) {
        CarStatus carStatus = carStatusList.get(position);
        
        holder.tvCarName.setText(carStatus.getVehicleName());
        holder.tvCarBrand.setText(carStatus.getBrand() + " " + carStatus.getModel());
        holder.tvCarStatus.setText(carStatus.getStatus());
        
        // Set status-specific information
        switch (carStatus.getStatus()) {
            case "RENTED":
                holder.tvExtraInfo.setVisibility(View.VISIBLE);
                holder.tvExtraInfo.setText("Thuê bởi: " + carStatus.getRentedBy() + 
                                         "\nNgày thuê: " + carStatus.getRentalDate() +
                                         "\nNgày trả: " + carStatus.getReturnDate());
                holder.tvCarStatus.setTextColor(context.getResources().getColor(R.color.status_rented));
                break;
            case "AVAILABLE":
                holder.tvExtraInfo.setVisibility(View.GONE);
                holder.tvCarStatus.setTextColor(context.getResources().getColor(R.color.status_available));
                break;
            case "MAINTENANCE":
                holder.tvExtraInfo.setVisibility(View.VISIBLE);
                holder.tvExtraInfo.setText("Bảo trì từ: " + carStatus.getRentalDate() +
                                         "\nDự kiến hoàn thành: " + carStatus.getReturnDate());
                holder.tvCarStatus.setTextColor(context.getResources().getColor(R.color.status_maintenance));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return carStatusList.size();
    }

    public static class CarStatusViewHolder extends RecyclerView.ViewHolder {
        TextView tvCarName, tvCarBrand, tvCarStatus, tvExtraInfo;

        public CarStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCarName = itemView.findViewById(R.id.tv_car_name);
            tvCarBrand = itemView.findViewById(R.id.tv_car_brand);
            tvCarStatus = itemView.findViewById(R.id.tv_car_status);
            tvExtraInfo = itemView.findViewById(R.id.tv_extra_info);
        }
    }
}
