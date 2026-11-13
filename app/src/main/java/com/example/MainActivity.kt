package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase.configureAppCheck
import com.example.ui.MainAppShell
import com.example.ui.auth.SignInScreen
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStringsProvider
import com.example.ui.theme.AskariTakafulTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check before UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var language by rememberSaveable { mutableStateOf(AppLanguage.URDU) }
    val isUrdu = language == AppLanguage.URDU
    val strings = AppStringsProvider.get(language)
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    val toggleLanguage = {
        language = if (language == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
    }

    AskariTakafulTheme(isUrdu = isUrdu) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            val user = currentUser
            if (user == null) {
                SignInScreen(
                    strings = strings,
                    language = language,
                    onToggleLanguage = toggleLanguage,
                    onAuthSuccess = {}
                )
            } else {
                key(user.uid) {
                    MainAppShell(
                        currentUserId = user.uid,
                        strings = strings,
                        language = language,
                        onToggleLanguage = toggleLanguage
                    )
                }
            }
        }
    }
}
