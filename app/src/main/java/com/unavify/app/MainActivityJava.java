package com.unavify.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.unavify.app.ui.auth.LoginScreenJava;
import com.unavify.app.ui.home.HomeScreenJava;

public class MainActivityJava extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For demonstration, always show LoginScreenJava first
        startActivity(new Intent(this, LoginScreenJava.class));
        finish();
    }
    // In a real app, you would check login state and navigate accordingly
} 