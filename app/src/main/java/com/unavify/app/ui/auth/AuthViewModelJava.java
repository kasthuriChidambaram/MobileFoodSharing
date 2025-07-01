package com.unavify.app.ui.auth;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.unavify.app.data.repository.AuthRepositoryJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModelJava extends ViewModel {

    private final AuthRepositoryJava authRepository;
    private final ExecutorService executorService;
    
    // State management using LiveData (Java equivalent of StateFlow)
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>();
    
    @Inject
    public AuthViewModelJava(AuthRepositoryJava authRepository) {
        this.authRepository = authRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Initialize with default state
        uiState.setValue(new AuthUiState());
        
        // Check authentication state on initialization
        checkAuthState();
    }
    
    // Public getter for UI state
    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }
    
    private void checkAuthState() {
        boolean isLoggedIn = authRepository.isUserLoggedIn();
        
        if (isLoggedIn) {
            // User is already logged in
            AuthUiState currentState = uiState.getValue();
            if (currentState != null) {
                AuthUiState newState = new AuthUiState.Builder()
                    .setLoggedIn(true)
                    .setLoading(false)
                    .build();
                uiState.setValue(newState);
            }
        }
    }
    
    public void sendOtp(String phoneNumber, Activity activity) {
        // Validate Indian phone number (10 digits)
        if (phoneNumber == null || phoneNumber.length() != 10) {
            setError("Please enter a valid 10-digit Indian phone number");
            return;
        }
        
        // Check if it's a valid Indian mobile number (starts with 6, 7, 8, or 9)
        Pattern indianMobilePattern = Pattern.compile("^[6-9]\\d{9}$");
        if (!indianMobilePattern.matcher(phoneNumber).matches()) {
            setError("Please enter a valid Indian mobile number");
            return;
        }
        
        // Format phone number with +91 country code for Firebase
        String formattedPhoneNumber = "+91" + phoneNumber;
        
        // Set loading state
        setLoading(true);
        clearError();
        
        // Execute OTP sending in background
        executorService.execute(() -> {
            try {
                // This would need to be adapted based on your AuthRepository implementation
                // For now, showing the structure
                authRepository.sendOtp(formattedPhoneNumber, activity, new AuthRepositoryJava.OtpCallback() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        AuthUiState newState = new AuthUiState.Builder()
                            .setLoading(false)
                            .setShowOtpScreen(true)
                            .setPhoneNumber(phoneNumber)
                            .setVerificationId(verificationId)
                            .setResendToken(token)
                            .build();
                        uiState.postValue(newState);
                    }
                    
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        AuthUiState newState = new AuthUiState.Builder()
                            .setLoading(false)
                            .setLoggedIn(true)
                            .build();
                        uiState.postValue(newState);
                    }
                    
                    @Override
                    public void onVerificationFailed(String message) {
                        setError(message);
                    }
                });
            } catch (Exception e) {
                setError("Failed to send OTP: " + e.getMessage());
            }
        });
    }
    
    public void verifyOtp(String otp) {
        AuthUiState currentState = uiState.getValue();
        if (currentState == null || currentState.getVerificationId() == null) {
            setError("Verification session expired. Please try again.");
            return;
        }
        
        setLoading(true);
        clearError();
        
        executorService.execute(() -> {
            try {
                authRepository.verifyOtp(currentState.getVerificationId(), otp, new AuthRepositoryJava.OtpResultCallback() {
                    @Override
                    public void onSuccess() {
                        AuthUiState newState = new AuthUiState.Builder()
                            .setLoading(false)
                            .setLoggedIn(true)
                            .build();
                        uiState.postValue(newState);
                    }
                    
                    @Override
                    public void onError(String message) {
                        setError(message);
                    }
                });
            } catch (Exception e) {
                setError("Failed to verify OTP: " + e.getMessage());
            }
        });
    }
    
    public void resendOtp(Activity activity) {
        AuthUiState currentState = uiState.getValue();
        if (currentState == null || currentState.getPhoneNumber() == null || currentState.getResendToken() == null) {
            setError("Unable to resend OTP. Please try again.");
            return;
        }
        
        setLoading(true);
        clearError();
        
        executorService.execute(() -> {
            try {
                String formattedPhoneNumber = "+91" + currentState.getPhoneNumber();
                authRepository.resendOtp(formattedPhoneNumber, currentState.getResendToken(), activity, new AuthRepositoryJava.OtpCallback() {
                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        AuthUiState newState = new AuthUiState.Builder()
                            .setLoading(false)
                            .setVerificationId(verificationId)
                            .setResendToken(token)
                            .build();
                        uiState.postValue(newState);
                    }
                    
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        AuthUiState newState = new AuthUiState.Builder()
                            .setLoading(false)
                            .setLoggedIn(true)
                            .build();
                        uiState.postValue(newState);
                    }
                    
                    @Override
                    public void onVerificationFailed(String message) {
                        setError(message);
                    }
                });
            } catch (Exception e) {
                setError("Failed to resend OTP: " + e.getMessage());
            }
        });
    }
    
    public void clearError() {
        AuthUiState currentState = uiState.getValue();
        if (currentState != null) {
            AuthUiState newState = new AuthUiState.Builder()
                .setLoading(currentState.isLoading())
                .setLoggedIn(currentState.isLoggedIn())
                .setShowOtpScreen(currentState.isShowOtpScreen())
                .setPhoneNumber(currentState.getPhoneNumber())
                .setVerificationId(currentState.getVerificationId())
                .setResendToken(currentState.getResendToken())
                .setError(null)
                .build();
            uiState.setValue(newState);
        }
    }
    
    public void goBackToPhoneInput() {
        AuthUiState newState = new AuthUiState.Builder()
            .setShowOtpScreen(false)
            .setVerificationId(null)
            .setResendToken(null)
            .setPhoneNumber(null)
            .build();
        uiState.setValue(newState);
    }
    
    public void signOut() {
        authRepository.signOut();
        uiState.setValue(new AuthUiState());
    }
    
    private void setLoading(boolean loading) {
        AuthUiState currentState = uiState.getValue();
        if (currentState != null) {
            AuthUiState newState = new AuthUiState.Builder()
                .setLoading(loading)
                .setLoggedIn(currentState.isLoggedIn())
                .setShowOtpScreen(currentState.isShowOtpScreen())
                .setPhoneNumber(currentState.getPhoneNumber())
                .setVerificationId(currentState.getVerificationId())
                .setResendToken(currentState.getResendToken())
                .setError(currentState.getError())
                .build();
            uiState.setValue(newState);
        }
    }
    
    private void setError(String error) {
        AuthUiState currentState = uiState.getValue();
        if (currentState != null) {
            AuthUiState newState = new AuthUiState.Builder()
                .setLoading(false)
                .setLoggedIn(currentState.isLoggedIn())
                .setShowOtpScreen(currentState.isShowOtpScreen())
                .setPhoneNumber(currentState.getPhoneNumber())
                .setVerificationId(currentState.getVerificationId())
                .setResendToken(currentState.getResendToken())
                .setError(error)
                .build();
            uiState.setValue(newState);
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
    

    
    // UI State class using Builder pattern for immutability
    public static class AuthUiState {
        private final boolean isLoading;
        private final boolean isLoggedIn;
        private final boolean showOtpScreen;
        private final String phoneNumber;
        private final String verificationId;
        private final PhoneAuthProvider.ForceResendingToken resendToken;
        private final String error;
        
        private AuthUiState(Builder builder) {
            this.isLoading = builder.isLoading;
            this.isLoggedIn = builder.isLoggedIn;
            this.showOtpScreen = builder.showOtpScreen;
            this.phoneNumber = builder.phoneNumber;
            this.verificationId = builder.verificationId;
            this.resendToken = builder.resendToken;
            this.error = builder.error;
        }
        
        // Default constructor
        public AuthUiState() {
            this.isLoading = false;
            this.isLoggedIn = false;
            this.showOtpScreen = false;
            this.phoneNumber = null;
            this.verificationId = null;
            this.resendToken = null;
            this.error = null;
        }
        
        // Getters
        public boolean isLoading() { return isLoading; }
        public boolean isLoggedIn() { return isLoggedIn; }
        public boolean isShowOtpScreen() { return showOtpScreen; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getVerificationId() { return verificationId; }
        public PhoneAuthProvider.ForceResendingToken getResendToken() { return resendToken; }
        public String getError() { return error; }
        
        // Builder class
        public static class Builder {
            private boolean isLoading = false;
            private boolean isLoggedIn = false;
            private boolean showOtpScreen = false;
            private String phoneNumber = null;
            private String verificationId = null;
            private PhoneAuthProvider.ForceResendingToken resendToken = null;
            private String error = null;
            
            public Builder setLoading(boolean loading) {
                this.isLoading = loading;
                return this;
            }
            
            public Builder setLoggedIn(boolean loggedIn) {
                this.isLoggedIn = loggedIn;
                return this;
            }
            
            public Builder setShowOtpScreen(boolean showOtpScreen) {
                this.showOtpScreen = showOtpScreen;
                return this;
            }
            
            public Builder setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
                return this;
            }
            
            public Builder setVerificationId(String verificationId) {
                this.verificationId = verificationId;
                return this;
            }
            
            public Builder setResendToken(PhoneAuthProvider.ForceResendingToken resendToken) {
                this.resendToken = resendToken;
                return this;
            }
            
            public Builder setError(String error) {
                this.error = error;
                return this;
            }
            
            public AuthUiState build() {
                return new AuthUiState(this);
            }
        }
    }
} 