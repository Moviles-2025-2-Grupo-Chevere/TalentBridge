package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.ConnectivityViewModel
import com.example.talent_bridge_kt.data.cache.ProfileMemoryCache
import com.example.talent_bridge_kt.data.cache.ProfileSummary
import com.example.talent_bridge_kt.domain.model.User
import com.example.talent_bridge_kt.domain.repository.SearchRepository
import kotlinx.coroutines.launch

data class SearchUiState(
    val querySkills: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val results: List<User> = emptyList(),
    val error: String? = null
)

class SearchViewModel(
    private val searchRepo: SearchRepository,
    private val connectivity: ConnectivityViewModel
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    fun onSkillsInput(csv: String) {
        val chips = csv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        uiState = uiState.copy(querySkills = chips)
    }

    // ------------- BOTÓN APPLY -------------
    fun search(mode: String = "any") {
        val q = uiState.querySkills.joinToString(",")
        if (q.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, results = emptyList())

            val online = connectivity.isConnected.value
            if (online) {
                try {
                    val remote = searchRepo.searchUsers(q, mode, 20)
                    uiState = uiState.copy(isLoading = false, results = remote)
                } catch (_: Exception) {
                    // si trataron de ir a firestore pero no se pudo, mostramos cache
                    val cached = offlineFilter(q)
                    uiState = uiState.copy(isLoading = false, results = cached, error = null)
                }
            } else {
                // sin internet: directo a RAM
                val cached = offlineFilter(q)
                uiState = uiState.copy(isLoading = false, results = cached, error = null)
            }
        }
    }

    // ------------- BOTÓN VIEW ALL -------------
    fun loadAll() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, results = emptyList())
            val online = connectivity.isConnected.value

            if (online) {
                try {
                    // si tu repo tiene getAll, úsalo; si no, busca con query vacía
                    val remote = searchRepo.searchUsers("", "any", 200)
                    uiState = uiState.copy(isLoading = false, results = remote)
                } catch (_: Exception) {
                    val cached = offlineFilter("")
                    uiState = uiState.copy(isLoading = false, results = cached, error = null)
                }
            } else {
                val cached = offlineFilter("")
                uiState = uiState.copy(isLoading = false, results = cached, error = null)
            }
        }
    }

    // -------- utilidades --------
    private fun offlineFilter(query: String): List<User> {
        val lower = query.lowercase()

        return ProfileMemoryCache.snapshot()
            .mapNotNull { (key, prof) ->
                val uid = key.removePrefix("uid:")
                prof.toOfflineUser(uid)
            }
            .filter { user ->
                if (lower.isBlank()) true
                else {
                    user.displayName.lowercase().contains(lower) ||
                            user.skills.any { it.lowercase().contains(lower) }
                }
            }
    }
}

private fun ProfileSummary.toOfflineUser(uid: String) = User(
    id = uid,
    displayName = this.displayName ?: "Unknown",
    email = "",
    headline = "",
    linkedin = "",
    mobileNumber = "",
    photoUrl = this.avatarUrl,
    location = "",
    skills = emptyList(),
    projects = emptyList(),
    isPublic = true,
    description = ""
)
