package com.example.talent_bridge_kt.data.analytics

import com.example.talent_bridge_kt.domain.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsTracker : AnalyticsTracker {
    override fun login(method: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    override fun signUp(method: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    override fun screen(name: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, name)
        }
    }
    override fun event(name: String, params: Map<String, String?>) {
        Firebase.analytics.logEvent(name) {
            params.forEach { (k, v) -> v?.let { param(k, it) } }
        }
    }
    override fun identify(userId: String?, role: String?) {
        Firebase.analytics.setUserId(userId)
        role?.let { Firebase.analytics.setUserProperty("role", it) }
    }
}
