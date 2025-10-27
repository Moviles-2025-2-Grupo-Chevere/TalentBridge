package com.example.talent_bridge_kt.data.firebase

import android.net.Uri
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class FirebaseProfileRepository : ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private fun uidOrError(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("No hay usuario autenticado")
    }


    private fun Profile.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "displayName" to name,
        "email" to email,
        "linkedin" to linkedin,
        "phone" to phone,
        "bio" to bio,
        "tags" to tags,
        "projects" to projects.map { p ->
            mapOf(
                "id" to p.id,
                "title" to p.title,
                "subtitle" to p.subtitle, // NUEVO
                "description" to p.description,
                "skills" to p.skills,
                "imgUrl" to p.imgUrl,     // NUEVO
                "createdAt" to p.createdAt, // NUEVO (Timestamp? de Firebase)
                "createdById" to p.createdById // NUEVO
            )
        },
        "avatarUrl" to avatarUrl,
        "projectsUpdatedAt" to projectsUpdatedAt

    )

    private fun mapToProfile(data: Map<String, Any?>, id: String): Profile {
        val projects = (data["projects"] as? List<*>)?.mapNotNull { any ->
            (any as? Map<*, *>)?.let { m ->
                com.example.talent_bridge_kt.domain.model.Project(
                    id = (m["id"] as? String).orEmpty(),
                    title = (m["title"] as? String).orEmpty(),
                    subtitle = m["subtitle"] as? String, // NUEVO
                    description = (m["description"] as? String).orEmpty(),
                    skills = ((m["skills"] as? List<*>)?.map { it.toString() } ?: emptyList()),
                    imgUrl = m["imgUrl"] as? String, // NUEVO
                    createdAt = m["createdAt"] as? com.google.firebase.Timestamp, // NUEVO
                    createdById = (m["createdById"] as? String).orEmpty() // NUEVO
                )
            }
        } ?: emptyList()

        val projectsUpdatedAtMillis = when (val v = data["projectsUpdatedAt"]) {
            is com.google.firebase.Timestamp -> v.toDate().time
            is Long -> v
            else -> null
        }

        return Profile(
            id = (data["id"] as? String) ?: id,
            name = (data["displayName"] as? String)
                ?: (data["name"] as? String)
                ?: "",
            email = (data["email"] as? String).orEmpty(),
            linkedin = data["linkedin"] as? String,
            phone = data["phone"] as? String,
            bio = data["bio"] as? String,
            tags = ((data["tags"] as? List<*>)?.map { it.toString() } ?: emptyList()),
            projects = projects,
            avatarUrl = data["avatarUrl"] as? String,
            projectsUpdatedAt = projectsUpdatedAtMillis
        )
    }


    // ----- Implementación -----

    override suspend fun getProfile(): Resource<Profile> = try {
        val uid = uidOrError()
        val snap = db.collection("users").document(uid).get().await()

        // Si no existe, crea un perfil base con lo que sepamos del auth user
        val profile = if (!snap.exists()) {
            val email = auth.currentUser?.email.orEmpty()
            val base = Profile(
                id = uid,
                name = auth.currentUser?.displayName.orEmpty(),
                email = email,
                linkedin = null,
                phone = null,
                bio = null,
                tags = emptyList(),
                projects = emptyList(),
                avatarUrl = null,
                projectsUpdatedAt = null
            )

            db.collection("users").document(uid)
                .set(base.toMap(), SetOptions.merge())
                .await()
            base
        } else {
            mapToProfile(snap.data ?: emptyMap(), snap.id)
        }

        Resource.Success(profile)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Error al obtener perfil")
    }

    override suspend fun updateProfile(profile: Profile): Resource<Profile> = try {
        val uid = uidOrError()
        val ref = db.collection("users").document(uid)

        // Lee el estado previo para detectar cambios en proyectos
        val before = ref.get().await().data ?: emptyMap<String, Any?>()
        val prevProjects = (before["projects"] as? List<*>) ?: emptyList<Any?>()

        fun norm(list: List<com.example.talent_bridge_kt.domain.model.Project>) =
            list.map {
                mapOf(
                    "id" to it.id,
                    "title" to it.title,
                    "subtitle" to it.subtitle,
                    "description" to it.description,
                    "skills" to it.skills,
                    "imgUrl" to it.imgUrl
                )
            }

        // list viene como List<*> y la voy filtrando a Map<*, *>
        fun normStored(list: List<*>): List<Map<String, Any?>> =
            list.mapNotNull { it as? Map<*, *> }   // descarta lo que no sea Map
                .map { m ->
                    mapOf(
                        "id" to (m["id"] as? String).orEmpty(),
                        "title" to (m["title"] as? String).orEmpty(),
                        "subtitle" to (m["subtitle"] as? String),
                        "description" to (m["description"] as? String).orEmpty(),
                        "skills" to ((m["skills"] as? List<*>)?.map { s -> s.toString() } ?: emptyList<String>()),
                        "imgUrl" to (m["imgUrl"] as? String)
                    )
                }


        val projectsChanged = norm(profile.projects) != normStored(prevProjects)

        val data = profile.copy(id = uid).toMap().toMutableMap()
        if (projectsChanged) {
            data["projectsUpdatedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
        }

        ref.set(data, SetOptions.merge()).await()

        // Vuelve a leer para resolver serverTimestamp
        val fresh = ref.get().await()
        val updated = mapToProfile(fresh.data ?: emptyMap(), fresh.id)

        Resource.Success(updated)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Error al actualizar perfil")
    }


    override suspend fun uploadAvatar(localImage: Uri): Resource<String> = try {
        val uid = uidOrError()
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(localImage).await()
        val url = ref.downloadUrl.await().toString()
        Resource.Success(url)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Error al subir avatar")
    }
}
