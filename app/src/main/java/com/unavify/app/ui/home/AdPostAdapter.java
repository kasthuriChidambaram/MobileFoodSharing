package com.unavify.app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.unavify.app.R;
import java.util.List;

public class AdPostAdapter extends RecyclerView.Adapter<AdPostAdapter.AdPostViewHolder> {
    private final List<AdPost> adPosts;
    private final Context context;

    public AdPostAdapter(Context context, List<AdPost> adPosts) {
        this.context = context;
        this.adPosts = adPosts;
    }

    @NonNull
    @Override
    public AdPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad_post, parent, false);
        return new AdPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdPostViewHolder holder, int position) {
        AdPost adPost = adPosts.get(position);
        
        // Set advertiser info
        holder.advertiserNameText.setText(adPost.advertiserName);
        
        // Load advertiser logo
        if (adPost.advertiserLogoUrl != null && !adPost.advertiserLogoUrl.isEmpty()) {
            Glide.with(context)
                    .load(adPost.advertiserLogoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(holder.advertiserLogoImage);
        } else {
            holder.advertiserLogoImage.setImageResource(R.drawable.profile_placeholder);
        }
        
        // Set ad content
        holder.adTitleText.setText(adPost.adTitle);
        holder.adDescriptionText.setText(adPost.adDescription);
        holder.callToActionButton.setText(adPost.callToAction);
        
        // Load ad image
        if (adPost.adImageUrl != null && !adPost.adImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(adPost.adImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .fitCenter()
                    .into(holder.adContentImage);
        } else {
            holder.adContentImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // Call to action button click
        holder.callToActionButton.setOnClickListener(v -> {
            if (adPost.adLinkUrl != null && !adPost.adLinkUrl.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adPost.adLinkUrl));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Link not available", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Ad info click
        holder.adInfoText.setOnClickListener(v -> {
            showAdInfoDialog(adPost);
        });
        
        // More options button
        holder.moreButton.setOnClickListener(v -> {
            showAdOptionsDialog(adPost);
        });
    }

    @Override
    public int getItemCount() {
        return adPosts.size();
    }

    private void showAdInfoDialog(AdPost adPost) {
        new AlertDialog.Builder(context)
                .setTitle("Why you're seeing this ad")
                .setMessage("This ad is shown based on your interests and activity in the app. " +
                           "You can control ad preferences in your settings.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Hide this ad", (dialog, which) -> {
                    // TODO: Implement ad hiding functionality
                    Toast.makeText(context, "Ad hidden", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showAdOptionsDialog(AdPost adPost) {
        String[] options = {"Report this ad", "Hide this ad", "Why this ad?"};
        
        new AlertDialog.Builder(context)
                .setTitle("Ad Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Report ad
                            Intent intent = new Intent(context, ReportActivity.class);
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
                            // Hide ad
                            Toast.makeText(context, "Ad hidden", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            // Why this ad
                            showAdInfoDialog(adPost);
                            break;
                    }
                })
                .show();
    }

    public static class AdPostViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView advertiserLogoImage;
        ShapeableImageView adContentImage;
        TextView advertiserNameText;
        TextView adTitleText;
        TextView adDescriptionText;
        Button callToActionButton;
        TextView adInfoText;
        ImageButton moreButton;

        public AdPostViewHolder(@NonNull View itemView) {
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