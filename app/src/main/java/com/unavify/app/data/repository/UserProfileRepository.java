package com.unavify.app.data.repository;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserProfileRepository {
    public interface SaveProfileCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface ProfileCheckCallback {
        void onResult(boolean isComplete);
    }

    public interface UsernameUniqueCallback {
        void onResult(boolean isUnique);
    }

    @Inject
    public UserProfileRepository() {
        // Initialize repository
    }

    public void isUsernameUnique(String username, UsernameUniqueCallback callback) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                boolean isUnique = querySnapshot.isEmpty();
                callback.onResult(isUnique);
            })
            .addOnFailureListener(e -> callback.onResult(false));
    }

    public void saveProfile(String username, Uri profileImageUri, SaveProfileCallback callback) {
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : null;
        if (phoneNumber == null) {
            callback.onFailure("User not authenticated");
            return;
        }
        
        if (profileImageUri != null) {
            // Upload new image to Firebase Storage
            String fileName = "profile_images/" + phoneNumber + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);
            storageRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    saveProfileToFirestore(username, downloadUri.toString(), phoneNumber, callback);
                }).addOnFailureListener(e -> {
                    callback.onFailure("Failed to get image URL: " + e.getMessage());
                }))
                .addOnFailureListener(e -> {
                    callback.onFailure("Image upload failed: " + e.getMessage());
                });
        } else {
            // No new image selected, preserve existing image URL
            preserveExistingImageAndSaveProfile(username, phoneNumber, callback);
        }
    }

    private void preserveExistingImageAndSaveProfile(String username, String phoneNumber, SaveProfileCallback callback) {
        // First get the existing profile to preserve the image URL
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(phoneNumber)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String existingImageUrl = null;
                if (documentSnapshot.exists()) {
                    existingImageUrl = documentSnapshot.getString("profileImageUrl");
                }
                // Save profile with existing image URL (or null if no existing image)
                saveProfileToFirestore(username, existingImageUrl, phoneNumber, callback);
            })
            .addOnFailureListener(e -> {
                // If we can't get existing profile, save with null image URL
                saveProfileToFirestore(username, null, phoneNumber, callback);
            });
    }

    private void saveProfileToFirestore(String username, String imageUrl, String phoneNumber, SaveProfileCallback callback) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("username", username);
        profileData.put("profileImageUrl", imageUrl);
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(phoneNumber)
            .set(profileData)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure("Failed to save profile: " + e.getMessage()));
    }

    // Check if profile is complete (username not empty, profileImageUrl not null)
    public void isProfileComplete(ProfileCheckCallback callback) {
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : null;
        if (phoneNumber == null) {
            callback.onResult(false);
            return;
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(phoneNumber)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    boolean isComplete = username != null && !username.isEmpty();
                    callback.onResult(isComplete);
                } else {
                    callback.onResult(false);
                }
            })
            .addOnFailureListener(e -> callback.onResult(false));
    }
} 