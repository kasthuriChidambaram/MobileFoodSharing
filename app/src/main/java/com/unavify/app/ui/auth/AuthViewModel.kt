package com.unavify.app.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.unavify.app.data.repository.AuthRepository
import com.unavify.app.data.repository.OtpResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (authRepository.isUserLoggedIn()) {
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                isLoading = false
            )
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        Log.d("AuthViewModel", "sendOtp called with phoneNumber: $phoneNumber")
        
        // Validate Indian phone number (10 digits)
        if (phoneNumber.length != 10) {
            Log.e("AuthViewModel", "Invalid phone number length: ${phoneNumber.length}")
            _uiState.value = _uiState.value.copy(
                error = "Please enter a valid 10-digit Indian phone number"
            )
            return
        }

        // Check if it's a valid Indian mobile number (starts with 6, 7, 8, or 9)
        if (!phoneNumber.matches(Regex("^[6-9]\\d{9}$"))) {
            Log.e("AuthViewModel", "Invalid Indian mobile number format: $phoneNumber")
            _uiState.value = _uiState.value.copy(
                error = "Please enter a valid Indian mobile number"
            )
            return
        }

        // Format phone number with +91 country code for Firebase
        val formattedPhoneNumber = "+91$phoneNumber"
        Log.d("AuthViewModel", "Formatted phone number for Firebase: $formattedPhoneNumber")

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            Log.d("AuthViewModel", "Calling authRepository.sendOtp")
            authRepository.sendOtp(formattedPhoneNumber, activity).collect { result ->
                Log.d("AuthViewModel", "Received result: $result")
                when (result) {
                    is OtpResult.CodeSent -> {
                        Log.d("AuthViewModel", "Code sent successfully, verificationId: ${result.verificationId}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showOtpScreen = true,
                            verificationId = result.verificationId,
                            resendToken = result.token,
                            phoneNumber = formattedPhoneNumber
                        )
                    }
                    is OtpResult.VerificationFailed -> {
                        Log.e("AuthViewModel", "Verification failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is OtpResult.VerificationCompleted -> {
                        Log.d("AuthViewModel", "Auto-verification completed")
                        // Auto-verification completed
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                    }
                    else -> {
                        Log.e("AuthViewModel", "Unknown result type: $result")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Something went wrong"
                        )
                    }
                }
            }
        }
    }

    fun verifyOtp(otp: String) {
        if (otp.length != 6) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a valid 6-digit OTP"
            )
            return
        }

        val verificationId = _uiState.value.verificationId
        if (verificationId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Verification ID not found"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = authRepository.verifyOtp(verificationId, otp)
            when (result) {
                is OtpResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is OtpResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Verification failed"
                    )
                }
            }
        }
    }

    fun resendOtp(activity: Activity) {
        val phoneNumber = _uiState.value.phoneNumber
        val resendToken = _uiState.value.resendToken

        if (phoneNumber == null || resendToken == null) {
            _uiState.value = _uiState.value.copy(
                error = "Cannot resend OTP"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            authRepository.resendOtp(phoneNumber, resendToken, activity).collect { result ->
                when (result) {
                    is OtpResult.CodeSent -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            verificationId = result.verificationId,
                            resendToken = result.token
                        )
                    }
                    is OtpResult.VerificationFailed -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is OtpResult.VerificationCompleted -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to resend OTP"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun goBackToPhoneInput() {
        _uiState.value = _uiState.value.copy(
            showOtpScreen = false,
            verificationId = null,
            resendToken = null,
            phoneNumber = null
        )
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val showOtpScreen: Boolean = false,
    val phoneNumber: String? = null,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val error: String? = null
) 