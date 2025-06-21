package com.midterm.mobiledesignfinalterm.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;
import com.midterm.mobiledesignfinalterm.admin.models.UserInfo;

import java.util.List;

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.UserInfoViewHolder> {

    private List<UserInfo> userInfoList;
    private Context context;

    public UserInfoAdapter(List<UserInfo> userInfoList, Context context) {
        this.userInfoList = userInfoList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user_info, parent, false);
        return new UserInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserInfoViewHolder holder, int position) {
        UserInfo userInfo = userInfoList.get(position);
        
        holder.tvUserName.setText(userInfo.getName().isEmpty() ? "Chưa cập nhật" : userInfo.getName());
        holder.tvUserPhone.setText(userInfo.getPhoneNumber());
        holder.tvUserEmail.setText(userInfo.getEmail().isEmpty() ? "Chưa cập nhật" : userInfo.getEmail());
        holder.tvUserCreated.setText("Đăng ký: " + userInfo.getCreatedAt());
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    public static class UserInfoViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserPhone, tvUserEmail, tvUserCreated;

        public UserInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserPhone = itemView.findViewById(R.id.tv_user_phone);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserCreated = itemView.findViewById(R.id.tv_user_created);
        }
    }
}
