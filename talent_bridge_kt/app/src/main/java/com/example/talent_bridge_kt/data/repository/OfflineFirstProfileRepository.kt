package com.example.talent_bridge_kt.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.LruCache
import androidx.core.content.getSystemService
import com.example.talent_bridge_kt.data.firebase.FirebaseProfileRepository
import com.example.talent_bridge_kt.data.local.ProfileDatabaseHelper
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext


class OfflineFirstProfileRepository(
    context: Context
) : ProfileRepository {

    private val dbHelper = ProfileDatabaseHelper(context.applicationContext)
    private val remoteRepo = FirebaseProfileRepository()
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!


    private val profileCache: LruCache<String, Profile> = object : LruCache<String, Profile>(1) {
        override fun sizeOf(key: String, value: Profile): Int {

            return 1
        }
    }

    private fun uidOrError(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("No hay usuario autenticado")
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


            profileCache.get(userId)?.let { cachedProfile ->
                return@supervisorScope Resource.Success(cachedProfile)
            }

            try {

                val localProfile = dbHelper.getProfile(userId)
                
                if (localProfile != null) {

                    profileCache.put(userId, localProfile)
                    

                    if (isNetworkAvailable() && dbHelper.isProfileDirty(userId)) {

                        syncProfileInBackground(localProfile)
                    }
                    
                    return@supervisorScope Resource.Success(localProfile)
                }


                if (isNetworkAvailable()) {
                    val remoteResult = remoteRepo.getProfile()
                    if (remoteResult is Resource.Success) {

                        dbHelper.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(userId, remoteResult.data)
                        return@supervisorScope Resource.Success(remoteResult.data)
                    }
                }


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
                dbHelper.saveProfile(defaultProfile, isDirty = true)
                profileCache.put(userId, defaultProfile)
                Resource.Success(defaultProfile)

            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al obtener perfil")
            }
        }
    }

    override suspend fun updateProfile(profile: Profile): Resource<Profile> = withContext(Dispatchers.IO) {
        supervisorScope {
            val userId = uidOrError()

            try {

                dbHelper.saveProfile(profile, isDirty = true)
                profileCache.put(userId, profile)


                if (isNetworkAvailable()) {
                    val remoteResult = remoteRepo.updateProfile(profile)
                    if (remoteResult is Resource.Success) {

                        dbHelper.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(userId, remoteResult.data)
                        return@supervisorScope Resource.Success(remoteResult.data)
                    } else {

                        return@supervisorScope Resource.Success(profile)
                    }
                } else {

                    dbHelper.addPendingUpdate(profile)
                    return@supervisorScope Resource.Success(profile)
                }
            } catch (e: Exception) {

                Resource.Success(profile)
            }
        }
    }

    override suspend fun uploadAvatar(localImage: Uri): Resource<String> = withContext(Dispatchers.IO) {
        supervisorScope {
            if (isNetworkAvailable()) {
                remoteRepo.uploadAvatar(localImage)
            } else {
                Resource.Error("No hay conexión a Internet para subir el avatar")
            }
        }
    }


    suspend fun syncNow(): Resource<Unit> = withContext(Dispatchers.IO) {
        supervisorScope {
            if (!isNetworkAvailable()) {
                return@supervisorScope Resource.Error("No hay conexión a Internet")
            }

            val userId = uidOrError()

            try {

                val localProfile = dbHelper.getProfile(userId)
                if (localProfile != null && dbHelper.isProfileDirty(userId)) {
                    val remoteResult = remoteRepo.updateProfile(localProfile)
                    when (remoteResult) {
                        is Resource.Success -> {
                            dbHelper.saveProfile(remoteResult.data, isDirty = false)
                            profileCache.put(userId, remoteResult.data)
                        }
                        is Resource.Error -> {
                            return@supervisorScope Resource.Error(remoteResult.message)
                        }
                    }
                }


                val pendingUpdates = dbHelper.getPendingUpdates()
                for (pendingProfile in pendingUpdates) {
                    val remoteResult = remoteRepo.updateProfile(pendingProfile)
                    if (remoteResult is Resource.Success) {
                        dbHelper.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(userId, remoteResult.data)
                    }
                }


                dbHelper.clearPendingUpdates()


                val remoteResult = remoteRepo.getProfile()
                if (remoteResult is Resource.Success) {
                    dbHelper.saveProfile(remoteResult.data, isDirty = false)
                    profileCache.put(userId, remoteResult.data)
                }

                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error al sincronizar")
            }
        }
    }

    private suspend fun syncProfileInBackground(profile: Profile) {
        withContext(Dispatchers.IO) {
            try {
                if (isNetworkAvailable() && dbHelper.isProfileDirty(profile.id)) {
                    val remoteResult = remoteRepo.updateProfile(profile)
                    if (remoteResult is Resource.Success) {
                        dbHelper.saveProfile(remoteResult.data, isDirty = false)
                        profileCache.put(profile.id, remoteResult.data)
                    }
                }
            } catch (e: Exception) {

            }
        }
    }
}
