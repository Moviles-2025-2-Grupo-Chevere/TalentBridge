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
import com.example.talent_bridge_kt.data.local.entities.PendingProjectEntity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase

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

    private val db = AppDatabase.getInstance(app)
    private val pendingDao = db.pendingProjectDao()

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

    private fun isCurrentlyOnline(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // API 23+
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private suspend fun uploadAndSaveProjectOnline(
        userId: String,
        title: String,
        description: String,
        skills: List<String>,
        imageUri: String?
    ) {
        val projectId = UUID.randomUUID().toString()

        val finalImgUrl = if (!imageUri.isNullOrBlank()) {
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
            "createdById" to userId
        )

        usersDb.collection("users")
            .document(userId)
            .set(
                mapOf(
                    "projects" to com.google.firebase.firestore.FieldValue.arrayUnion(projectMap)
                ),
                SetOptions.merge()
            )
            .await()
    }

    fun syncPendingProjects() = viewModelScope.launch {
        val pending = pendingDao.getAllPending()
        if (pending.isEmpty()) return@launch

        for (item in pending) {
            try {
                uploadAndSaveProjectOnline(
                    userId = item.userId,
                    title = item.title,
                    description = item.description,
                    skills = if (item.skillsCsv.isBlank()) emptyList()
                    else item.skillsCsv.split(",").map { it.trim() },
                    imageUri = item.imageUri
                )
                // si se subió bien, lo quitamos de la cola
                pendingDao.deletePending(item.localId)
            } catch (e: Exception) {
                // si falla uno, seguimos con el siguiente
                e.printStackTrace()
            }
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
        imageUri: String?,
        onResult: (Boolean, Throwable?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(false, IllegalStateException("No hay usuario autenticado"))
            return@launch
        }

        val isOnline = isCurrentlyOnline()

        if (isOnline) {
            try {
                uploadAndSaveProjectOnline(
                    userId = uid,
                    title = title,
                    description = description,
                    skills = skills,
                    imageUri = imageUri
                )
                onResult(true, null)
            } catch (e: Throwable) {
                onResult(false, e)
            }
        } else {
            // guardar en cola local
            val pending = PendingProjectEntity(
                localId = UUID.randomUUID().toString(),
                userId = uid,
                title = title,
                description = description,
                skillsCsv = skills.joinToString(","),
                imageUri = imageUri,
                createdAt = System.currentTimeMillis()
            )
            pendingDao.insertPending(pending)
            onResult(true, null)   // ✅ para la UI es “ok, quedó guardado”
        }
    }
}
