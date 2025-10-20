package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.repository.FirestoreProjectsRepository
import com.example.talent_bridge_kt.data.repository.ProjectRepository
import com.example.talent_bridge_kt.domain.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

class ProjectsViewModel(
    app: Application,
    private val firestoreRepo: FirestoreProjectsRepository = FirestoreProjectsRepository(),
    private val localRepo: ProjectRepository = ProjectRepository(app)
) : AndroidViewModel(app) {

    // --- Firestore data ---
    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    // --- Local favorites ---
    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    init {
        refresh()
        loadSavedProjects()
    }

    // 🔥 Fetch from Firestore
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

    // 💾 Load saved (local) projects
    fun loadSavedProjects() {
        viewModelScope.launch {
            _savedProjects.value = localRepo.getSavedProjects()
        }
    }

    fun toggleFavorite(project: Project) {
        viewModelScope.launch {
            val entity = project.toEntity()
            if (localRepo.isProjectSaved(project.id)) {
                localRepo.removeProject(project.id)
            } else {
                localRepo.saveProject(entity)
            }
            loadSavedProjects()
        }
    }

    suspend fun isFavorite(projectId: String): Boolean {
        return localRepo.isProjectSaved(projectId)
    }

    // 🔄 Extension mapper to convert Project → ProjectEntity
    private fun Project.toEntity() = ProjectEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = skills.joinToString(","), // lista como texto
        imgUrl = imgUrl,
        createdAt = createdAt?.toDate()?.toString(),
        createdById = createdById
    )
    // (Opcional) Mapper inverso si más adelante quieres pintar los guardados como Project
    private fun ProjectEntity.toDomain() = Project(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = if (skills.isNullOrBlank()) emptyList() else skills.split(","),
        imgUrl = imgUrl,
        createdAt = null, // si quieres, parsea el String a Date y llévalo a Timestamp
        createdById = createdById
    )
}
