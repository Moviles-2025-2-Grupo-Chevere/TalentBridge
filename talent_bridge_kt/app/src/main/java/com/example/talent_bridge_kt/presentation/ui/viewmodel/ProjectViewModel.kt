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
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProjectsViewModel(
    app: Application,
    private val firestoreRepo: FirestoreProjectsRepository = FirestoreProjectsRepository(),
    private val localRepo: ProjectRepository = ProjectRepository(app),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val usersDb: com.google.firebase.firestore.FirebaseFirestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
) : AndroidViewModel(app) {


    // Usuario actual (si no hay sesión, usa "guest")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    // Firestore
    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)

    // Room (favoritos del usuario)
    private val _savedProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val savedProjects: StateFlow<List<ProjectEntity>> = _savedProjects

    private var projectsListener: ListenerRegistration? = null

    init {
        refresh()
        listenProjectsRealtime()
        loadSavedProjects()
    }

    private fun listenProjectsRealtime() {
        projectsListener?.remove()
        projectsListener = firestoreRepo.listenAllProjects { list ->
            projects.value = list
            loading.value = false
            error.value = null
        }
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

    override fun onCleared() {
        super.onCleared()
        projectsListener?.remove()
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

    fun createProject(
        title: String,
        description: String,
        skills: List<String>,
        imageUri: String?,                   // viene del popup
        onResult: (Boolean, Throwable?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("No hay usuario autenticado")

            val projectId = UUID.randomUUID().toString()


            val finalImgUrl: String? = if (!imageUri.isNullOrBlank()) {
                val uri = Uri.parse(imageUri)

                val ref = storage.reference
                    .child("project_images")
                    .child(projectId)

                ref.putFile(uri).await()

                ref.downloadUrl.await().toString()
            } else {
                null
            }

            val projectMap = mapOf(
                "id" to projectId,
                "title" to title,
                "description" to description,
                "imgUrl" to finalImgUrl,
                "skills" to skills,
                "createdAt" to Timestamp.now(),
                "createdById" to uid
            )

            val userRef = usersDb.collection("users").document(uid)
            userRef.set(
                mapOf(
                    "projects" to com.google.firebase.firestore.FieldValue.arrayUnion(projectMap)
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            refresh()

            onResult(true, null)
        } catch (t: Throwable) {
            onResult(false, t)
        }
    }
}
