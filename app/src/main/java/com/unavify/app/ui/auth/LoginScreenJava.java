package com.unavify.app.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.unavify.app.R;
import com.unavify.app.SplashActivity;
import dagger.hilt.android.AndroidEntryPoint;
import android.util.Log;

@AndroidEntryPoint
public class LoginScreenJava extends AppCompatActivity {
    
    private AuthViewModelJava authViewModel;
    
    // UI Components
    private EditText phoneNumberEditText;
    private EditText otpEditText;
    private Button sendOtpButton;
    private Button verifyOtpButton;
    private Button resendOtpButton;
    private Button backButton;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextView errorTextView;
    
    // State tracking
    private boolean isOtpScreen = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LoginScreenJava", "onCreate: user=" + FirebaseAuth.getInstance().getCurrentUser());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d("LoginScreenJava", "User is logged in, redirecting to SplashActivity");
            startActivity(new Intent(this, SplashActivity.class));
            finish();
            return;
        }

        Log.d("LoginScreenJava", "User is NOT logged in, showing login UI");
        setContentView(R.layout.activity_login);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModelJava.class);
        
        // Initialize UI components
        initializeViews();
        setupListeners();
        observeViewModel();
    }
    
    private void initializeViews() {
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        otpEditText = findViewById(R.id.otp_edit_text);
        sendOtpButton = findViewById(R.id.send_otp_button);
        verifyOtpButton = findViewById(R.id.verify_otp_button);
        resendOtpButton = findViewById(R.id.resend_otp_button);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progress_bar);
        titleTextView = findViewById(R.id.title_text_view);
        subtitleTextView = findViewById(R.id.subtitle_text_view);
        errorTextView = findViewById(R.id.error_text_view);
        
        // Initially show phone number screen
        showPhoneNumberScreen();
    }
    
    private void setupListeners() {
        // Phone number input listener
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // Enable/disable send button based on phone number length
                sendOtpButton.setEnabled(s.length() == 10);
            }
        });
        
        // OTP input listener
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // Enable/disable verify button based on OTP length
                verifyOtpButton.setEnabled(s.length() == 6);
            }
        });
        
        // Button click listeners
        sendOtpButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString();
            authViewModel.sendOtp(phoneNumber, this);
        });
        
        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString();
            authViewModel.verifyOtp(otp);
        });
        
        resendOtpButton.setOnClickListener(v -> {
            authViewModel.resendOtp(this);
        });
        
        backButton.setOnClickListener(v -> {
            authViewModel.goBackToPhoneInput();
        });
    }
    
    private void observeViewModel() {
        // Observe UI state changes
        authViewModel.getUiState().observe(this, uiState -> {
            if (uiState != null) {
                updateUI(uiState);
            }
        });
    }
    
    private void updateUI(AuthViewModelJava.AuthUiState uiState) {
        // Update loading state
        progressBar.setVisibility(uiState.isLoading() ? View.VISIBLE : View.GONE);
        
        // Update error state
        if (uiState.getError() != null) {
            errorTextView.setText(uiState.getError());
            errorTextView.setVisibility(View.VISIBLE);
        } else {
            errorTextView.setVisibility(View.GONE);
        }
        
        // Update screen based on OTP screen state
        if (uiState.isShowOtpScreen() && !isOtpScreen) {
            showOtpScreen(uiState.getPhoneNumber());
        } else if (!uiState.isShowOtpScreen() && isOtpScreen) {
            showPhoneNumberScreen();
        }
        
        // Handle login success
        if (uiState.isLoggedIn()) {
            onLoginSuccess();
        }
    }
    
    private void showPhoneNumberScreen() {
        isOtpScreen = false;
        
        // Show phone number input
        phoneNumberEditText.setVisibility(View.VISIBLE);
        sendOtpButton.setVisibility(View.VISIBLE);
        
        // Hide OTP input
        otpEditText.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        resendOtpButton.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        subtitleTextView.setVisibility(View.GONE);
        
        // Update title
        titleTextView.setText("Enter your phone number");
        
        // Clear fields
        phoneNumberEditText.setText("");
        otpEditText.setText("");
    }
    
    private void showOtpScreen(String phoneNumber) {
        isOtpScreen = true;
        
        // Hide phone number input
        phoneNumberEditText.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.GONE);
        
        // Show OTP input
        otpEditText.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.VISIBLE);
        resendOtpButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        subtitleTextView.setVisibility(View.VISIBLE);
        
        // Update title and subtitle
        titleTextView.setText("Enter OTP");
        subtitleTextView.setText("OTP sent to +91" + phoneNumber);
        
        // Clear OTP field
        otpEditText.setText("");
    }
    
    private void onLoginSuccess() {
        
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Only clear error if authViewModel is initialized
        if (authViewModel != null) {
            authViewModel.clearError();
        }
    }
} 