package com.unavify.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.unavify.app.R;
import com.unavify.app.ui.auth.AuthViewModelJava;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeScreenJava extends AppCompatActivity {
    private AuthViewModelJava viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView subtitleText = findViewById(R.id.subtitle_text);
        TextView comingSoonText = findViewById(R.id.coming_soon_text);
        TextView featuresText = findViewById(R.id.features_text);
        Button signOutButton = findViewById(R.id.sign_out_button);
        Button editProfileButton = findViewById(R.id.edit_profile_button);

        welcomeText.setText("Welcome to Unavify! \uD83C\uDF7D\uFE0F");
        subtitleText.setText("Your Food Recipe Community");
        comingSoonText.setText("Coming Soon!");
        featuresText.setText("• Share your favorite recipes\n" +
                "• Participate in monthly contests\n" +
                "• Connect with food lovers\n" +
                "• Win exciting rewards");

        // Get the ViewModel using Hilt's ViewModelProvider
        viewModel = new ViewModelProvider(this).get(AuthViewModelJava.class);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.signOut();
                Intent intent = new Intent(HomeScreenJava.this, com.unavify.app.ui.auth.LoginScreenJava.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenJava.this, com.unavify.app.ui.profile.ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
} 