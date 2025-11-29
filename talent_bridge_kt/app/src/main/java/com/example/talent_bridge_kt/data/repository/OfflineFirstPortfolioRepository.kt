package com.example.talent_bridge_kt.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.core.content.getSystemService
import com.example.talent_bridge_kt.data.local.PendingPortfolioEdit
import com.example.talent_bridge_kt.data.local.PortfolioEditStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OfflineFirstPortfolioRepository(
    context: Context,
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val contentResolver: ContentResolver
) {
    val editStore = PortfolioEditStore(context.applicationContext)
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    val baseRepo = PortfolioRepositorySimple(storage, db, auth, contentResolver)

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    suspend fun updateTitle(portfolioId: String, newTitle: String) = withContext(Dispatchers.IO) {
        supervisorScope {
            editStore.applyLocalEdit(portfolioId, title = newTitle)
            
            if (isNetworkAvailable()) {
                try {
                    val uid = auth.currentUser?.uid ?: return@supervisorScope
                    db.collection("users").document(uid)
                        .collection("portfolios").document(portfolioId)
                        .update("title", newTitle)
                        .await()
                } catch (e: Exception) {
                    editStore.addPendingEdit(PendingPortfolioEdit(portfolioId, title = newTitle))
                }
            } else {
                editStore.addPendingEdit(PendingPortfolioEdit(portfolioId, title = newTitle))
            }
        }
    }

    suspend fun uploadAll(
        items: List<Pair<Uri, String>>, // Uri and title
        onProgress: (PortfolioRepositorySimple.Progress) -> Unit = {}
    ): List<PortfolioUploadResult> = baseRepo.uploadAll(items, onProgress)

    suspend fun deletePortfolio(portfolioId: String, storagePath: String?) = withContext(Dispatchers.IO) {
        supervisorScope {
            editStore.removeLocalPortfolio(portfolioId)
            
            if (isNetworkAvailable()) {
                try {
                    val uid = auth.currentUser?.uid ?: return@supervisorScope
                    
                    db.collection("users").document(uid)
                        .collection("portfolios").document(portfolioId)
                        .delete()
                        .await()
                    
                    storagePath?.let { path ->
                        try {
                            storage.reference.child(path).delete().await()
                        } catch (e: Exception) {
                            // Error deleting from storage
                        }
                    }
                } catch (e: Exception) {
                    editStore.addPendingDelete(portfolioId, storagePath)
                }
            } else {
                editStore.addPendingDelete(portfolioId, storagePath)
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

            val successfulEdits = mutableListOf<PendingPortfolioEdit>()

            for (edit in pendingEdits) {
                try {
                    val docRef = db.collection("users").document(uid)
                        .collection("portfolios").document(edit.portfolioId)
                    
                    val updates = mutableMapOf<String, Any>()
                    edit.title?.let { updates["title"] = it }
                    
                    if (updates.isNotEmpty()) {
                        docRef.update(updates).await()
                        successfulEdits.add(edit)
                    }
                } catch (e: Exception) {
                    // Error syncing edit
                }
            }

            if (successfulEdits.size == pendingEdits.size) {
                editStore.clearPendingEdits()
            } else if (successfulEdits.isNotEmpty()) {
                val successfulIds = successfulEdits.map { it.portfolioId }.toSet()
                val remainingEdits = pendingEdits.filter { it.portfolioId !in successfulIds }
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

            for ((portfolioId, storagePath) in pendingDeletes) {
                try {
                    db.collection("users").document(uid)
                        .collection("portfolios").document(portfolioId)
                        .delete()
                        .await()
                    
                    storagePath?.let { path ->
                        try {
                            storage.reference.child(path).delete().await()
                        } catch (e: Exception) {
                            // Error deleting from storage
                        }
                    }
                    
                    successfulDeletes.add(portfolioId)
                } catch (e: Exception) {
                    // Error syncing delete
                }
            }

            if (successfulDeletes.isNotEmpty()) {
                editStore.removePendingDeletes(successfulDeletes)
            }
        }
    }
}

