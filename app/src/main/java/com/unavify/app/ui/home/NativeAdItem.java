package com.unavify.app.ui.home;

public class NativeAdItem {
    private String headline;
    private String body;
    private String callToAction;
    private String advertiser;
    private String imageUrl;
    private boolean isAdMobAd;
    
    public NativeAdItem(String headline, String body, String callToAction, String advertiser, String imageUrl) {
        this.headline = headline;
        this.body = body;
        this.callToAction = callToAction;
        this.advertiser = advertiser;
        this.imageUrl = imageUrl;
        this.isAdMobAd = true;
    }
    
    // Getters
    public String getHeadline() { return headline; }
    public String getBody() { return body; }
    public String getCallToAction() { return callToAction; }
    public String getAdvertiser() { return advertiser; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAdMobAd() { return isAdMobAd; }
    
    // Setters
    public void setHeadline(String headline) { this.headline = headline; }
    public void setBody(String body) { this.body = body; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
    public void setAdvertiser(String advertiser) { this.advertiser = advertiser; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
} 