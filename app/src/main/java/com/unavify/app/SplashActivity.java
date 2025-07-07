package com.unavify.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.unavify.app.data.repository.UserProfileRepository;
import com.unavify.app.ui.home.HomeScreenJava;
import com.unavify.app.ui.profile.EditProfileActivity;
import com.unavify.app.ui.auth.LoginScreenJava;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SplashActivity", "onCreate: user=" + FirebaseAuth.getInstance().getCurrentUser());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            UserProfileRepository repo = new UserProfileRepository();
            repo.isProfileComplete(isComplete -> {
                if (isComplete) {
                    // Returning user: go directly to Home
                    startActivity(new Intent(this, HomeScreenJava.class));
                } else {
                    // New user: go directly to Edit Profile (no welcome screen)
                    startActivity(new Intent(this, EditProfileActivity.class));
                }
                finish();
            });
        } else {
            Log.d("SplashActivity", "User not logged in, going to LoginScreenJava");
            startActivity(new Intent(this, LoginScreenJava.class));
            finish();
        }
    }
} 