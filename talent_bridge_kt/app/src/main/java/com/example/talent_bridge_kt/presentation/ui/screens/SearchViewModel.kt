package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.ConnectivityViewModel
import com.example.talent_bridge_kt.data.local.dao.FeedStudentDao
import com.example.talent_bridge_kt.domain.model.User
import com.example.talent_bridge_kt.domain.repository.SearchRepository
import com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SearchUiState(
    val querySkills: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val results: List<User> = emptyList(),
    val error: String? = null,
    val termProgress: List<FirestoreSearchRepository.TermSearchProgress> = emptyList()
)

class SearchViewModel(
    private val searchRepo: SearchRepository,
    private val connectivity: ConnectivityViewModel,
    private val feedStudentDao: FeedStudentDao
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    private var offlineJob: Job? = null
    private var onlineJob: Job? = null

    fun onSkillsInput(csv: String) {
        val chips = csv
            .split(Regex("[, ]+|,+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        uiState = uiState.copy(querySkills = chips)
    }

    fun search(mode: String = "any") {
        val terms = uiState.querySkills
        if (terms.isEmpty()) return


        uiState = uiState.copy(
            isLoading = true,
            error = null,
            termProgress = emptyList(),
            results = emptyList()
        )


        offlineJob?.cancel()
        offlineJob = viewModelScope.launch(Dispatchers.IO) {
            val offlineResults = offlineSearch(
                queryCsv = terms.joinToString(","),
                mode = mode
            )


            uiState = uiState.copy(
                isLoading = false,
                results = offlineResults,
                error = if (offlineResults.isEmpty()) {
                    "No hay resultados en cache. Abre 'Explore Students' cuando tengas internet."
                } else null
            )
        }


        onlineJob?.cancel()
        onlineJob = viewModelScope.launch(Dispatchers.IO) {
            val online = connectivity.isConnected.first()
            if (!online) return@launch

            try {
                val onlineResults: List<User> = if (searchRepo is FirestoreSearchRepository) {

                    searchRepo.searchParallel(
                        terms = terms,
                        limit = 20,
                        onProgress = { progress ->

                            uiState = uiState.copy(termProgress = progress)
                        }
                    )
                } else {

                    val q = terms.joinToString(",")
                    searchRepo.searchUsers(q, mode, 20)
                }

                uiState = uiState.copy(

                    results = onlineResults,
                    termProgress = emptyList(),
                    error = null
                )
            } catch (e: Exception) {

                uiState = uiState.copy(
                    termProgress = emptyList(),
                    error = e.message ?: "Error buscando online. Se muestran resultados en cache."
                )
            }
        }
    }


    fun loadAll() {
        uiState = uiState.copy(
            isLoading = true,
            error = null,
            termProgress = emptyList(),
            results = emptyList()
        )


        offlineJob?.cancel()
        offlineJob = viewModelScope.launch(Dispatchers.IO) {
            val cached = offlineSearch("", "any")
            uiState = uiState.copy(
                isLoading = false,
                results = cached,
                error = if (cached.isEmpty()) {
                    "No hay perfiles en cache. Abre 'Explore Students' cuando tengas internet."
                } else null
            )
        }


        onlineJob?.cancel()
        onlineJob = viewModelScope.launch(Dispatchers.IO) {
            val online = connectivity.isConnected.first()
            if (!online) return@launch

            try {
                val list = if (searchRepo is FirestoreSearchRepository) {
                    searchRepo.getAllProfiles(200)
                } else {
                    searchRepo.searchUsers("", "any", 200)
                }
                uiState = uiState.copy(
                    results = list,
                    error = null,
                    termProgress = emptyList()
                )
            } catch (e: Exception) {

                uiState = uiState.copy(
                    error = e.message ?: "No se pudieron actualizar los perfiles desde la nube."
                )
            }
        }
    }


    private suspend fun offlineSearch(queryCsv: String, mode: String): List<User> {

        val cachedEntities = feedStudentDao.getAllCachedStudents()
        if (cachedEntities.isEmpty()) {
            return emptyList()
        }


        if (queryCsv.isBlank()) {
            return cachedEntities.map { it.toUser() }
        }


        val terms = queryCsv
            .split(Regex("[, ]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        val users = cachedEntities.map { it.toUser() }

        return if (mode.equals("any", ignoreCase = true)) {
            users.asSequence()
                .map { user ->
                    val matches = terms.count { term ->
                        user.displayName.lowercase().contains(term) ||
                                user.skills.any { sk -> sk.lowercase().contains(term) }
                    }
                    Ranked(user, matches)
                }
                .filter { it.matches > 0 }
                .sortedByDescending { it.matches }
                .map { it.user }
                .take(20)
                .toList()
        } else {

            users.asSequence()
                .filter { user ->
                    terms.all { term ->
                        user.displayName.lowercase().contains(term) ||
                                user.skills.any { sk -> sk.lowercase().contains(term) }
                    }
                }
                .take(20)
                .toList()
        }
    }

    private data class Ranked(val user: User, val matches: Int)
}

private fun com.example.talent_bridge_kt.data.local.entities.FeedStudentEntity.toUser(): User {
    val skillsList = if (skillsCsv.isNullOrBlank()) {
        emptyList()
    } else {
        skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    return User(
        id = uid,
        displayName = displayName,
        email = "",
        headline = headline ?: "",
        linkedin = "",
        mobileNumber = "",
        photoUrl = avatarUrl,
        location = "",
        skills = skillsList,
        projects = emptyList(),
        isPublic = true,
        description = bio
    )
}
