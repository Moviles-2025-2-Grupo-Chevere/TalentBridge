package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver
import com.example.talent_bridge_kt.data.repository.ContactCenterRepository
import com.example.talent_bridge_kt.data.repository.ContactReviewLocalRepository
import com.example.talent_bridge_kt.domain.model.ContactRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactCenterViewModel(
    app: Application,
    private val repo: ContactCenterRepository = ContactCenterRepository(),
    private val localQueue: ContactReviewLocalRepository = ContactReviewLocalRepository(app),
    private val connectivityObserver: AndroidConnectivityObserver = AndroidConnectivityObserver(app)
) : AndroidViewModel(app) {

    private val _received = MutableStateFlow<List<ContactRequest>>(emptyList())
    val received: StateFlow<List<ContactRequest>> = _received

    private val _sent = MutableStateFlow<List<ContactRequest>>(emptyList())
    val sent: StateFlow<List<ContactRequest>> = _sent

    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val info = MutableStateFlow<String?>(null)
    val isOffline = MutableStateFlow(false)

    init {
        load()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collectLatest { connected ->
                val wasOffline = isOffline.value
                isOffline.value = !connected

                if (connected && wasOffline) {
                    syncPendingReviews()
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("User not logged in")

                val (rec, sent) = repo.loadRequestsForUser(uid)
                _received.value = rec.sortedByDescending { it.contactRequestTime }
                _sent.value = sent.sortedByDescending { it.contactRequestTime }
            } catch (e: Exception) {
                error.value = e.message ?: "Error loading contact requests"
            } finally {
                loading.value = false
            }
        }
    }

    fun markReviewed(request: ContactRequest) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            try {
                if (isOffline.value) {
                    localQueue.enqueue(request.id, now)
                    applyLocalReview(request.id, now)
                    info.value = "Changes will be synced once connection is restored."
                } else {
                    repo.markReviewedOnline(request.id, now)
                    applyLocalReview(request.id, now)
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Error updating request"
            }
        }
    }

    private fun applyLocalReview(requestId: String, reviewTimeMs: Long) {
        _received.value = _received.value.map {
            if (it.id == requestId) it.copy(reviewed = true, reviewTime = reviewTimeMs) else it
        }
        _sent.value = _sent.value.map {
            if (it.id == requestId) it.copy(reviewed = true, reviewTime = reviewTimeMs) else it
        }
    }

    private fun syncPendingReviews() {
        viewModelScope.launch {
            try {
                val items = localQueue.list()
                if (items.isEmpty()) return@launch

                for (item in items) {
                    try {
                        repo.markReviewedOnline(item.requestId, item.reviewTimeMs)
                        localQueue.remove(item.id)
                    } catch (_: Exception) {
                        // keep in queue
                    }
                }

                // Reload from server to reflect final state
                load()
                info.value = "All pending contact changes were synced."
            } catch (_: Exception) {
            }
        }
    }
}


