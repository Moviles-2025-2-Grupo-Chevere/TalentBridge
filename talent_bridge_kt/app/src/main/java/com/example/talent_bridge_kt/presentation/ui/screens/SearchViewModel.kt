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
import kotlinx.coroutines.flow.first
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
    private val searchRepo: SearchRepository,
    private val connectivity: ConnectivityViewModel,
    private val feedStudentDao: FeedStudentDao
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

                        // Verificar conectividad antes de buscar
                        val online = connectivity.isConnected.first()
                        
                        val results = if (online) {
                            // Intentar búsqueda online
                            try {
                                if (searchRepo is FirestoreSearchRepository) {
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
                            } catch (e: Exception) {
                                // Si falla online, usar cache offline
                                offlineSearch(terms.joinToString(","), "any")
                            }
                        } else {
                            // Sin internet: buscar en cache
                            offlineSearch(terms.joinToString(","), "any")
                        }
                        
                        emit(SearchUiState(
                            querySkills = terms,
                            isLoading = false,
                            results = results,
                            error = if (results.isEmpty() && !online) "Sin conexión. Visita 'Explore Students' para cargar perfiles en cache" else null,
                            termProgress = emptyList()
                        ))
                    } catch (e: Exception) {
                        // Si hay error, intentar buscar en cache como último recurso
                        val cached = try {
                            offlineSearch(terms.joinToString(","), "any")
                        } catch (_: Exception) {
                            emptyList()
                        }
                        emit(SearchUiState(
                            querySkills = terms,
                            isLoading = false,
                            results = cached,
                            error = if (cached.isEmpty()) (e.message ?: "Error") else null,
                            termProgress = emptyList()
                        ))
                    }
                }.flowOn(Dispatchers.IO)
            }
            .onEach { newState ->
                uiState = newState
            }
            .catch { e ->
                // Fallback a cache en caso de error
                val cached = try {
                    offlineSearch("", "any")
                } catch (_: Exception) {
                    emptyList()
                }
                uiState = uiState.copy(
                    isLoading = false,
                    error = if (cached.isEmpty()) (e.message ?: "Error") else null,
                    results = cached,
                    termProgress = emptyList()
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSkillsInput(csv: String) {
        // Permitir búsqueda tanto por nombre como por habilidades
        // Separar por comas o espacios múltiples
        val chips = csv.split(Regex("[, ]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        uiState = uiState.copy(querySkills = chips)
        // Trigger search flow (will cancel previous if user is still typing)
        searchQueryFlow.value = chips
    }

    // ------------- BOTÓN APPLY -------------
    fun search(mode: String = "any") {
        val terms = uiState.querySkills
        if (terms.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uiState = uiState.copy(isLoading = true, results = emptyList(), error = null, termProgress = emptyList())
                
                // Verificar conectividad
                val online = connectivity.isConnected.first()
                
                val list = if (online) {
                    try {
                        // Intentar búsqueda online
                        if (searchRepo is FirestoreSearchRepository) {
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
                    } catch (e: Exception) {
                        // Si falla online, usar cache offline
                        offlineSearch(terms.joinToString(","), mode)
                    }
                } else {
                    // Sin internet: buscar en cache
                    offlineSearch(terms.joinToString(","), mode)
                }
                
                uiState = uiState.copy(
                    isLoading = false, 
                    results = list, 
                    termProgress = emptyList(),
                    error = if (list.isEmpty() && !online) "Sin conexión. Visita 'Explore Students' para cargar perfiles en cache" else null
                )
            } catch (e: Exception) {
                // Fallback: intentar buscar en cache
                val cached = try {
                    offlineSearch(terms.joinToString(","), mode)
                } catch (_: Exception) {
                    emptyList()
                }
                uiState = uiState.copy(
                    isLoading = false, 
                    results = cached,
                    error = if (cached.isEmpty()) (e.message ?: "Error") else null, 
                    termProgress = emptyList()
                )
            }
        }
    }
    // ------------- BOTÓN VIEW ALL -------------
    fun loadAll() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, results = emptyList(), termProgress = emptyList())
            
            try {
                val online = connectivity.isConnected.first()
                
                if (online) {
                    try {
                        // Intentar usar getAllProfiles si está disponible (más eficiente)
                        val list = if (searchRepo is FirestoreSearchRepository) {
                            searchRepo.getAllProfiles(200)
                        } else {
                            // Fallback a searchUsers con query vacía
                            searchRepo.searchUsers("", "any", 200)
                        }
                        uiState = uiState.copy(isLoading = false, results = list, termProgress = emptyList())
                    } catch (e: Exception) {
                        // Si falla, usar cache offline
                        val cached = offlineSearch("", "any")
                        uiState = uiState.copy(
                            isLoading = false, 
                            results = cached,
                            error = if (cached.isEmpty()) "Sin conexión y sin resultados en cache" else null,
                            termProgress = emptyList()
                        )
                    }
                } else {
                    // Sin internet: mostrar todos los perfiles del cache
                    val cached = offlineSearch("", "any")
                    uiState = uiState.copy(
                        isLoading = false, 
                        results = cached,
                        error = if (cached.isEmpty()) "Sin conexión. Visita la sección 'Explore Students' para cargar perfiles en cache" else null,
                        termProgress = emptyList()
                    )
                }
            } catch (e: Exception) {
                // Fallback: siempre intentar buscar en cache
                val cached = offlineSearch("", "any")
                uiState = uiState.copy(
                    isLoading = false, 
                    results = cached,
                    error = if (cached.isEmpty()) "No se encontraron resultados" else null,
                    termProgress = emptyList()
                )
            }
        }
    }

    // -------- utilidades --------
    private suspend fun offlineSearch(queryCsv: String, mode: String): List<User> {
        // Obtener todos los estudiantes cacheados de Room
        val cachedEntities = feedStudentDao.getAllCachedStudents()
        
        if (cachedEntities.isEmpty()) {
            return emptyList()
        }

        // Si la query está vacía, devolver todos
        if (queryCsv.isBlank()) {
            return cachedEntities.map { it.toUser() }
        }

        // Procesar términos de búsqueda (soporta comas y espacios como separadores)
        val terms = queryCsv.split(Regex("[, ]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (terms.isEmpty()) {
            return cachedEntities.map { it.toUser() }
        }

        val termsSet = terms.toSet()

        // Convertir entidades a Users y filtrar
        val users = cachedEntities.map { it.toUser() }

        return if (mode.lowercase() == "any") {
            // Modo "any": busca usuarios que tengan cualquiera de los términos (en nombre o skills)
            users.asSequence()
                .map { user ->
                    val nameMatches = terms.count { term ->
                        user.displayName.lowercase().contains(term)
                    }
                    val skillMatches = user.skills.count { skill ->
                        termsSet.any { term -> skill.lowercase().contains(term) }
                    }
                    val totalMatches = nameMatches + skillMatches
                    Ranked(user, totalMatches)
                }
                .filter { it.matches > 0 }
                .sortedWith(
                    compareByDescending<Ranked> { it.matches }
                        .thenByDescending { it.user.skills.size }
                )
                .map { it.user }
                .take(20)
                .toList()
        } else {
            // Modo "all": busca usuarios que tengan todos los términos
            users.asSequence()
                .filter { user ->
                    // Verificar que todos los términos estén en el nombre o en las skills
                    terms.all { term ->
                        user.displayName.lowercase().contains(term) ||
                        user.skills.any { skill -> skill.lowercase().contains(term) }
                    }
                }
                .sortedWith(
                    compareByDescending<User> { it.skills.size }
                )
                .take(20)
                .toList()
        }
    }

    private data class Ranked(val user: User, val matches: Int)
}

// Extension function para convertir FeedStudentEntity a User
private fun com.example.talent_bridge_kt.data.local.entities.FeedStudentEntity.toUser(): User {
    val skillsList = if (skillsCsv.isBlank()) {
        emptyList()
    } else {
        skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    return User(
        id = uid,
        displayName = displayName,
        email = "",
        headline = headline,
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
