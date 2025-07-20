# Firebase Ads Collection Setup 🔥📱

## Overview
This guide will help you set up the `ads` collection in Firebase Firestore with sample ad documents that your app can read.

## 🚀 Setup Instructions

### Step 1: Access Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **unavify**
3. Navigate to **Firestore Database**

### Step 2: Create Ads Collection
1. Click **"Start collection"** (if no collections exist)
2. Collection ID: `ads`
3. Click **"Next"**

### Step 3: Add Sample Ad Documents

#### Ad Document 1: Organic Harvest
**Document ID:** `organic_harvest_ad`

```json
{
  "advertiserName": "Organic Harvest",
  "advertiserLogoUrl": "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=100&h=100&fit=crop&crop=face",
  "adTitle": "Fresh Organic Vegetables Delivered",
  "adDescription": "Get farm-fresh organic vegetables delivered to your doorstep. Support local farmers and enjoy the best quality produce.",
  "adImageUrl": "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&h=600&fit=crop",
  "adLinkUrl": "https://organic-harvest.com",
  "callToAction": "Order Now",
  "isSponsored": true,
  "timestamp": 1752645691874,
  "adType": 1,
  "isActive": true,
  "priority": 1,
  "impressions": 0,
  "clicks": 0
}
```

#### Ad Document 2: Chef's Kitchen
**Document ID:** `chefs_kitchen_ad`

```json
{
  "advertiserName": "Chef's Kitchen",
  "advertiserLogoUrl": "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=100&h=100&fit=crop&crop=face",
  "adTitle": "Professional Kitchen Equipment",
  "adDescription": "Upgrade your kitchen with professional-grade tools and appliances. From beginner to chef-level equipment.",
  "adImageUrl": "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800&h=600&fit=crop",
  "adLinkUrl": "https://chefs-kitchen.com",
  "callToAction": "Shop Equipment",
  "isSponsored": true,
  "timestamp": 1752645691874,
  "adType": 1,
  "isActive": true,
  "priority": 1,
  "impressions": 0,
  "clicks": 0
}
```

#### Ad Document 3: Healthy Bites
**Document ID:** `healthy_bites_ad`

```json
{
  "advertiserName": "Healthy Bites",
  "advertiserLogoUrl": "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=100&h=100&fit=crop&crop=face",
  "adTitle": "Delicious Healthy Recipes",
  "adDescription": "Discover thousands of delicious, healthy recipes. Weekly meal plans and cooking tips included.",
  "adImageUrl": "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=800&h=600&fit=crop",
  "adLinkUrl": "https://healthy-bites.com",
  "callToAction": "Get Recipes",
  "isSponsored": true,
  "timestamp": 1752645691874,
  "adType": 1,
  "isActive": true,
  "priority": 1,
  "impressions": 0,
  "clicks": 0
}
```

#### Ad Document 4: Spice World
**Document ID:** `spice_world_ad`

```json
{
  "advertiserName": "Spice World",
  "advertiserLogoUrl": "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=100&h=100&fit=crop&crop=face",
  "adTitle": "Premium Spices & Herbs",
  "adDescription": "Explore our collection of premium spices and herbs from around the world. Elevate your cooking.",
  "adImageUrl": "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800&h=600&fit=crop",
  "adLinkUrl": "https://spice-world.com",
  "callToAction": "Explore Spices",
  "isSponsored": true,
  "timestamp": 1752645691874,
  "adType": 1,
  "isActive": true,
  "priority": 1,
  "impressions": 0,
  "clicks": 0
}
```

## 📋 Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `advertiserName` | String | Name of the advertiser/brand |
| `advertiserLogoUrl` | String | URL to advertiser's logo image |
| `adTitle` | String | Main headline of the ad |
| `adDescription` | String | Detailed description of the ad |
| `adImageUrl` | String | URL to the main ad image |
| `adLinkUrl` | String | URL where users go when they click the ad |
| `callToAction` | String | Button text (e.g., "Shop Now", "Learn More") |
| `isSponsored` | Boolean | Always true for ads |
| `timestamp` | Number | When the ad was created |
| `adType` | Number | 1=Image, 2=Video, 3=Carousel |
| `isActive` | Boolean | Whether the ad should be shown |
| `priority` | Number | Higher numbers show first |
| `impressions` | Number | How many times ad was shown |
| `clicks` | Number | How many times ad was clicked |

## 🔧 How to Add Documents

### Method 1: Manual Entry
1. Click **"Add document"** in the ads collection
2. Enter the Document ID (e.g., `organic_harvest_ad`)
3. Add each field manually using the JSON structure above

### Method 2: Import JSON
1. Use Firebase CLI or Admin SDK to import the JSON data
2. Or copy-paste the JSON structure field by field

## ✅ Verification Steps

1. **Check Collection**: Verify `ads` collection exists
2. **Check Documents**: Verify all 4 sample ads are added
3. **Check Fields**: Ensure all required fields are present
4. **Check Status**: Ensure `isActive` is set to `true`

## 🚀 Testing

After setting up the ads collection:

1. **Run your app** - it should now load ads from Firebase
2. **Check logs** - look for "Loaded X ads from Firebase" message
3. **Browse feed** - ads should appear every 5 posts
4. **Test interactions** - click ads, report ads, etc.

## 🔄 App Updates

The app has been updated to:
- ✅ Remove hardcoded sample ads
- ✅ Only load ads from Firebase
- ✅ Show posts only if no ads are available
- ✅ Log ad loading status for debugging

## 🎯 Next Steps

1. **Add the sample ads** to Firebase using the JSON above
2. **Test the app** to ensure ads load correctly
3. **Customize ads** with your own content
4. **Add real advertisers** when ready for production

---

**Ready to monetize your app? Set up the ads collection and start generating revenue! 💰📱** 