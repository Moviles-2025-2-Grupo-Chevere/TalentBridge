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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
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

    // Usuario actual (si no hay sesión, usa "guest")
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    // Feed con cache offline
    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)
    val isOffline = MutableStateFlow(false)

    // Eventos de aplicación (para mostrar mensajes en UI)
    val applicationEvent = MutableStateFlow<String?>(null)

    // Track projects the current user has applied to
    val appliedProjectIds = MutableStateFlow<Set<String>>(emptySet())

    // Room (favoritos del usuario)
    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    private val usersCol = Firebase.firestore.collection("users")

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
                        // Solo refrescar cuando pasamos de offline -> online
                        syncPendingApplications()
                        refresh()
                        loadAppliedIds()
                    } else if (!connected && !wasOffline) {
                        // Perdió internet ahora: cambiar a cache
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

    // Enviar aplicación del usuario logueado al array applications del documento users/{uid}
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

                val users = Firebase.firestore.collection("users")
                val userRef = users.document(uid)

                // Verificar duplicado (si hay datos locales, este get puede fallar sin internet)
                val existingIds: Set<String> = try {
                    val snap = userRef.get().await()
                    val existing = (snap.get("applications") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                        ?: emptyList()
                    existing.mapNotNull { it["projectId"] as? String }.toSet()
                } catch (_: Exception) {
                    emptySet()
                }
                if (existingIds.contains(project.id) || appliedProjectIds.value.contains(project.id)) {
                    onAlreadyApplied()
                    return@launch
                }

                // Intentar enviar online primero
                val application = mapOf(
                    "projectId" to project.id,
                    "createdById" to project.createdById,
                    "appliedAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    userRef.update("applications", FieldValue.arrayUnion(application)).await()
                    // actualizar cache local de aplicados
                    appliedProjectIds.value = appliedProjectIds.value + project.id
                    onSuccess()
                } catch (_: Exception) {
                    // Fallback: encolar localmente y avisar al usuario
                    applicationsLocal.enqueue(project.id, project.createdById, project.title)
                    applicationEvent.value = "Tu aplicación a \"${project.title}\" se enviará cuando tengas internet."
                    onQueuedOffline()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al aplicar")
            }
        }
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
                    applicationEvent.value = "Se aplicó correctamente al proyecto \"${item.projectTitle}\""
                } catch (_: Exception) {
                    // Dejar en cola para próximo intento
                }
            }
        } catch (_: Exception) { }
    }

    /* -------- Feed con Cache Offline -------- */
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
                    // Apply branch (reuse existing apply logic; respects offline queue)
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
                            onQueuedOffline()
                        }
                    )
                } else {
                    // Unapply branch: only when online for now
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

    /* --------- Mappers ---------- */
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
