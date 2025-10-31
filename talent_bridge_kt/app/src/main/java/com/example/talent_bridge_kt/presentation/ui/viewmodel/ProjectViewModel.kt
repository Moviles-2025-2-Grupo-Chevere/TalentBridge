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
import com.example.talent_bridge_kt.data.firebase.analytics.ApplicationAnalytics
import com.example.talent_bridge_kt.data.firebase.FirebaseStorageRepository
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.data.firebase.FirebaseProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import android.net.Uri
import java.util.UUID
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
    private val applicationsLocal: ApplicationsLocalRepository = ApplicationsLocalRepository(app),
    private val storageRepo: FirebaseStorageRepository = FirebaseStorageRepository(),
    private val profileRepo: ProfileRepository = FirebaseProfileRepository()
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
                    
                    // Trackear en Firebase Analytics
                    trackApplicationAnalytics(uid, project.id, project.title)
                    
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
                    
                    // Trackear en Firebase Analytics (aplicación sincronizada offline)
                    trackApplicationAnalytics(uid, item.projectId, item.projectTitle)
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
    
    /**
     * Obtiene el major del usuario y envía evento a Firebase Analytics
     * Nota: El semester se calcula automáticamente desde la fecha actual (período de 6 meses),
     * no desde datos del usuario.
     */
    private suspend fun trackApplicationAnalytics(uid: String, projectId: String, projectTitle: String?) {
        try {
            val userDoc = usersCol.document(uid).get().await()
            
            // Intentar obtener major de diferentes formas
            val majorRaw = userDoc.getString("major")
            val major = majorRaw?.trim()?.takeIf { it.isNotEmpty() && it != "\"\"" } 
                ?: "unknown"
            
            // Debug: verificar qué valores tiene el documento
            android.util.Log.d("ProjectViewModel", 
                "trackApplicationAnalytics: majorRaw='$majorRaw', major='$major', projectId='$projectId'")
            android.util.Log.d("ProjectViewModel", 
                "Full user doc data keys: ${userDoc.data?.keys?.joinToString()}")
            
            // El semester se calcula automáticamente desde la fecha actual (no del usuario)
            ApplicationAnalytics.logProjectApplication(
                semester = null,  // Se calcula automáticamente
                major = major,
                projectId = projectId,
                projectTitle = projectTitle,
                lastLoginAt = null  // No se usa
            )
        } catch (e: Exception) {
            // Error al obtener datos del usuario o enviar analytics, no es crítico
            android.util.Log.e("ProjectViewModel", "Error in trackApplicationAnalytics", e)
            e.printStackTrace()
        }
    }

    fun createProject(
        title: String,
        description: String,
        skills: List<String>,
        imageUri: String?,
        callback: (Boolean, Exception?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("Debes iniciar sesión para crear un proyecto")

                // 1. Subir imagen si existe
                var imageUrl: String? = null
                if (!imageUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(imageUri)
                        val projectId = UUID.randomUUID().toString()
                        imageUrl = storageRepo.uploadProjectImage(projectId, uri)
                    } catch (e: Exception) {
                        callback(false, Exception("Error al subir la imagen: ${e.message}", e))
                        return@launch
                    }
                }

                // 2. Obtener perfil actual
                val profileResult = profileRepo.getProfile()
                val currentProfile = when (profileResult) {
                    is com.example.talent_bridge_kt.domain.util.Resource.Success -> profileResult.data
                    is com.example.talent_bridge_kt.domain.util.Resource.Error -> {
                        callback(false, Exception("Error al obtener perfil: ${profileResult.message}"))
                        return@launch
                    }
                }

                // 3. Crear nuevo proyecto
                val projectId = UUID.randomUUID().toString()
                val newProject = Project(
                    id = projectId,
                    title = title,
                    subtitle = null,
                    description = description,
                    skills = skills,
                    imgUrl = imageUrl,
                    createdAt = com.google.firebase.Timestamp.now(),
                    createdById = uid
                )

                // 4. Actualizar perfil con el nuevo proyecto
                val updatedProfile = currentProfile.copy(
                    projects = currentProfile.projects + newProject
                )

                val updateResult = profileRepo.updateProfile(updatedProfile)
                when (updateResult) {
                    is com.example.talent_bridge_kt.domain.util.Resource.Success -> {
                        callback(true, null)
                    }
                    is com.example.talent_bridge_kt.domain.util.Resource.Error -> {
                        callback(false, Exception("Error al actualizar perfil: ${updateResult.message}"))
                    }
                }
            } catch (e: Exception) {
                callback(false, e)
            }
        }
    }
}
