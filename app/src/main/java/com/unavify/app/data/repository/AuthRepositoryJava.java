package com.unavify.app.data.repository;

import android.app.Activity;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryJava {
    private final FirebaseAuth firebaseAuth;

    @Inject
    public AuthRepositoryJava(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public Object getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void sendOtp(String phoneNumber, Activity activity, OtpCallback callback) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                callback.onVerificationCompleted(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                callback.onVerificationFailed(e.getMessage() != null ? e.getMessage() : "Verification failed");
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                callback.onCodeSent(verificationId, token);
            }
        };
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyOtp(String verificationId, String otp, OtpResultCallback callback) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "OTP verification failed"));
        } catch (Exception e) {
            callback.onError(e.getMessage() != null ? e.getMessage() : "OTP verification failed");
        }
    }

    public void resendOtp(String phoneNumber, PhoneAuthProvider.ForceResendingToken token, Activity activity, OtpCallback callback) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                callback.onVerificationCompleted(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                callback.onVerificationFailed(e.getMessage() != null ? e.getMessage() : "Verification failed");
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                callback.onCodeSent(verificationId, token);
            }
        };
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public interface OtpCallback {
        void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token);
        void onVerificationCompleted(PhoneAuthCredential credential);
        void onVerificationFailed(String message);
    }
    public interface OtpResultCallback {
        void onSuccess();
        void onError(String message);
    }
} 