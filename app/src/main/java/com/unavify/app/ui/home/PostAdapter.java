package com.unavify.app.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.unavify.app.R;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<Post> posts;
    private final Context context;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        android.util.Log.d("FEED_DEBUG", "onBindViewHolder: position=" + position + ", caption=" + post.caption + ", username=" + post.username);
        holder.usernameText.setText(post.username != null ? post.username : "User");
        holder.captionText.setText(post.caption);
        Glide.with(context)
                .load(post.userProfileImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(holder.userImage);
        Glide.with(context)
                .load(post.mediaUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.postImage);
        holder.commentsButton.setOnClickListener(v ->
                Toast.makeText(context, "Comments feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView userImage;
        ShapeableImageView postImage;
        TextView usernameText;
        TextView captionText;
        Button commentsButton;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.image_user_profile);
            postImage = itemView.findViewById(R.id.image_post);
            usernameText = itemView.findViewById(R.id.text_username);
            captionText = itemView.findViewById(R.id.text_caption);
            commentsButton = itemView.findViewById(R.id.button_comments);
        }
    }
} 