package com.example.talent_bridge_kt.domain.repository

import android.net.Uri
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.util.Resource

/**
 * Abstraction over the data source(s).
 * The data layer will implement this (Firebase, REST, etc.).
 */
interface ProfileRepository {

    /** Fetch the current user's profile. */
    suspend fun getProfile(): Resource<Profile>

    /** Update profile fields and return the updated profile. */
    suspend fun updateProfile(profile: Profile): Resource<Profile>

    /**
     * Upload a local image and return its remote/public URL.
     * (e.g., uploads to Firebase Storage and returns the download URL)
     */
    suspend fun uploadAvatar(localImage: Uri): Resource<String>
}
