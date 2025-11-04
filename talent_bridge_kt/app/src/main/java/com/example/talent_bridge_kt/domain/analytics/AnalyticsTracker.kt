package com.example.talent_bridge_kt.domain.analytics

interface AnalyticsTracker {
    fun login(method: String)
    fun signUp(method: String)
    fun screen(name: String)
    fun event(name: String, params: Map<String, String?> = emptyMap())
    fun identify(userId: String?, role: String? = null)
}
