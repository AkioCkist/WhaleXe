package com.midterm.mobiledesignfinalterm.CarDetail;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private final Context context;
    private List<Amenity> amenityList;
    private RecyclerView recyclerView;

    public AmenityAdapter(Context context, List<Amenity> amenityList) {
        this.context = context;
        this.amenityList = amenityList;
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenityList = amenities;
        notifyDataSetChanged();
        // Tự động điều chỉnh chiều cao của RecyclerView sau khi dữ liệu thay đổi
        if (recyclerView != null) {
            adjustRecyclerViewHeight();
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        // Điều chỉnh chiều cao ngay khi adapter được gắn vào RecyclerView
        adjustRecyclerViewHeight();
    }

    private void adjustRecyclerViewHeight() {
        if (recyclerView == null || amenityList == null || amenityList.isEmpty()) return;

        // Tính toán chiều cao dựa trên số lượng item và thông số của item_layout
        int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.amenity_item_height);
        if (itemHeight == 0) {
            // Nếu không có dimension, lấy giá trị mặc định (chiều cao của một hàng + padding)
            itemHeight = (int) (36 * context.getResources().getDisplayMetrics().density); // 36dp là ước lượng chiều cao item
        }

        int totalRows = (int) Math.ceil(amenityList.size() / 2.0); // Giả sử hiển thị 2 cột
        int totalHeight = totalRows * itemHeight;

        ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        if (layoutParams != null && layoutParams.height != totalHeight) {
            layoutParams.height = totalHeight;
            recyclerView.setLayoutParams(layoutParams);
            Log.d("AmenityAdapter", "Set RecyclerView height to: " + totalHeight + ", rows: " + totalRows);
        }
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.amenity_item, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        Amenity amenity = amenityList.get(position);
        holder.tvAmenityName.setText(amenity.getDescription());
        // Add logging
        Log.d("AmenityAdapter", "Setting icon for: " + amenity.getName() +
                ", icon name: " + amenity.getIcon() +
                ", resolved resource ID: " + amenity.getIconResId());
        int resourceId = mapIconToResourceId(amenity.getIcon());
        holder.imgAmenityIcon.setImageResource(resourceId);
    }

    private int mapIconToResourceId(String iconName) {
        if (iconName == null) return R.drawable.cardetail_ic_default;

        // Convert icon name to lowercase for case-insensitive matching
        String icon = iconName.toLowerCase();
        switch (icon) {
            case "bluetooth":
                return R.drawable.cardetail_ic_bluetooth;
            case "camera":
                return R.drawable.cardetail_ic_adventurecamera;
            case "airbag":
                return R.drawable.cardetail_ic_airbag;
            case "etc":
                return R.drawable.cardetail_ic_etc;
            case "sunroof":
                return R.drawable.cardetail_ic_carroof;
            case "sportmode":
                return R.drawable.cardetail_ic_sportmode;
            case "tablet":
                return R.drawable.cardetail_ic_screencar;
            case "camera360":
                return R.drawable.cardetail_ic_camera360;
            case "map":
                return R.drawable.cardetail_ic_map;
            case "rotateccw":
                return R.drawable.cardetail_ic_rearviewcamera;
            case "circle":
                return R.drawable.cardetail_ic_cartire;
            case "package":
                return R.drawable.cardetail_ic_cartrunk;
            case "shield":
                return R.drawable.cardetail_ic_collisionsensor;
            case "radar":
                return R.drawable.cardetail_ic_reversesenser;
            case "childseat":
                return R.drawable.cardetail_ic_childseat;
            default:
                return R.drawable.cardetail_ic_default;
        }
    }

    @Override
    public int getItemCount() {
        return amenityList.size();
    }

    public static class AmenityViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAmenityIcon;
        TextView tvAmenityName;

        public AmenityViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAmenityIcon = itemView.findViewById(R.id.iv_iconAmenity);
            tvAmenityName = itemView.findViewById(R.id.tv_amenityName);
        }
    }
}
