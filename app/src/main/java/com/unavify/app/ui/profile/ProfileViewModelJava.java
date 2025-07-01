package com.unavify.app.ui.profile;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.unavify.app.data.repository.UserProfileRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;



@HiltViewModel
public class ProfileViewModelJava extends ViewModel {
    
    private final UserProfileRepository repository;
    private final ExecutorService executorService;
    
    // State management using LiveData
    private final MutableLiveData<Boolean> profileExists = new MutableLiveData<>();
    private final MutableLiveData<UsernameCheckState> usernameCheck = new MutableLiveData<>();
    private final MutableLiveData<ProfileSaveResult> saveResult = new MutableLiveData<>();
    
    @Inject
    public ProfileViewModelJava(UserProfileRepository repository) {
        this.repository = repository;
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
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 