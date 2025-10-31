package com.example.talent_bridge_kt.presentation.ui.screens



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.domain.model.User
import com.example.talent_bridge_kt.domain.repository.SearchRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.example.talent_bridge_kt.data.repository.FirestoreSearchRepository

data class SearchUiState(
    val querySkills: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val results: List<User> = emptyList(),
    val error: String? = null,
    val termProgress: List<FirestoreSearchRepository.TermSearchProgress> = emptyList()
)

class SearchViewModel(
    private val searchRepo: SearchRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set
    

    private val searchQueryFlow = MutableStateFlow<List<String>>(emptyList())
    
    init {
        searchQueryFlow
            .debounce(300)
            .filter { it.isNotEmpty() }
            .flatMapLatest { terms ->

                flow {
                    try {
                        emit(SearchUiState(
                            querySkills = terms,
                            isLoading = true,
                            results = emptyList(),
                            error = null,
                            termProgress = emptyList()
                        ))

                        val results = if (searchRepo is FirestoreSearchRepository) {
                            searchRepo.searchParallel(
                                terms = terms,
                                limit = 20,
                                onProgress = { progress ->

                                    uiState = uiState.copy(termProgress = progress)
                                }
                            )
                        } else {
                            val q = terms.joinToString(",")
                            searchRepo.searchUsers(q, "any", 20)
                        }
                        
                        emit(SearchUiState(
                            querySkills = terms,
                            isLoading = false,
                            results = results,
                            error = null,
                            termProgress = emptyList()
                        ))
                    } catch (e: Exception) {
                        emit(SearchUiState(
                            querySkills = terms,
                            isLoading = false,
                            results = emptyList(),
                            error = e.message ?: "Error",
                            termProgress = emptyList()
                        ))
                    }
                }.flowOn(Dispatchers.IO)
            }
            .onEach { newState ->
                uiState = newState
            }
            .catch { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Error",
                    termProgress = emptyList()
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSkillsInput(csv: String) {
        val chips = csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        uiState = uiState.copy(querySkills = chips)
        // Trigger search flow (will cancel previous if user is still typing)
        searchQueryFlow.value = chips
    }

    fun search(mode: String = "any") {
        val terms = uiState.querySkills
        if (terms.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uiState = uiState.copy(isLoading = true, results = emptyList(), error = null, termProgress = emptyList())
                
                val list = if (searchRepo is FirestoreSearchRepository) {
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
                
                uiState = uiState.copy(isLoading = false, results = list, termProgress = emptyList())
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error", termProgress = emptyList())
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
