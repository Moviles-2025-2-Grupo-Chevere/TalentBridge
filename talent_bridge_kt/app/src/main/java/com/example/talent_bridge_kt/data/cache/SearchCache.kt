package com.example.talent_bridge_kt.data.cache

import android.util.LruCache
import com.example.talent_bridge_kt.data.common.normalizeAsciiLower
import com.example.talent_bridge_kt.domain.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class SearchCache {

    private val cache: LruCache<String, List<User>> = object : LruCache<String, List<User>>(50) {
        override fun sizeOf(key: String, value: List<User>): Int {

            return value.size * 200 + key.length
        }
    }
    

    private val mutex = Mutex()
    

    private val cacheScope = CoroutineScope(Dispatchers.IO)
    

    private val inFlightRequests = mutableMapOf<String, Deferred<List<User>>>()
    

    private fun createCacheKey(terms: List<String>): String {
        return terms
            .map { it.trim().normalizeAsciiLower() }
            .filter { it.isNotEmpty() }
            .sorted()
            .distinct()
            .joinToString(",")
    }
    

    suspend fun get(terms: List<String>): List<User>? {
        val key = createCacheKey(terms)
        return cache.get(key)
    }
    

    suspend fun put(terms: List<String>, results: List<User>) {
        val key = createCacheKey(terms)
        cache.put(key, results)
    }
    

    suspend fun getOrFetch(
        terms: List<String>,
        fetchBlock: suspend () -> List<User>
    ): List<User> {
        val key = createCacheKey(terms)
        

        cache.get(key)?.let { return it }
        

        val deferred: Deferred<List<User>> = mutex.withLock {

            cache.get(key)?.let { 
                return it
            }
            

            inFlightRequests[key]?.let { existingDeferred ->

                return@withLock existingDeferred
            }
            

            val newDeferred = cacheScope.async {
                try {
                    val results = fetchBlock()

                    val cacheKey = createCacheKey(terms)
                    cache.put(cacheKey, results)
                    results
                } finally {

                    mutex.withLock {
                        inFlightRequests.remove(key)
                    }
                }
            }
            

            inFlightRequests[key] = newDeferred
            newDeferred
        }
        

        return deferred.await()
    }
    

    fun clear() {
        cache.evictAll()
    }
}

