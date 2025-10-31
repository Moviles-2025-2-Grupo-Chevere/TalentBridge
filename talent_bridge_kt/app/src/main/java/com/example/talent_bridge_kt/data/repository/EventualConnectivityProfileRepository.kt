package com.example.talent_bridge_kt.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.SparseArray
import androidx.core.content.getSystemService
import com.example.talent_bridge_kt.data.firebase.FirebaseProfileRepository
import com.example.talent_bridge_kt.data.local.AvatarStorageManager
import com.example.talent_bridge_kt.data.local.CachedProfile
import com.example.talent_bridge_kt.data.local.ProfilePreferencesStore
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 * Eventual connectivity profile repository that:
 * - Uses DataStore Preferences for key/value local storage
 * - Uses SparseArray for in-memory caching
 * - Implements form-level offline editing
 * - Auto-syncs when connectivity returns
 */
class EventualConnectivityProfileRepository(
    context: Context
) : ProfileRepository {

    private val prefsStore = ProfilePreferencesStore(context.applicationContext)
    private val avatarStorage = AvatarStorageManager(context.applicationContext)
    private val remoteRepo = FirebaseProfileRepository()
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!

    // SparseArray cache for in-memory profile caching
    private val profileCache: SparseArray<CachedProfile> = SparseArray()

    private fun uidOrError(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No hay usuario autenticado")
    }

    private fun getCacheKey(userId: String): Int {
        return userId.hashCode()
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override suspend fun getProfile(): Resource<Profile> = withContext(Dispatchers.IO) {
        supervisorScope {
            val userId = uidOrError()
            val cacheKey = getCacheKey(userId)

            // Check SparseArray cache first
            profileCache.get(cacheKey)?.let { cached ->
                return@supervisorScope Resource.Success(cached.profile)
            }

            try {
                // Try to get from DataStore (local key/value storage)
                val localProfile = prefsStore.getProfile()
                
                if (localProfile != null) {
                    // Check if there's a local avatar that should be used
                    val localAvatarUri = avatarStorage.getLocalAvatarUri()
                    val profileWithLocalAvatar = if (localAvatarUri != null && localProfile.avatarUrl.isNullOrBlank()) {
                        // Use local avatar if no remote URL exists yet
                        localProfile.copy(avatarUrl = localAvatarUri.toString())
                    } else {
                        localProfile
                    }
                    
                    // Cache it in SparseArray
                    profileCache.put(cacheKey, CachedProfile(profileWithLocalAvatar))
                    
                    // If online and profile is dirty, try to sync in background
                    if (isNetworkAvailable() && prefsStore.isDirty()) {
                        // Attempt sync in background (don't block)
                        syncProfileInBackground(profileWithLocalAvatar)
                    }
                    
                    return@supervisorScope Resource.Success(profileWithLocalAvatar)
                }

                // If not in local storage, try remote if online
                if (isNetworkAvailable()) {
                    val remoteResult = remoteRepo.getProfile()
                    if (remoteResult is Resource.Success) {
                        // Save to DataStore and cache
                        prefsStore.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                        return@supervisorScope Resource.Success(remoteResult.data)
                    }
                }

                // If we get here, no profile exists anywhere
                // Create a default profile
                val defaultProfile = Profile(
                    id = userId,
                    name = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(),
                    email = FirebaseAuth.getInstance().currentUser?.email.orEmpty(),
                    linkedin = null,
                    phone = null,
                    bio = null,
                    tags = emptyList(),
                    projects = emptyList(),
                    avatarUrl = null,
                    projectsUpdatedAt = null
                )
                prefsStore.saveProfile(defaultProfile, isDirty = true)
                profileCache.put(cacheKey, CachedProfile(defaultProfile))
                Resource.Success(defaultProfile)

            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al obtener perfil")
            }
        }
    }

    override suspend fun updateProfile(profile: Profile): Resource<Profile> = withContext(Dispatchers.IO) {
        supervisorScope {
            val userId = uidOrError()
            val cacheKey = getCacheKey(userId)

            try {
                // Always save locally first (offline-first, form-level edit)
                prefsStore.saveProfile(profile, isDirty = true)
                profileCache.put(cacheKey, CachedProfile(profile))

                // Try to sync to remote if online
                if (isNetworkAvailable()) {
                    val remoteResult = remoteRepo.updateProfile(profile)
                    when (remoteResult) {
                        is Resource.Success -> {
                            // Update local with synced version
                            prefsStore.saveProfile(remoteResult.data, isDirty = false)
                            profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                            return@supervisorScope Resource.Success(remoteResult.data)
                        }
                        is Resource.Error -> {
                            // Network error, but we saved locally
                            return@supervisorScope Resource.Success(profile)
                        }
                    }
                } else {
                    // Offline - save as pending for eventual sync
                    prefsStore.savePendingProfile(profile)
                    return@supervisorScope Resource.Success(profile)
                }
            } catch (e: Exception) {
                // Even if remote fails, return local success
                Resource.Success(profile)
            }
        }
    }

    override suspend fun uploadAvatar(localImage: Uri): Resource<String> = withContext(Dispatchers.IO) {
        supervisorScope {
            try {
                // Always save locally first (offline-first)
                val localUri = avatarStorage.saveAvatarLocally(localImage)
                
                // Try to upload immediately if online
                if (isNetworkAvailable()) {
                    val uploadResult = remoteRepo.uploadAvatar(localUri)
                    when (uploadResult) {
                        is Resource.Success -> {
                            // Update profile with remote URL and clear pending
                            avatarStorage.clearPendingUpload()
                            avatarStorage.updateAvatarUrl(uploadResult.data)
                            
                            // Update profile in DataStore with new avatar URL
                            val userId = uidOrError()
                            val currentProfile = prefsStore.getProfile()
                            currentProfile?.let { profile ->
                                val updatedProfile = profile.copy(avatarUrl = uploadResult.data)
                                prefsStore.saveProfile(updatedProfile, isDirty = false)
                                val cacheKey = getCacheKey(userId)
                                profileCache.put(cacheKey, CachedProfile(updatedProfile))
                            }
                            
                            return@supervisorScope Resource.Success(uploadResult.data)
                        }
                        is Resource.Error -> {
                            // Upload failed, but file is saved locally
                            // Return local URI as temporary URL (Coil will cache it)
                            return@supervisorScope Resource.Success(localUri.toString())
                        }
                    }
                } else {
                    // Offline - saved locally, queued for upload
                    // Update profile with local URI so AsyncImage can display it immediately
                    val userId = uidOrError()
                    val currentProfile = prefsStore.getProfile()
                    currentProfile?.let { profile ->
                        val updatedProfile = profile.copy(avatarUrl = localUri.toString())
                        prefsStore.saveProfile(updatedProfile, isDirty = true)
                        val cacheKey = getCacheKey(userId)
                        profileCache.put(cacheKey, CachedProfile(updatedProfile))
                    }
                    // Return local URI so it displays immediately via Coil cache
                    return@supervisorScope Resource.Success(localUri.toString())
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al procesar avatar")
            }
        }
    }

    /**
     * Syncs local changes to remote when connectivity is available.
     * Automatically called when connectivity returns.
     */
    suspend fun syncNow(): Resource<Unit> = withContext(Dispatchers.IO) {
        supervisorScope {
            if (!isNetworkAvailable()) {
                return@supervisorScope Resource.Error("No hay conexión a Internet")
            }

            val userId = uidOrError()
            val cacheKey = getCacheKey(userId)

            try {
                // Check if there's a dirty profile to sync
                val localProfile = prefsStore.getProfile()
                if (localProfile != null && prefsStore.isDirty()) {
                    val remoteResult = remoteRepo.updateProfile(localProfile)
                    when (remoteResult) {
                        is Resource.Success -> {
                            prefsStore.saveProfile(remoteResult.data, isDirty = false)
                            profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                        }
                        is Resource.Error -> {
                            return@supervisorScope Resource.Error(remoteResult.message)
                        }
                    }
                }

                // Process pending profile if exists
                val pendingProfile = prefsStore.getPendingProfile()
                pendingProfile?.let {
                    val remoteResult = remoteRepo.updateProfile(it)
                    if (remoteResult is Resource.Success) {
                        prefsStore.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                    }
                }

                // Clear pending after successful sync
                prefsStore.clearPendingProfile()

                // Also try to fetch latest from remote to sync down
                val remoteResult = remoteRepo.getProfile()
                if (remoteResult is Resource.Success) {
                    prefsStore.saveProfile(remoteResult.data, isDirty = false)
                    profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                }

                // Sync pending avatar uploads
                syncPendingAvatars()

                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al sincronizar")
            }
        }
    }

    private suspend fun syncProfileInBackground(profile: Profile) {
        withContext(Dispatchers.IO) {
            try {
                if (isNetworkAvailable() && prefsStore.isDirty()) {
                    val remoteResult = remoteRepo.updateProfile(profile)
                    if (remoteResult is Resource.Success) {
                        val userId = uidOrError()
                        val cacheKey = getCacheKey(userId)
                        prefsStore.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(cacheKey, CachedProfile(remoteResult.data))
                    }
                }
            } catch (e: Exception) {
                // Silent failure in background sync
            }
        }
    }

    /**
     * Syncs pending avatar uploads when connectivity is available.
     * Called automatically during syncNow().
     */
    private suspend fun syncPendingAvatars() = withContext(Dispatchers.IO) {
        supervisorScope {
            if (!isNetworkAvailable()) return@supervisorScope
            
            if (!avatarStorage.hasPendingUpload()) return@supervisorScope
            
            try {
                val pendingPath = avatarStorage.getPendingAvatarPath() ?: return@supervisorScope
                val file = java.io.File(pendingPath)
                if (!file.exists()) {
                    avatarStorage.clearPendingUpload()
                    return@supervisorScope
                }
                
                val fileUri = android.net.Uri.fromFile(file)
                val uploadResult = remoteRepo.uploadAvatar(fileUri)
                
                when (uploadResult) {
                    is Resource.Success -> {
                        // Update profile with remote URL
                        val userId = uidOrError()
                        val currentProfile = prefsStore.getProfile()
                        currentProfile?.let { profile ->
                            val updatedProfile = profile.copy(avatarUrl = uploadResult.data)
                            prefsStore.saveProfile(updatedProfile, isDirty = false)
                            val cacheKey = getCacheKey(userId)
                            profileCache.put(cacheKey, CachedProfile(updatedProfile))
                        }
                        
                        avatarStorage.clearPendingUpload()
                        avatarStorage.updateAvatarUrl(uploadResult.data)
                    }
                    is Resource.Error -> {
                        // Keep pending for next sync attempt
                    }
                }
            } catch (e: Exception) {
                // Keep pending for next sync attempt
            }
        }
    }
}

