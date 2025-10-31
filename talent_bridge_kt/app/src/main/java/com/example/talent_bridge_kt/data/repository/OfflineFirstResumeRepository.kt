package com.example.talent_bridge_kt.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.core.content.getSystemService
import com.example.talent_bridge_kt.data.local.PendingResumeEdit
import com.example.talent_bridge_kt.data.local.ResumeEditStore
import com.example.talent_bridge_kt.domain.model.ResumeLanguage
import com.example.talent_bridge_kt.domain.model.ResumeUploadResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class OfflineFirstResumeRepository(
    context: Context,
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val contentResolver: ContentResolver
) {
    val editStore = ResumeEditStore(context.applicationContext)
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    val baseRepo = ResumeRepositorySimple(storage, db, auth, contentResolver)

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }


    suspend fun updateFileName(resumeId: String, newFileName: String) = withContext(Dispatchers.IO) {
        supervisorScope {

            editStore.applyLocalEdit(resumeId, fileName = newFileName, language = null)
            

            if (isNetworkAvailable()) {
                try {
                    val uid = auth.currentUser?.uid ?: return@supervisorScope
                    db.collection("users").document(uid)
                        .collection("resumes").document(resumeId)
                        .update("fileName", newFileName)
                        .await()
                } catch (e: Exception) {

                    editStore.addPendingEdit(PendingResumeEdit(resumeId, fileName = newFileName, language = null))
                }
            } else {

                editStore.addPendingEdit(PendingResumeEdit(resumeId, fileName = newFileName, language = null))
            }
        }
    }


    suspend fun updateLanguage(resumeId: String, newLanguage: ResumeLanguage) = withContext(Dispatchers.IO) {
        supervisorScope {

            editStore.applyLocalEdit(resumeId, fileName = null, language = newLanguage.name)
            

            if (isNetworkAvailable()) {
                try {
                    val uid = auth.currentUser?.uid ?: return@supervisorScope
                    db.collection("users").document(uid)
                        .collection("resumes").document(resumeId)
                        .update("language", newLanguage.name)
                        .await()
                } catch (e: Exception) {

                    editStore.addPendingEdit(PendingResumeEdit(resumeId, fileName = null, language = newLanguage.name))
                }
            } else {

                editStore.addPendingEdit(PendingResumeEdit(resumeId, fileName = null, language = newLanguage.name))
            }
        }
    }


    suspend fun uploadAll(
        items: List<Pair<Uri, ResumeLanguage>>,
        onProgress: (ResumeRepositorySimple.Progress) -> Unit = {}
    ): List<ResumeUploadResult> = baseRepo.uploadAll(items, onProgress)


    suspend fun deleteResume(resumeId: String, storagePath: String?) = withContext(Dispatchers.IO) {
        supervisorScope {

            editStore.removeLocalResume(resumeId)
            

            if (isNetworkAvailable()) {
                try {
                    val uid = auth.currentUser?.uid ?: return@supervisorScope
                    

                    db.collection("users").document(uid)
                        .collection("resumes").document(resumeId)
                        .delete()
                        .await()
                    

                    storagePath?.let { path ->
                        try {
                            storage.reference.child(path).delete().await()
                        } catch (e: Exception) {

                        }
                    }
                } catch (e: Exception) {

                    editStore.addPendingDelete(resumeId, storagePath)
                }
            } else {

                editStore.addPendingDelete(resumeId, storagePath)
            }
        }
    }


    suspend fun syncPendingEditsAwait() = withContext(Dispatchers.IO) {
        supervisorScope {
            if (!isNetworkAvailable()) {
                return@supervisorScope
            }

            val uid = auth.currentUser?.uid ?: return@supervisorScope
            val pendingEdits = editStore.getPendingEdits()
            if (pendingEdits.isEmpty()) return@supervisorScope

            val successfulEdits = mutableListOf<PendingResumeEdit>()

            for (edit in pendingEdits) {
                try {
                    val docRef = db.collection("users").document(uid)
                        .collection("resumes").document(edit.resumeId)
                    
                    val updates = mutableMapOf<String, Any>()
                    edit.fileName?.let { updates["fileName"] = it }
                    edit.language?.let { updates["language"] = it }
                    
                    if (updates.isNotEmpty()) {
                        docRef.update(updates).await()
                        successfulEdits.add(edit)
                    }
                } catch (e: Exception) {

                }
            }


            if (successfulEdits.size == pendingEdits.size) {

                editStore.clearPendingEdits()
            } else if (successfulEdits.isNotEmpty()) {

                val successfulIds = successfulEdits.map { it.resumeId }.toSet()
                val remainingEdits = pendingEdits.filter { it.resumeId !in successfulIds }
                editStore.clearPendingEdits()

                remainingEdits.forEach { edit ->
                    editStore.addPendingEdit(edit)
                }
            }
        }
    }


    suspend fun syncPendingDeletesAwait() = withContext(Dispatchers.IO) {
        supervisorScope {
            if (!isNetworkAvailable()) {
                return@supervisorScope
            }

            val uid = auth.currentUser?.uid ?: return@supervisorScope
            val pendingDeletes = editStore.getPendingDeletes()
            if (pendingDeletes.isEmpty()) return@supervisorScope

            val successfulDeletes = mutableSetOf<String>()

            for ((resumeId, storagePath) in pendingDeletes) {
                try {

                    db.collection("users").document(uid)
                        .collection("resumes").document(resumeId)
                        .delete()
                        .await()
                    
                    // Delete from Storage if path is available
                    storagePath?.let { path ->
                        try {
                            storage.reference.child(path).delete().await()
                        } catch (e: Exception) {

                        }
                    }
                    
                    successfulDeletes.add(resumeId)
                } catch (e: Exception) {

                }
            }


            if (successfulDeletes.isNotEmpty()) {
                editStore.removePendingDeletes(successfulDeletes)
            }
        }
    }
}

