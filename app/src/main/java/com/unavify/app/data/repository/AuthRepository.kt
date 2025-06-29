package com.unavify.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun getCurrentUser() = firebaseAuth.currentUser

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    suspend fun sendOtp(phoneNumber: String, activity: android.app.Activity): Flow<OtpResult> = callbackFlow {
        Log.d("AuthRepository", "sendOtp called with phoneNumber: $phoneNumber")
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("AuthRepository", "onVerificationCompleted called")
                trySend(OtpResult.VerificationCompleted(credential))
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Log.e("AuthRepository", "onVerificationFailed: ${e.message}", e)
                trySend(OtpResult.VerificationFailed(e.message ?: "Verification failed"))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("AuthRepository", "onCodeSent called with verificationId: $verificationId")
                trySend(OtpResult.CodeSent(verificationId, token))
            }
        }

        try {
            Log.d("AuthRepository", "Building PhoneAuthOptions")
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            Log.d("AuthRepository", "Calling PhoneAuthProvider.verifyPhoneNumber")
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception in sendOtp: ${e.message}", e)
            trySend(OtpResult.VerificationFailed(e.message ?: "Failed to send OTP"))
        }
        
        awaitClose()
    }

    suspend fun verifyOtp(verificationId: String, otp: String): OtpResult {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            firebaseAuth.signInWithCredential(credential).await()
            OtpResult.Success
        } catch (e: Exception) {
            OtpResult.Error(e.message ?: "OTP verification failed")
        }
    }

    suspend fun resendOtp(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken,
        activity: android.app.Activity
    ): Flow<OtpResult> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(OtpResult.VerificationCompleted(credential))
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                trySend(OtpResult.VerificationFailed(e.message ?: "Verification failed"))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(OtpResult.CodeSent(verificationId, token))
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        awaitClose()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

sealed class OtpResult {
    object Success : OtpResult()
    data class Error(val message: String) : OtpResult()
    data class CodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    ) : OtpResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : OtpResult()
    data class VerificationFailed(val message: String) : OtpResult()
} 