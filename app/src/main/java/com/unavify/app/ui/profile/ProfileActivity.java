package com.unavify.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.unavify.app.R;
import com.unavify.app.ui.home.HomeScreenJava;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {
    
    private ProfileViewModelJava profileViewModel;
    
    // UI Components
    private TextView welcomeTextView;
    private TextView phoneNumberTextView;
    private Button editProfileButton;
    private Button homeButton;
    private Button signOutButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize ViewModel
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModelJava.class);
        
        // Initialize UI components
        initializeViews();
        setupListeners();
        observeViewModel();
        
        // Load user profile
        profileViewModel.loadUserProfile();
    }
    
    private void initializeViews() {
        welcomeTextView = findViewById(R.id.welcome_text_view);
        phoneNumberTextView = findViewById(R.id.phone_number_text_view);
        editProfileButton = findViewById(R.id.edit_profile_button);
        homeButton = findViewById(R.id.home_button);
        signOutButton = findViewById(R.id.sign_out_button);
    }
    
    private void setupListeners() {
        editProfileButton.setOnClickListener(v -> {
            String username = null;
            String profileImageUrl = null;
            if (profileViewModel.getUserProfile().getValue() != null) {
                username = profileViewModel.getUserProfile().getValue().getUsername();
                profileImageUrl = profileViewModel.getUserProfile().getValue().getProfileImageUrl();
            }
            Intent intent = new Intent(this, EditProfileActivity.class);
            if (username != null) intent.putExtra("username", username);
            if (profileImageUrl != null) intent.putExtra("profileImageUrl", profileImageUrl);
            startActivity(intent);
        });
        
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeScreenJava.class);
            startActivity(intent);
        });
        
        signOutButton.setOnClickListener(v -> {
            profileViewModel.signOut();
        });
    }
    
    private void observeViewModel() {
        // Observe user profile data
        profileViewModel.getUserProfile().observe(this, userProfile -> {
            if (userProfile != null) {
                updateProfileUI(userProfile);
            }
        });
        
        // Observe sign out result
        profileViewModel.getSignOutResult().observe(this, signOutResult -> {
            if (signOutResult instanceof ProfileSaveResult.Success) {
                onSignOutSuccess();
            } else if (signOutResult instanceof ProfileSaveResult.Error) {
                ProfileSaveResult.Error error = (ProfileSaveResult.Error) signOutResult;
                Toast.makeText(this, "Sign out failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateProfileUI(UserProfile userProfile) {
        if (userProfile.getUsername() != null && !userProfile.getUsername().isEmpty()) {
            welcomeTextView.setText("Welcome, " + userProfile.getUsername() + "!");
        } else {
            welcomeTextView.setText("Welcome!");
        }
        
        if (userProfile.getPhoneNumber() != null) {
            phoneNumberTextView.setText("Phone: " + userProfile.getPhoneNumber());
        }
    }
    
    private void onSignOutSuccess() {
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate back to login screen and clear activity stack
        Intent intent = new Intent(this, com.unavify.app.ui.auth.LoginScreenJava.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 