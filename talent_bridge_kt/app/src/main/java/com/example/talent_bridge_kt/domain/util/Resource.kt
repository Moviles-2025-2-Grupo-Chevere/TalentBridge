package com.example.talent_bridge_kt.domain.util

/**
 * Simple result wrapper for use cases and repositories.
 * You can swap this later for Arrow Either, Kotlin Result, etc.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Resource<Nothing>()
}
