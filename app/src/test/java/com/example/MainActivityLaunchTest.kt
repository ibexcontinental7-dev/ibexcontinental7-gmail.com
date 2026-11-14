package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.firebase.FirebaseApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class MainActivityLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Config(sdk = [29])
    fun launchMainActivity_api29_rendersWithoutCrash() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_language_toggle").assertExists()
        composeRule.onNodeWithTag("google_sign_in_button").assertExists()
    }

    @Test
    @Config(sdk = [36])
    fun launchMainActivity_api36_rendersWithoutCrash() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_language_toggle").assertExists()
        composeRule.onNodeWithTag("google_sign_in_button").assertExists()
    }

    @Test
    @Config(sdk = [24])
    fun launchMainActivity_api24_rendersWithoutCrash() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_language_toggle").assertExists()
    }
}
