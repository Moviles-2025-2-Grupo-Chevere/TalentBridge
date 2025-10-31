package com.example.talent_bridge_kt.data.local

import com.example.talent_bridge_kt.domain.model.Profile


data class CachedProfile(
    val profile: Profile,
    val cachedAt: Long = System.currentTimeMillis()
)

