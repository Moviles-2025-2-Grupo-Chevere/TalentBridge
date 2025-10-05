package com.example.talent_bridge_kt.presentation.ui.screens



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val searchRepo: SearchRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    fun onSkillsInput(csv: String) {
        val chips = csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        uiState = uiState.copy(querySkills = chips)
    }

    fun search(mode: String = "any") {
        val q = uiState.querySkills.joinToString(",")
        if (q.isBlank()) return
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, results = emptyList(), error = null)
                val list = searchRepo.searchUsers(q, mode, 20)
                uiState = uiState.copy(isLoading = false, results = list)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error")
            }
        }
    }
    fun loadAll() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, results = emptyList(), error = null)
                val list = (searchRepo as? com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository)
                    ?.getAllProfiles(200) ?: emptyList()
                uiState = uiState.copy(isLoading = false, results = list)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error")
            }
        }
    }

}
