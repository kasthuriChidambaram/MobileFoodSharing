package com.unavify.app.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.unavify.app.R;
import java.util.List;

public class UnifiedFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final int VIEW_TYPE_NATIVE_AD = 2;
    
    private final List<Object> feedItems;
    private final Context context;

    public UnifiedFeedAdapter(Context context, List<Object> feedItems) {
        this.context = context;
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        } else if (viewType == VIEW_TYPE_AD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad_post, parent, false);
            return new AdViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad, parent, false);
            return new NativeAdViewHolder(view, context);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = feedItems.get(position);
        
        if (holder instanceof PostViewHolder && item instanceof Post) {
            Post post = (Post) item;
            PostViewHolder postHolder = (PostViewHolder) holder;
            
            // Bind post data directly
            postHolder.usernameText.setText(post.username != null ? post.username : "User");
            postHolder.captionText.setText(post.caption);
            
            // Set comment count
            if (post.commentCount > 0) {
                postHolder.commentCountText.setText(String.valueOf(post.commentCount));
                postHolder.commentCountText.setVisibility(View.VISIBLE);
            } else {
                postHolder.commentCountText.setVisibility(View.GONE);
            }
            
            // Load user profile image
            if (post.userProfileImageUrl != null && !post.userProfileImageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(post.userProfileImageUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(postHolder.userImage);
            } else {
                postHolder.userImage.setImageResource(R.drawable.profile_placeholder);
            }
            
            // Load post media
            if (post.mediaUrl != null && !post.mediaUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(post.mediaUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .fitCenter()
                        .into(postHolder.postImage);
            }
            
            // Set click listeners
            postHolder.commentsButton.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, CommentActivity.class);
                intent.putExtra("postId", post.postId);
                intent.putExtra("postCaption", post.caption);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).startActivityForResult(intent, 1002);
                } else {
                    context.startActivity(intent);
                }
            });
            
            postHolder.reportButton.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, ReportActivity.class);
                intent.putExtra("postId", post.postId);
                intent.putExtra("postCaption", post.caption);
                intent.putExtra("contentType", "post");
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).startActivityForResult(intent, 1003);
                } else {
                    context.startActivity(intent);
                }
            });
            
        } else if (holder instanceof AdViewHolder && item instanceof AdPost) {
            AdPost adPost = (AdPost) item;
            AdViewHolder adHolder = (AdViewHolder) holder;
            
            // Bind ad data directly
            adHolder.advertiserNameText.setText(adPost.advertiserName);
            adHolder.adTitleText.setText(adPost.adTitle);
            adHolder.adDescriptionText.setText(adPost.adDescription);
            adHolder.callToActionButton.setText(adPost.callToAction);
            
            // Load advertiser logo
            if (adPost.advertiserLogoUrl != null && !adPost.advertiserLogoUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(adPost.advertiserLogoUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(adHolder.advertiserLogoImage);
            } else {
                adHolder.advertiserLogoImage.setImageResource(R.drawable.profile_placeholder);
            }
            
            // Load ad image
            if (adPost.adImageUrl != null && !adPost.adImageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(adPost.adImageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .fitCenter()
                        .into(adHolder.adContentImage);
            } else {
                adHolder.adContentImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
            
            // Set click listeners
            adHolder.callToActionButton.setOnClickListener(v -> {
                if (adPost.adLinkUrl != null && !adPost.adLinkUrl.isEmpty()) {
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(adPost.adLinkUrl));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        // Link opening failed silently
                    }
                }
            });
            
            adHolder.adInfoText.setOnClickListener(v -> {
                showAdInfoDialog(adPost);
            });
            
            adHolder.moreButton.setOnClickListener(v -> {
                showAdOptionsDialog(adPost);
            });
        } else if (holder instanceof NativeAdViewHolder && item instanceof NativeAd) {
            NativeAd nativeAd = (NativeAd) item;
            NativeAdViewHolder nativeAdHolder = (NativeAdViewHolder) holder;
            nativeAdHolder.bind(nativeAd);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < feedItems.size()) {
            Object item = feedItems.get(position);
            if (item instanceof Post) {
                return VIEW_TYPE_POST;
            } else if (item instanceof AdPost) {
                return VIEW_TYPE_AD;
            } else if (item instanceof NativeAd) {
                return VIEW_TYPE_NATIVE_AD;
            }
        }
        return VIEW_TYPE_POST; // Default
    }

    @Override
    public int getItemCount() {
        return feedItems != null ? feedItems.size() : 0;
    }

    private void showAdInfoDialog(AdPost adPost) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Why you're seeing this ad")
                .setMessage("This ad is shown based on your interests and activity in the app. " +
                           "You can control ad preferences in your settings.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Hide this ad", (dialog, which) -> {
                    // Ad hidden silently
                })
                .show();
    }

    private void showAdOptionsDialog(AdPost adPost) {
        String[] options = {"Report this ad", "Hide this ad", "Why this ad?"};
        
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Ad Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Report ad
                            android.content.Intent intent = new android.content.Intent(context, ReportActivity.class);
                            intent.putExtra("postId", adPost.adId);
                            intent.putExtra("postCaption", adPost.adTitle);
                            intent.putExtra("contentType", "ad");
                            if (context instanceof android.app.Activity) {
                                ((android.app.Activity) context).startActivityForResult(intent, 1005);
                            } else {
                                context.startActivity(intent);
                            }
                            break;
                        case 1:
                            // Hide ad silently
                            break;
                        case 2:
                            // Why this ad
                            showAdInfoDialog(adPost);
                            break;
                    }
                })
                .show();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.imageview.ShapeableImageView userImage;
        com.google.android.material.imageview.ShapeableImageView postImage;
        android.widget.TextView usernameText;
        android.widget.TextView captionText;
        android.widget.TextView commentCountText;
        android.widget.ImageButton commentsButton;
        android.widget.ImageButton reportButton;
        
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.image_user_profile);
            postImage = itemView.findViewById(R.id.image_post);
            usernameText = itemView.findViewById(R.id.text_username);
            captionText = itemView.findViewById(R.id.text_caption);
            commentCountText = itemView.findViewById(R.id.text_comment_count);
            commentsButton = itemView.findViewById(R.id.button_comments);
            reportButton = itemView.findViewById(R.id.button_report_post);
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.imageview.ShapeableImageView advertiserLogoImage;
        com.google.android.material.imageview.ShapeableImageView adContentImage;
        android.widget.TextView advertiserNameText;
        android.widget.TextView adTitleText;
        android.widget.TextView adDescriptionText;
        android.widget.Button callToActionButton;
        android.widget.TextView adInfoText;
        android.widget.ImageButton moreButton;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            advertiserLogoImage = itemView.findViewById(R.id.image_advertiser_logo);
            adContentImage = itemView.findViewById(R.id.image_ad_content);
            advertiserNameText = itemView.findViewById(R.id.text_advertiser_name);
            adTitleText = itemView.findViewById(R.id.text_ad_title);
            adDescriptionText = itemView.findViewById(R.id.text_ad_description);
            callToActionButton = itemView.findViewById(R.id.button_call_to_action);
            adInfoText = itemView.findViewById(R.id.text_ad_info);
            moreButton = itemView.findViewById(R.id.button_ad_more);
        }
    }
} 