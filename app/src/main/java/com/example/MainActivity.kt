package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase.configureAppCheck
import com.example.ui.MainAppShell
import com.example.ui.auth.SignInScreen
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStringsProvider
import com.example.ui.theme.AskariTakafulTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "AskariStartup"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A missing google-services.json must not crash the whole application.
        val firebaseReady = try {
            FirebaseApp.initializeApp(this) != null
        } catch (error: Exception) {
            Log.e(TAG, "Firebase initialization failed; opening setup screen.", error)
            false
        }

        if (firebaseReady) {
            configureAppCheck(this, intent)
        } else {
            Log.e(TAG, "Firebase is not configured. Cloud sign-in and cloud data are unavailable.")
        }

        enableEdgeToEdge()
        setContent {
            AppNavigation(firebaseConfigured = firebaseReady)
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
fun AppNavigation(firebaseConfigured: Boolean) {
    var language by rememberSaveable { mutableStateOf(AppLanguage.URDU) }
    val isUrdu = language == AppLanguage.URDU
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
    val toggleLanguage = {
        language = if (language == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
    }

    AskariTakafulTheme(isUrdu = isUrdu) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            if (firebaseConfigured) {
                FirebaseReadyNavigation(
                    language = language,
                    onToggleLanguage = toggleLanguage
                )
            } else {
                FirebaseSetupRequiredScreen(
                    language = language,
                    onToggleLanguage = toggleLanguage
                )
            }
        }
    }
}

@Composable
private fun FirebaseReadyNavigation(
    language: AppLanguage,
    onToggleLanguage: () -> Unit
) {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val strings = AppStringsProvider.get(language)
    val user = currentUser

    if (user == null) {
        SignInScreen(
            strings = strings,
            language = language,
            onToggleLanguage = onToggleLanguage,
            onAuthSuccess = {}
        )
    } else {
        key(user.uid) {
            MainAppShell(
                currentUserId = user.uid,
                strings = strings,
                language = language,
                onToggleLanguage = onToggleLanguage
            )
        }
    }
}

@Composable
private fun FirebaseSetupRequiredScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit
) {
    val isUrdu = language == AppLanguage.URDU
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isUrdu) "Askari Takaful" else "Askari Takaful",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (isUrdu)
                    "ایپ کی بنیادی کنفیگریشن ابھی مکمل نہیں۔ اسی وجہ سے لاگ اِن اور آن لائن ڈیٹا دستیاب نہیں۔ براہِ کرم ایپ بنانے والے سے Firebase کنفیگریشن مکمل کروائیں۔"
                else
                    "The app setup is not complete yet. Sign-in and cloud data are unavailable until the Firebase configuration is added to this project.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onToggleLanguage,
                modifier = Modifier.testTag("firebase_setup_required")
            ) {
                Text(if (isUrdu) "English" else "اردو")
            }
        }
    }
}
