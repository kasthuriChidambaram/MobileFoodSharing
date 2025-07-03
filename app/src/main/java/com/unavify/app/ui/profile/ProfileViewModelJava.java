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
        executorService.execute(() -> {
            try {
                Object profile = repository.getCurrentUserProfile();
                boolean exists = profile != null;
                profileExists.postValue(exists);
            } catch (Exception e) {
                profileExists.postValue(false);
            }
        });
    }
    
    public void checkUsernameUnique(String username) {
        usernameCheck.setValue(new UsernameCheckState.Loading());
        
        executorService.execute(() -> {
            try {
                boolean isUnique = repository.isUsernameUnique(username);
                
                if (isUnique) {
                    usernameCheck.postValue(new UsernameCheckState.Available());
                } else {
                    usernameCheck.postValue(new UsernameCheckState.Taken("Username already taken"));
                }
            } catch (Exception e) {
                usernameCheck.postValue(new UsernameCheckState.Error("Failed to check username"));
            }
        });
    }
    
    public void saveProfile(String username, Uri profileImageUri) {
        saveResult.setValue(new ProfileSaveResult.Loading());
        
        executorService.execute(() -> {
            try {
                boolean success = repository.saveProfile(username, profileImageUri);
                
                if (success) {
                    saveResult.postValue(new ProfileSaveResult.Success());
                } else {
                    saveResult.postValue(new ProfileSaveResult.Error("Failed to save profile"));
                }
            } catch (Exception e) {
                saveResult.postValue(new ProfileSaveResult.Error(e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
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
        executorService.execute(() -> {
            try {
                // Get current user from Firebase Auth
                String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                
                if (userId != null) {
                    String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    
                    // Try to get profile from repository
                    Object profile = repository.getCurrentUserProfile();
                    
                    if (profile != null) {
                        // Convert to UserProfile if needed
                        UserProfile userProfile = new UserProfile(userId, "User", phoneNumber);
                        this.userProfile.postValue(userProfile);
                    } else {
                        // Create basic profile with available info
                        UserProfile userProfile = new UserProfile(userId, "User", phoneNumber);
                        this.userProfile.postValue(userProfile);
                    }
                }
            } catch (Exception e) {
                // Handle error - create basic profile
                String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown";
                String phoneNumber = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() : "unknown";
                
                UserProfile userProfile = new UserProfile(userId, "User", phoneNumber);
                this.userProfile.postValue(userProfile);
            }
        });
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