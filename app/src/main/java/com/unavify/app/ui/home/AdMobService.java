package com.unavify.app.ui.home;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.AdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import androidx.annotation.NonNull;

public class AdMobService {
    private static final String TAG = "AdMobService";
    
    // Test ad unit IDs - Replace with your real ad unit IDs for production
    private static final String NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    
    private final Context context;
    private final List<NativeAd> loadedNativeAds;
    private final List<InterstitialAd> loadedInterstitialAds;
    private final Random random;
    private boolean isInitialized = false;
    
    public AdMobService(Context context) {
        this.context = context;
        this.loadedNativeAds = new ArrayList<>();
        this.loadedInterstitialAds = new ArrayList<>();
        this.random = new Random();
        initializeAdMob();
    }
    
    private void initializeAdMob() {
        try {
            MobileAds.initialize(context, initializationStatus -> {
                Log.d(TAG, "AdMob initialized successfully");
                isInitialized = true;
                loadNativeAds();
                loadInterstitialAds();
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AdMob", e);
        }
    }
    
    public void loadNativeAds() {
        if (!isInitialized) {
            Log.w(TAG, "AdMob not initialized yet");
            return;
        }
        
        // Load multiple native ads for variety
        for (int i = 0; i < 3; i++) {
            loadSingleNativeAd();
        }
    }

    private void loadSingleNativeAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            NativeAdOptions nativeAdOptions = new NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build();

            // Using AdLoader instead of NativeAd.load()
            AdLoader adLoader = new AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            Log.d(TAG, "Native ad loaded successfully");
                            loadedNativeAds.add(nativeAd);

                            nativeAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "Native ad paid event: " +
                                        adValue.getCurrencyCode() + " " + adValue.getValueMicros());
                            });
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, "Native ad failed to load: " + loadAdError.getMessage());
                        }
                    })
                    .withNativeAdOptions(nativeAdOptions)
                    .build();

            adLoader.loadAd(adRequest);

        } catch (Exception e) {
            Log.e(TAG, "Error loading native ad", e);
        }
    }
    
    public void loadInterstitialAds() {
        if (!isInitialized) {
            Log.w(TAG, "AdMob not initialized yet");
            return;
        }
        
        // Load multiple interstitial ads for variety
        for (int i = 0; i < 3; i++) {
            loadSingleInterstitialAd();
        }
    }
    
    private void loadSingleInterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            
            InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd interstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully");
                    loadedInterstitialAds.add(interstitialAd);
                    
                    // Set up ad lifecycle
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed");
                        }
                        
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.e(TAG, "Interstitial ad failed to show: " + adError.getMessage());
                        }
                        
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad showed full screen content");
                        }
                    });
                }
                
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading interstitial ad", e);
        }
    }
    
    public AdView createBannerAd() {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        
        Log.d(TAG, "Banner ad created and loading");
        return adView;
    }
    
    public NativeAd getRandomNativeAd() {
        if (loadedNativeAds.isEmpty()) {
            Log.w(TAG, "No native ads available");
            return null;
        }
        
        int index = random.nextInt(loadedNativeAds.size());
        NativeAd ad = loadedNativeAds.get(index);
        Log.d(TAG, "Returning native ad");
        return ad;
    }
    
    public InterstitialAd getRandomInterstitialAd() {
        if (loadedInterstitialAds.isEmpty()) {
            Log.w(TAG, "No interstitial ads available");
            return null;
        }
        
        int index = random.nextInt(loadedInterstitialAds.size());
        InterstitialAd ad = loadedInterstitialAds.get(index);
        Log.d(TAG, "Returning interstitial ad");
        return ad;
    }
    
    public int getLoadedNativeAdCount() {
        return loadedNativeAds.size();
    }
    
    public int getLoadedAdCount() {
        return loadedNativeAds.size();
    }
    
    public boolean hasNativeAds() {
        return !loadedNativeAds.isEmpty();
    }
    
    public boolean hasAds() {
        return !loadedNativeAds.isEmpty() || !loadedInterstitialAds.isEmpty();
    }
    
    public void refreshAds() {
        Log.d(TAG, "Refreshing ads");
        loadedNativeAds.clear();
        loadedInterstitialAds.clear();
        loadNativeAds();
        loadInterstitialAds();
    }
    
    public void destroy() {
        loadedNativeAds.clear();
        loadedInterstitialAds.clear();
        Log.d(TAG, "AdMob service destroyed");
    }
}
