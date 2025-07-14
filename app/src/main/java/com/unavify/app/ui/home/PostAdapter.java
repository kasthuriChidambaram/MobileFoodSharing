package com.unavify.app.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        
        // Set comment count - show only number if comments exist
        android.util.Log.d("FEED_DEBUG", "Setting comment count for post " + post.postId + " (caption: " + post.caption + "): " + post.commentCount);
        if (post.commentCount > 0) {
            holder.commentCountText.setText(String.valueOf(post.commentCount));
            holder.commentCountText.setVisibility(View.VISIBLE);
            android.util.Log.d("FEED_DEBUG", "Showing comment count: " + post.commentCount + " for post: " + post.postId);
        } else {
            holder.commentCountText.setVisibility(View.GONE);
            android.util.Log.d("FEED_DEBUG", "Hiding comment count for post: " + post.postId);
        }
        // Load user profile image with null check
        if (post.userProfileImageUrl != null && !post.userProfileImageUrl.isEmpty()) {
        Glide.with(context)
                .load(post.userProfileImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(holder.userImage);
        } else {
            // Set default placeholder for null/empty profile image
            holder.userImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        // Load post media with null check
        if (post.mediaUrl != null && !post.mediaUrl.isEmpty()) {
            Glide.with(context)
                    .asBitmap()
                    .load(post.mediaUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .fitCenter()
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull android.graphics.Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        float aspectRatio = (float) width / height;
                        String type;
                        if (Math.abs(aspectRatio - 1.0f) < 0.01f) {
                            type = "square";
                        } else if (aspectRatio < 1.0f && aspectRatio >= 0.8f) {
                            type = "portrait";
                        } else if (aspectRatio > 1.0f && aspectRatio <= 1.91f) {
                            type = "landscape";
                        } else {
                            type = "other";
                        }
                        android.util.Log.d("FEED_DEBUG", "Image aspectRatio=" + aspectRatio + ", classified as " + type);
                        int maxWidth = holder.postImage.getWidth();
                        if (maxWidth == 0) {
                            holder.postImage.post(() -> {
                                int realWidth = holder.postImage.getWidth();
                                if (realWidth > 0) {
                                    int newHeight = (int) (realWidth / aspectRatio);
                                    android.view.ViewGroup.LayoutParams params = holder.postImage.getLayoutParams();
                                    params.height = newHeight;
                                    holder.postImage.setLayoutParams(params);
                                    holder.postImage.setImageBitmap(resource);
                                } else {
                                    holder.postImage.setImageBitmap(resource);
                                }
                            });
                        } else {
                            int newHeight = (int) (maxWidth / aspectRatio);
                            android.view.ViewGroup.LayoutParams params = holder.postImage.getLayoutParams();
                            params.height = newHeight;
                            holder.postImage.setLayoutParams(params);
                            holder.postImage.setImageBitmap(resource);
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        holder.postImage.setImageDrawable(placeholder);
                    }
                });
        } else {
            // Set default placeholder for null/empty media URL
            holder.postImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        holder.commentsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.postId);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, 1002);
            } else {
                context.startActivity(intent);
            }
        });
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
        TextView commentCountText;
        android.widget.ImageButton commentsButton;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.image_user_profile);
            postImage = itemView.findViewById(R.id.image_post);
            usernameText = itemView.findViewById(R.id.text_username);
            captionText = itemView.findViewById(R.id.text_caption);
            commentCountText = itemView.findViewById(R.id.text_comment_count);
            commentsButton = itemView.findViewById(R.id.button_comments);
        }
    }
} 