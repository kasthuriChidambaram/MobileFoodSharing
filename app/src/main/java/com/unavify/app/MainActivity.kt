package com.unavify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unavify.app.ui.auth.AuthViewModel
import com.unavify.app.ui.auth.LoginScreen
import com.unavify.app.ui.home.HomeScreen
import com.unavify.app.ui.theme.UnavifyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnavifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UnavifyApp()
                }
            }
        }
    }
}

@Composable
fun UnavifyApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (authState.isLoggedIn) {
        HomeScreen(
            onSignOut = { authViewModel.signOut() }
        )
    } else {
        LoginScreen(
            onLoginSuccess = {
                // Navigation to main app will be handled by state change
            }
        )
    }
}