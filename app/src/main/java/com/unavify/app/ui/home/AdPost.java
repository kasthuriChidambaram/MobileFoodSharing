package com.unavify.app.ui.home;

public class AdPost {
    public String adId;
    public String advertiserName;
    public String advertiserLogoUrl;
    public String adTitle;
    public String adDescription;
    public String adImageUrl;
    public String adLinkUrl;
    public String callToAction;
    public boolean isSponsored;
    public long timestamp;
    public int adType; // 1: Image Ad, 2: Video Ad, 3: Carousel Ad
    public boolean isActive; // Whether the ad should be shown
    public int priority; // Higher numbers show first
    public int impressions; // How many times ad was shown
    public int clicks; // How many times ad was clicked

    public AdPost() {}

    public AdPost(String adId, String advertiserName, String advertiserLogoUrl, 
                  String adTitle, String adDescription, String adImageUrl, 
                  String adLinkUrl, String callToAction, boolean isSponsored, 
                  long timestamp, int adType) {
        this.adId = adId;
        this.advertiserName = advertiserName;
        this.advertiserLogoUrl = advertiserLogoUrl;
        this.adTitle = adTitle;
        this.adDescription = adDescription;
        this.adImageUrl = adImageUrl;
        this.adLinkUrl = adLinkUrl;
        this.callToAction = callToAction;
        this.isSponsored = isSponsored;
        this.timestamp = timestamp;
        this.adType = adType;
        this.isActive = true; // Default to active
        this.priority = 1; // Default priority
        this.impressions = 0;
        this.clicks = 0;
    }
} 