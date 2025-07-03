package com.unavify.app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.unavify.app.R;
import com.unavify.app.ui.home.HomeScreenJava;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText usernameEditText;
    private ImageView profileImageView;
    private Button selectImageButton;
    private Button saveButton;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private ProfileViewModelJava profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        usernameEditText = findViewById(R.id.edit_text_username);
        profileImageView = findViewById(R.id.image_view_profile);
        selectImageButton = findViewById(R.id.button_select_image);
        saveButton = findViewById(R.id.button_save);
        progressBar = findViewById(R.id.progress_bar);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModelJava.class);

        // Get current username and image from intent
        String currentUsername = getIntent().getStringExtra("username");
        String currentImageUrl = getIntent().getStringExtra("profileImageUrl");
        if (currentUsername != null) {
            usernameEditText.setText(currentUsername);
        }
        // TODO: Load currentImageUrl into profileImageView if not null

        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProfile());

        profileViewModel.getSaveResult().observe(this, result -> {
            if (result instanceof ProfileSaveResult.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                if (result instanceof ProfileSaveResult.Success) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    goToHome();
                } else if (result instanceof ProfileSaveResult.Error) {
                    String message = ((ProfileSaveResult.Error) result).getMessage();
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void saveProfile() {
        String username = usernameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return;
        }
        profileViewModel.saveProfile(username, selectedImageUri);
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeScreenJava.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 