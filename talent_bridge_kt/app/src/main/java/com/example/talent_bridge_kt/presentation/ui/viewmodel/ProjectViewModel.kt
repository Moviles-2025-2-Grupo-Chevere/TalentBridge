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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProjectsViewModel(
    app: Application,
    private val feedRepo: FeedRepository = FeedRepository(
        app,
        connectivityObserver = AndroidConnectivityObserver(app)
    ),
    private val localRepo: ProjectRepository = ProjectRepository(app)
) : AndroidViewModel(app) {

    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)
    val isOffline = MutableStateFlow(false)

    val applicationEvent = MutableStateFlow<String?>(null)
    val appliedProjectIds = MutableStateFlow<Set<String>>(emptySet())

    private val usersCol = Firebase.firestore.collection("users")

    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    init {
        loadProjects()
        loadSavedProjects()
        viewModelScope.launch { refresh() }
        viewModelScope.launch { loadAppliedIds() }
        viewModelScope.launch {
            AndroidConnectivityObserver(getApplication())
                .observe()
                .collectLatest { connected ->
                    val wasOffline = isOffline.value
                    isOffline.value = !connected

                    if (connected && wasOffline && !loading.value) {
                        refresh()
                        loadAppliedIds()
                    } else if (!connected && !wasOffline) {
                        loadCachedProjectsOnly()
                    }
                }
        }
    }

    private suspend fun loadAppliedIds() {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val snap = usersCol.document(uid).get().await()
            val apps = (snap.get("applications") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
            val ids = apps.mapNotNull { it["projectId"] as? String }.toSet()
            appliedProjectIds.value = ids
        } catch (_: Exception) {}
    }

    fun applyToProject(
        project: Project,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onAlreadyApplied: () -> Unit,
        onQueuedOffline: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("Debes iniciar sesión para aplicar")

                val userRef = usersCol.document(uid)

                val existingIds: Set<String> = try {
                    val snap = userRef.get().await()
                    val existing = (snap.get("applications") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                        ?: emptyList()
                    existing.mapNotNull { it["projectId"] as? String }.toSet()
                } catch (_: Exception) { emptySet() }

                if (existingIds.contains(project.id) || appliedProjectIds.value.contains(project.id)) {
                    onAlreadyApplied()
                    return@launch
                }

                val application = mapOf(
                    "projectId" to project.id,
                    "createdById" to project.createdById,
                    "appliedAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    userRef.update("applications", FieldValue.arrayUnion(application)).await()
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    onSuccess()
                } catch (_: Exception) {
                    applicationEvent.value = "Tu aplicación a \"${project.title}\" se enviará cuando tengas internet."
                    onQueuedOffline()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al aplicar")
            }
        }
    }

    private fun loadProjects() = viewModelScope.launch {
        feedRepo.getProjects().collectLatest { cachedProjects ->
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
        }
    }

    suspend fun isFavorite(projectId: String): Boolean =
        localRepo.isProjectSaved(projectId, userId)

    fun toggleApplication(
        project: Project,
        onApplied: () -> Unit,
        onUnapplied: () -> Unit,
        onError: (String) -> Unit,
        onQueuedOffline: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("Debes iniciar sesión para aplicar")

                val isApplied = appliedProjectIds.value.contains(project.id)

                if (!isApplied) {
                    applyToProject(
                        project,
                        onSuccess = {
                            appliedProjectIds.value = appliedProjectIds.value + project.id
                            onApplied()
                        },
                        onError = onError,
                        onAlreadyApplied = {
                            appliedProjectIds.value = appliedProjectIds.value + project.id
                            onApplied()
                        },
                        onQueuedOffline = { onQueuedOffline() }
                    )
                } else {
                    if (isOffline.value) {
                        onError("Sin internet: no es posible retirar tu aplicación ahora")
                        return@launch
                    }
                    val userRef = usersCol.document(uid)
                    val snap = userRef.get().await()
                    val apps = (snap.get("applications") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
                    val newApps = apps.filter { (it["projectId"] as? String) != project.id }
                    val newData = mapOf("applications" to newApps)
                    userRef.update(newData).await()

                    appliedProjectIds.value = appliedProjectIds.value - project.id
                    onUnapplied()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar aplicación")
            }
        }
    }

    private fun Project.toEntity(userId: String) = ProjectEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = skills.joinToString(","),
        imgUrl = imgUrl,
        createdAt = createdAt?.toDate()?.toString(),
        createdById = createdById,
        userId = userId
    )

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
