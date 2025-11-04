package com.example.talent_bridge_kt.data.cache

import java.util.concurrent.ConcurrentHashMap

data class ProfileSummary(
    val displayName: String?,
    val avatarUrl: String?
)

private data class CacheEntry(
    val value: ProfileSummary,
    val expiresAtMs: Long
)

object ProfileMemoryCache {
    private const val DEFAULT_TTL_MS: Long = 5 * 60 * 1000
    private val store = ConcurrentHashMap<String, CacheEntry>()

    fun get(key: String): ProfileSummary? {
        val entry = store[key] ?: return null
        val now = System.currentTimeMillis()
        return if (now < entry.expiresAtMs) entry.value
        else { store.remove(key); null }
    }

    fun put(key: String, value: ProfileSummary, ttlMs: Long = DEFAULT_TTL_MS) {
        val expiresAt = System.currentTimeMillis() + ttlMs
        store[key] = CacheEntry(value, expiresAt)
    }

    fun clear(key: String) { store.remove(key) }
    fun clearAll() { store.clear() }

    fun snapshot(): Map<String, ProfileSummary> =
        store.mapValues { it.value.value }
}



