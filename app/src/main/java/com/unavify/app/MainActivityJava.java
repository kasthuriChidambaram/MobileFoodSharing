package com.unavify.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivityJava extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is logged in, go to SplashActivity (which will route to Home or EditProfile)
            startActivity(new Intent(this, SplashActivity.class));
        } else {
            // Not logged in, go to login
            startActivity(new Intent(this, com.unavify.app.ui.auth.LoginScreenJava.class));
        }
        finish();
    }
    // In a real app, you would check login state and navigate accordingly
} 