# Firebase Ads Document Update Guide 🔧

## Problem Identified
Your existing ad documents are missing the `isActive` and `priority` fields that the app expects. This is why ads aren't showing up.

## 🔧 Quick Fix Steps

### Step 1: Update Each Ad Document
For each ad document in your `ads` collection, add these missing fields:

#### For `healthy_bites_ad`:
1. Click on the document
2. Click **"Add field"**
3. Add these fields:

| Field Name | Type | Value |
|------------|------|-------|
| `isActive` | Boolean | `true` |
| `priority` | Number | `1` |

#### For `chefs_kitchen_ad`:
1. Click on the document
2. Click **"Add field"**
3. Add these fields:

| Field Name | Type | Value |
|------------|------|-------|
| `isActive` | Boolean | `true` |
| `priority` | Number | `1` |

#### For `organic_harvest_ad`:
1. Click on the document
2. Click **"Add field"**
3. Add these fields:

| Field Name | Type | Value |
|------------|------|-------|
| `isActive` | Boolean | `true` |
| `priority` | Number | `1` |

### Step 2: Verify Document Structure
After updating, each ad document should have these fields:

```json
{
  "adDescription": "...",
  "adImageUrl": "...",
  "adLinkUrl": "...",
  "adTitle": "...",
  "adType": 1,
  "advertiserLogoUrl": "...",
  "advertiserName": "...",
  "callToAction": "...",
  "clicks": 0,
  "impressions": 0,
  "isActive": true,        // ← NEW FIELD
  "priority": 1,           // ← NEW FIELD
  "isSponsored": true,
  "timestamp": 1752645691874
}
```

## 🚀 Test the App

1. **Run your app** after updating the documents
2. **Check logs** for these messages:
   - `"Loaded X active ads from Firebase"`
   - `"Creating feed with X posts, available ads: Y"`
   - `"Inserted ad 'Ad Title' at position Z"`

3. **Look for ads** in your feed - they should appear every 5 posts

## 🔍 Debug Logs to Watch

In Android Studio Logcat, filter by `"FEED_DEBUG"` and look for:

```
✅ SUCCESS MESSAGES:
- "Loaded X active ads from Firebase"
- "Creating feed with X posts, available ads: Y"
- "Inserted ad 'Ad Title' at position Z"

❌ ERROR MESSAGES:
- "Failed to load active ads from Firebase, trying all ads"
- "No ads available - feed will show posts only"
```

## 🎯 Expected Result

After adding the missing fields:
- ✅ Ads will load from Firebase
- ✅ Ads will appear every 5 posts in your feed
- ✅ You'll see "Sponsored" posts with images and call-to-action buttons
- ✅ Logs will show successful ad loading

## 📱 What You'll See

Your feed will now show:
```
Post 1
Post 2  
Post 3
Post 4
Post 5
[AD: Healthy Bites - "Get Recipes"]
Post 6
Post 7
...
```

---

**Update those documents and your ads will start showing! 🎉📱** 