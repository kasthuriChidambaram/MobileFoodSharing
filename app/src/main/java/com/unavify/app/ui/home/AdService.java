package com.unavify.app.ui.home;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdService {
    private static final String TAG = "AdService";
    // Removed fixed frequency - now calculates optimal positions dynamically
    
    private final FirebaseFirestore firestore;
    private final List<AdPost> availableAds;
    private final Random random;
    
    public AdService() {
        this.firestore = FirebaseFirestore.getInstance();
        this.availableAds = new ArrayList<>();
        this.random = new Random();
        loadAds();
    }
    
    public void loadAds() {
        // First try to load ads with isActive filter
        firestore.collection("ads")
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    availableAds.clear();
                    for (var doc : querySnapshot) {
                        AdPost ad = doc.toObject(AdPost.class);
                        if (ad != null) {
                            ad.adId = doc.getId();
                            // Set defaults if fields are missing
                            if (!ad.isActive) ad.isActive = true;
                            if (ad.priority == 0) ad.priority = 1;
                            availableAds.add(ad);
                        }
                    }
                    Log.d(TAG, "Loaded " + availableAds.size() + " active ads from Firebase");
                    
                    // Log the loaded ads for debugging
                    for (int i = 0; i < availableAds.size(); i++) {
                        AdPost ad = availableAds.get(i);
                        Log.d(TAG, "Loaded ad " + i + ": '" + ad.adTitle + "' (priority " + ad.priority + ", id: " + ad.adId + ")");
                    }
                    
                    // If no ads found with isActive filter, try loading all ads
                    if (availableAds.isEmpty()) {
                        loadAllAds();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load active ads from Firebase, trying all ads", e);
                    // If query fails (e.g., missing isActive field), try loading all ads
                    loadAllAds();
                });
    }
    
    private void loadAllAds() {
        firestore.collection("ads")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    availableAds.clear();
                    for (var doc : querySnapshot) {
                        AdPost ad = doc.toObject(AdPost.class);
                        if (ad != null) {
                            ad.adId = doc.getId();
                            // Set defaults if fields are missing
                            if (!ad.isActive) ad.isActive = true;
                            if (ad.priority == 0) ad.priority = 1;
                            availableAds.add(ad);
                        }
                    }
                    Log.d(TAG, "Loaded " + availableAds.size() + " ads from Firebase (all ads)");
                    
                    // Log the loaded ads for debugging
                    for (int i = 0; i < availableAds.size(); i++) {
                        AdPost ad = availableAds.get(i);
                        Log.d(TAG, "Loaded ad " + i + ": '" + ad.adTitle + "' (priority " + ad.priority + ", id: " + ad.adId + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load ads from Firebase", e);
                    Log.d(TAG, "No ads available - feed will show posts only");
                });
    }
    

    
    public List<Object> insertAdsIntoFeed(List<Post> posts) {
        List<Object> feedItems = new ArrayList<>();
        int adCount = 0;
        
        if (posts == null) {
            Log.w(TAG, "Posts list is null, returning empty feed");
            return feedItems;
        }
        
        Log.d(TAG, "Creating feed with " + posts.size() + " posts, available ads: " + availableAds.size());
        
        // Calculate optimal ad positions to distribute ads evenly
        List<Integer> adPositions = calculateAdPositions(posts.size(), availableAds.size());
        Log.d(TAG, "Calculated ad positions: " + adPositions);
        
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            if (post != null) {
                feedItems.add(post);
                
                // Check if we should insert an ad at this position
                if (adPositions.contains(i + 1) && adCount < availableAds.size() && !availableAds.isEmpty()) {
                    AdPost ad = getAdByPriority(adCount);
                    if (ad != null) {
                        feedItems.add(ad);
                        adCount++;
                        Log.d(TAG, "Inserted ad '" + ad.adTitle + "' (priority " + ad.priority + ") at position " + (i + 1));
                    }
                }
            }
        }
        
        Log.d(TAG, "Feed created with " + posts.size() + " posts and " + adCount + " ads");
        return feedItems;
    }
    
    private List<Integer> calculateAdPositions(int totalPosts, int totalAds) {
        List<Integer> positions = new ArrayList<>();
        
        if (totalAds == 0 || totalPosts == 0) {
            Log.d(TAG, "No ads or posts to calculate positions for");
            return positions;
        }
        
        // Distribute ads evenly across the feed
        int spacing = totalPosts / (totalAds + 1);
        Log.d(TAG, "Calculating positions: " + totalPosts + " posts, " + totalAds + " ads, spacing: " + spacing);
        
        for (int i = 1; i <= totalAds; i++) {
            int position = i * spacing;
            // Ensure position doesn't exceed total posts
            if (position <= totalPosts) {
                positions.add(position);
                Log.d(TAG, "Ad " + i + " will be at position " + position);
            }
        }
        
        // If we have more ads than calculated positions, add them at the end
        while (positions.size() < totalAds && positions.size() < totalPosts) {
            int lastPosition = positions.isEmpty() ? 0 : positions.get(positions.size() - 1);
            int newPosition = Math.min(lastPosition + spacing, totalPosts);
            if (newPosition > lastPosition) {
                positions.add(newPosition);
                Log.d(TAG, "Additional ad will be at position " + newPosition);
            } else {
                break;
            }
        }
        
        Log.d(TAG, "Final ad positions: " + positions);
        return positions;
    }
    
    private AdPost getAdByPriority(int index) {
        if (availableAds.isEmpty()) {
            return null;
        }
        
        // Cycle through available ads to ensure variety
        int actualIndex = index % availableAds.size();
        AdPost ad = availableAds.get(actualIndex);
        Log.d(TAG, "Getting ad at index " + index + " (actual: " + actualIndex + "): '" + ad.adTitle + "' (priority " + ad.priority + ", id: " + ad.adId + ")");
        return ad;
    }
    
    private AdPost getRandomAd() {
        if (availableAds.isEmpty()) {
            return null;
        }
        
        int index = random.nextInt(availableAds.size());
        return availableAds.get(index);
    }
    
    public void trackAdImpression(String adId) {
        firestore.collection("ads")
                .document(adId)
                .update("impressions", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ad impression tracked for " + adId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to track ad impression", e));
    }
    
    public void trackAdClick(String adId) {
        firestore.collection("ads")
                .document(adId)
                .update("clicks", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ad click tracked for " + adId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to track ad click", e));
    }
    
    public void hideAd(String adId) {
        // TODO: Implement ad hiding functionality
        Log.d(TAG, "Ad hidden: " + adId);
    }
    
    public void reportAd(String adId, String reason) {
        // TODO: Implement ad reporting functionality
        Log.d(TAG, "Ad reported: " + adId + " for reason: " + reason);
    }
} 