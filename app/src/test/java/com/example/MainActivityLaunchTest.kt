package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
    fun launchMainActivity_api29_showsSetupInsteadOfCrashing() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("firebase_setup_required").assertExists()
    }

    @Test
    @Config(sdk = [36])
    fun launchMainActivity_api36_showsSetupInsteadOfCrashing() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("firebase_setup_required").assertExists()
    }

    @Test
    @Config(sdk = [24])
    fun launchMainActivity_api24_showsSetupInsteadOfCrashing() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("firebase_setup_required").assertExists()
    }
}
