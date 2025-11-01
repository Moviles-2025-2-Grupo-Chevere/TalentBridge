package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver
import com.example.talent_bridge_kt.data.repository.UsersFeedRepository
import com.example.talent_bridge_kt.domain.model.StudentListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudentsViewModel(
    app: Application,
    private val repo: UsersFeedRepository = UsersFeedRepository(
        app,
        connectivityObserver = AndroidConnectivityObserver(app)
    ),
    private var lastLoadWasOffline: Boolean = false,
    private var lastLoadTimestamp: Long = 0L

) : AndroidViewModel(app) {

    val students: MutableStateFlow<List<StudentListItem>> = MutableStateFlow(emptyList())
    val loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableStateFlow<String?> = MutableStateFlow(null)
    val isOffline: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        observeCache()
        viewModelScope.launch { refresh() }
        viewModelScope.launch {
            AndroidConnectivityObserver(getApplication())
                .observe()
                .collectLatest { connected ->
                    val wasOffline = isOffline.value
                    isOffline.value = !connected
                    if (connected) {
                        if (lastLoadWasOffline && !loading.value) {
                            refresh()
                        }
                    } else {
                        if (!wasOffline) {
                            loadCachedOnly()
                        }
                    }
                }
        }
    }

    private fun observeCache() = viewModelScope.launch {
        repo.getStudents().collectLatest { cached ->
            if (isOffline.value) {
                students.value = cached
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        loading.value = true
        error.value = null
        val result = repo.refresh()
        result.fold(
            onSuccess = { all ->
                students.value = all
                isOffline.value = false

                lastLoadWasOffline = false
                lastLoadTimestamp = System.currentTimeMillis()
            },
            onFailure = { e ->
                error.value = e.message ?: "Error loading students"
                isOffline.value = true

                lastLoadWasOffline = true
                lastLoadTimestamp = System.currentTimeMillis()
            }
        )
        loading.value = false
    }

    private fun loadCachedOnly() = viewModelScope.launch {
        try {
            val cached = repo.getStudents().first()
            students.value = cached
            error.value = null

            lastLoadWasOffline = true
            lastLoadTimestamp = System.currentTimeMillis()
        } catch (e: Exception) {
            error.value = "No cached students available"
        }
    }
}
