package com.unavify.app.ui.profile;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.unavify.app.data.repository.AuthRepositoryJava;
import com.unavify.app.data.repository.UserProfileRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;



@HiltViewModel
public class ProfileViewModelJava extends ViewModel {
    
    private final UserProfileRepository repository;
    private final AuthRepositoryJava authRepository;
    private final ExecutorService executorService;
    
    // State management using LiveData
    private final MutableLiveData<Boolean> profileExists = new MutableLiveData<>();
    private final MutableLiveData<UsernameCheckState> usernameCheck = new MutableLiveData<>();
    private final MutableLiveData<ProfileSaveResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<ProfileSaveResult> signOutResult = new MutableLiveData<>();
    
    @Inject
    public ProfileViewModelJava(UserProfileRepository repository, AuthRepositoryJava authRepository) {
        this.repository = repository;
        this.authRepository = authRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    // Public getters for LiveData
    public LiveData<Boolean> getProfileExists() {
        return profileExists;
    }
    
    public LiveData<UsernameCheckState> getUsernameCheck() {
        return usernameCheck;
    }
    
    public LiveData<ProfileSaveResult> getSaveResult() {
        return saveResult;
    }
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    public LiveData<ProfileSaveResult> getSignOutResult() {
        return signOutResult;
    }
    
    public void checkProfileExists() {
        repository.isProfileComplete(isComplete -> profileExists.postValue(isComplete));
    }
    
    public void checkUsernameUnique(String username) {
        usernameCheck.setValue(new UsernameCheckState.Loading());
        repository.isUsernameUnique(username, isUnique -> {
            if (isUnique) {
                usernameCheck.postValue(new UsernameCheckState.Available());
            } else {
                usernameCheck.postValue(new UsernameCheckState.Taken("Username already taken"));
            }
        });
    }
    
    public void saveProfile(String username, Uri profileImageUri) {
        saveResult.setValue(new ProfileSaveResult.Loading());
        
        repository.saveProfile(username, profileImageUri, new UserProfileRepository.SaveProfileCallback() {
            @Override
            public void onSuccess() {
                saveResult.postValue(new ProfileSaveResult.Success());
            }
            @Override
            public void onFailure(String errorMessage) {
                saveResult.postValue(new ProfileSaveResult.Error(errorMessage));
            }
        });
    }
    
    public void clearSaveResult() {
        saveResult.setValue(null);
    }
    
    public void clearUsernameCheck() {
        usernameCheck.setValue(null);
    }
    
    public void loadUserProfile() {
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : null;
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (phoneNumber == null || userId == null) {
            userProfile.postValue(null);
            return;
        }
        // Fetch user profile from Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(phoneNumber)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                    String phone = documentSnapshot.getId();
                    UserProfile profile = new UserProfile(userId, username, phone, null, profileImageUrl);
                    userProfile.postValue(profile);
                } else {
                    userProfile.postValue(null);
                }
            })
            .addOnFailureListener(e -> userProfile.postValue(null));
    }
    
    public void signOut() {
        signOutResult.setValue(new ProfileSaveResult.Loading());
        
        executorService.execute(() -> {
            try {
                authRepository.signOut();
                // Sign out is always successful if no exception is thrown
                signOutResult.postValue(new ProfileSaveResult.Success());
            } catch (Exception e) {
                signOutResult.postValue(new ProfileSaveResult.Error(e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 