package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.repository.FeedRepository
import com.example.talent_bridge_kt.data.repository.ProjectRepository
import com.example.talent_bridge_kt.data.repository.ApplicationsLocalRepository
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
    private val localRepo: ProjectRepository = ProjectRepository(app),
    private val applicationsLocal: ApplicationsLocalRepository = ApplicationsLocalRepository(app)
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
                        syncPendingApplications()
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

                // Si ya está marcado como offline en el estado, guardar directamente
                if (isOffline.value) {
                    applicationsLocal.enqueue(project.id, project.createdById, project.title)
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    applicationEvent.value = "Cuando tengas internet se enviará la aplicación al proyecto \"${project.title}\""
                    onQueuedOffline()
                    return@launch
                }

                val userRef = usersCol.document(uid)

                // Intentar obtener aplicaciones existentes
                val existingIds: Set<String> = try {
                    val snap = userRef.get().await()
                    val existing = (snap.get("applications") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                        ?: emptyList()
                    existing.mapNotNull { it["projectId"] as? String }.toSet()
                } catch (e: Exception) {
                    // Si falla el get, probablemente estamos offline
                    // Guardar localmente y notificar
                    applicationsLocal.enqueue(project.id, project.createdById, project.title)
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    applicationEvent.value = "Cuando tengas internet se enviará la aplicación al proyecto \"${project.title}\""
                    onQueuedOffline()
                    return@launch
                }

                // Verificar si ya aplicó
                if (existingIds.contains(project.id) || appliedProjectIds.value.contains(project.id)) {
                    onAlreadyApplied()
                    return@launch
                }

                // Intentar aplicar
                val application = mapOf(
                    "projectId" to project.id,
                    "createdById" to project.createdById,
                    "appliedAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    userRef.update("applications", FieldValue.arrayUnion(application)).await()
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    onSuccess()
                } catch (e: Exception) {
                    // Falló el update: guardar localmente y notificar
                    applicationsLocal.enqueue(project.id, project.createdById, project.title)
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    applicationEvent.value = "Cuando tengas internet se enviará la aplicación al proyecto \"${project.title}\""
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

    private fun syncPendingApplications() = viewModelScope.launch {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val list = applicationsLocal.list()
            if (list.isEmpty()) return@launch

            val userRef = usersCol.document(uid)
            for (item in list) {
                val application = mapOf(
                    "projectId" to item.projectId,
                    "createdById" to item.createdById,
                    "appliedAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    userRef.update("applications", FieldValue.arrayUnion(application)).await()
                    applicationsLocal.remove(item.id)
                    appliedProjectIds.value = appliedProjectIds.value + item.projectId
                    applicationEvent.value = "Ya se envió la aplicación al proyecto \"${item.projectTitle}\" debido a que ya se conectó a internet"
                } catch (_: Exception) {
                    // Dejar en cola para próximo intento
                }
            }
        } catch (_: Exception) { }
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
                        onQueuedOffline = {
                            // Actualizar estado local incluso si se encoló offline
                            appliedProjectIds.value = appliedProjectIds.value + project.id
                            onQueuedOffline()
                        }
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
