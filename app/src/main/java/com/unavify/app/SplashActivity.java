package com.unavify.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SplashActivity", "onCreate: user=" + FirebaseAuth.getInstance().getCurrentUser());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is logged in, go to profile
            startActivity(new Intent(this, com.unavify.app.ui.profile.ProfileActivity.class));
        } else {
            // Not logged in, go to login
            startActivity(new Intent(this, com.unavify.app.ui.auth.LoginScreenJava.class));
        }
        finish();
    }
} 