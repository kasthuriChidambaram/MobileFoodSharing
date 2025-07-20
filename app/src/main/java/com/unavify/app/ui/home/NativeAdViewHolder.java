package com.unavify.app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.unavify.app.R;
import com.unavify.app.ui.home.ReportActivity;

public class NativeAdViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "NativeAdViewHolder";
    
    private final Context context;
    private final NativeAdView nativeAdView;
    private final ImageView adIcon;
    private final TextView adHeadline;
    private final TextView adAdvertiser;
    private final MediaView adMedia;
    private final TextView adBody;
    private final Button adCallToAction;
    private final ImageButton btnReportAd;
    private NativeAd nativeAd;
    
    public NativeAdViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        
        // Initialize views
        nativeAdView = (NativeAdView) itemView;
        adIcon = itemView.findViewById(R.id.ad_icon);
        adHeadline = itemView.findViewById(R.id.ad_headline);
        adAdvertiser = itemView.findViewById(R.id.ad_advertiser);
        adMedia = itemView.findViewById(R.id.ad_media);
        adBody = itemView.findViewById(R.id.ad_body);
        adCallToAction = itemView.findViewById(R.id.ad_call_to_action);
        btnReportAd = itemView.findViewById(R.id.btn_report_ad);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // Report ad button
        btnReportAd.setOnClickListener(v -> {
            Log.d(TAG, "Report ad button clicked");
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("report_type", "ad");
            intent.putExtra("item_id", "native_ad");
            intent.putExtra("item_title", "Native Advertisement");
            context.startActivity(intent);
        });
    }
    
    public void bind(NativeAd nativeAd) {
        if (nativeAd == null) {
            Log.w(TAG, "Native ad is null, cannot bind");
            return;
        }
        
        try {
            Log.d(TAG, "Binding native ad: " + nativeAd.getHeadline());
            
            // Store the native ad reference
            this.nativeAd = nativeAd;
            
            // Bind the native ad to the NativeAdView
            nativeAdView.setNativeAd(nativeAd);
            
            // Bind headline
            if (nativeAd.getHeadline() != null) {
                adHeadline.setText(nativeAd.getHeadline());
                nativeAdView.setHeadlineView(adHeadline);
            }
            
            // Bind advertiser
            if (nativeAd.getAdvertiser() != null) {
                adAdvertiser.setText(nativeAd.getAdvertiser());
                nativeAdView.setAdvertiserView(adAdvertiser);
            }
            
            // Bind body
            if (nativeAd.getBody() != null) {
                adBody.setText(nativeAd.getBody());
                nativeAdView.setBodyView(adBody);
            }
            
            // Bind call to action
            if (nativeAd.getCallToAction() != null) {
                adCallToAction.setText(nativeAd.getCallToAction());
                nativeAdView.setCallToActionView(adCallToAction);
            }
            
            // Bind media (using MediaView instead of ImageView)
            if (nativeAd.getMediaContent() != null) {
                adMedia.setVisibility(View.VISIBLE);
                adMedia.setMediaContent(nativeAd.getMediaContent());
                nativeAdView.setMediaView(adMedia);
            } else {
                adMedia.setVisibility(View.GONE);
            }
            
            // Bind icon
            if (nativeAd.getIcon() != null) {
                adIcon.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(nativeAd.getIcon().getDrawable())
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(adIcon);
                nativeAdView.setIconView(adIcon);
            } else {
                adIcon.setVisibility(View.VISIBLE);
                adIcon.setImageResource(R.drawable.profile_placeholder);
            }
            
            Log.d(TAG, "Native ad bound successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error binding native ad", e);
        }
    }
    
    public void unbind() {
        try {
            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }
            Log.d(TAG, "Native ad unbound");
        } catch (Exception e) {
            Log.e(TAG, "Error unbinding native ad", e);
        }
    }
} 