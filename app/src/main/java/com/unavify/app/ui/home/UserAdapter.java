package com.unavify.app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.unavify.app.R;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> users;
    private final Context context;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        Log.d("USER_ADAPTER", "Binding username: " + user.username);
        holder.usernameText.setText(user.username != null ? user.username : "User");
        
        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(holder.userImage);
        } else {
            // Show gradient placeholder for missing profile image
            holder.userImage.setImageResource(R.drawable.profile_placeholder);
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView userImage;
        TextView usernameText;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.image_user_profile);
            usernameText = itemView.findViewById(R.id.text_username);
        }
    }
} 