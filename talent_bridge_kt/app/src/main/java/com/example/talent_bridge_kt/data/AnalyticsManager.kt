package com.example.talent_bridge_kt.data

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {

    fun logScreenView(screenName: String, screenClass: String = "ComposeScreen") {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    fun logScreenDuration(screenName: String, durationMs: Long) {
        val params = Bundle().apply {
            putString("screen_name", screenName)
            putLong("duration_ms", durationMs)
        }
        Firebase.analytics.logEvent("screen_duration", params)
    }
}
