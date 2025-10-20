package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.repository.FirestoreProjectsRepository
import com.example.talent_bridge_kt.data.repository.ProjectRepository
import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectsViewModel(
    app: Application,
    private val firestoreRepo: FirestoreProjectsRepository = FirestoreProjectsRepository(),
    private val localRepo: ProjectRepository = ProjectRepository(app)
) : AndroidViewModel(app) {

    // Usuario actual (si no hay sesión, usa "guest")
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    // Firestore
    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)

    // Room (favoritos del usuario)
    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    init {
        refresh()
        loadSavedProjects()
    }

    /* -------- Firestore -------- */
    fun refresh() = viewModelScope.launch {
        loading.value = true
        error.value = null
        try {
            projects.value = firestoreRepo.fetchAllProjects()
        } catch (e: Exception) {
            error.value = e.message ?: "Error loading projects"
        } finally {
            loading.value = false
        }
    }

    /* ---------- Room ----------- */
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
