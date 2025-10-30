package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.repository.FeedRepository
import com.example.talent_bridge_kt.data.repository.ProjectRepository
import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProjectsViewModel(
    app: Application,
    private val feedRepo: FeedRepository = FeedRepository(
        app,
        connectivityObserver = AndroidConnectivityObserver(app)
    ),
    private val localRepo: ProjectRepository = ProjectRepository(app)
) : AndroidViewModel(app) {

    // Usuario actual (si no hay sesión, usa "guest")
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    // Feed con cache offline
    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)
    val isOffline = MutableStateFlow(false)

    // Room (favoritos del usuario)
    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    init {
        loadProjects()
        loadSavedProjects()
        viewModelScope.launch {
            refresh()
        }
        viewModelScope.launch {
            AndroidConnectivityObserver(getApplication())
                .observe()
                .collectLatest { connected ->
                    val wasOffline = isOffline.value
                    isOffline.value = !connected
                    
                    if (connected && !loading.value) {
                        // Recuperó internet: cargar todos los proyectos
                        refresh()
                    } else if (!connected && !wasOffline) {
                        // Perdió internet: cambiar a mostrar solo cache
                        loadCachedProjectsOnly()
                    }
                }
        }
    }

    /* -------- Feed con Cache Offline -------- */
    private fun loadProjects() = viewModelScope.launch {
        // Solo observar cache cuando estemos offline
        // Cuando estemos online, refresh() manejará los proyectos directamente
        feedRepo.getProjects().collectLatest { cachedProjects ->
            // Solo actualizar si estamos offline
            if (isOffline.value) {
                projects.value = cachedProjects
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        loading.value = true
        error.value = null
        
        val result = feedRepo.refreshProjects()
        
        result.fold(
            onSuccess = { freshProjects ->
                projects.value = freshProjects
                isOffline.value = false
            },
            onFailure = { exception ->
                error.value = exception.message ?: "Error loading projects"
                isOffline.value = true
            }
        )
        
        loading.value = false
    }

    private fun loadCachedProjectsOnly() = viewModelScope.launch {
        try {
            val cachedProjects = feedRepo.getProjects().first()
            projects.value = cachedProjects
            error.value = null
        } catch (e: Exception) {
            error.value = "No cached projects available"
        }
    }

    /* ---------- Room (Favoritos) ----------- */
    fun loadSavedProjects() {
        viewModelScope.launch {
            localRepo.getSavedProjects(userId).collectLatest { list ->
                _savedProjects.value = list
            }
        }
    }

    fun toggleFavorite(project: Project) {
        viewModelScope.launch {
            val entity = project.toEntity(userId)
            if (localRepo.isProjectSaved(project.id, userId)) {
                localRepo.removeProject(project.id, userId)
            } else {
                localRepo.saveProject(entity)
            }
            // collectLatest actualizará _savedProjects automáticamente
        }
    }

    suspend fun isFavorite(projectId: String): Boolean =
        localRepo.isProjectSaved(projectId, userId)

    /* --------- Mappers ---------- */
    private fun Project.toEntity(userId: String) = ProjectEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = skills.joinToString(","), // CSV
        imgUrl = imgUrl,
        createdAt = createdAt?.toDate()?.toString(),
        createdById = createdById,
        userId = userId
    )

    // (opcional) para reusar UI con entities
    private fun ProjectEntity.toDomain() = Project(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() },
        imgUrl = imgUrl,
        createdAt = null,
        createdById = createdById
    )
}
