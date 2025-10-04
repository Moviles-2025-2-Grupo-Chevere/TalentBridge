// app/src/main/java/com/example/talent_bridge_kt/data/repository/FirestoreSearchRepository.kt
package com.example.talent_bridge_kt.data.repository

import com.example.talent_bridge_kt.data.common.normalizeAsciiLower
import com.example.talent_bridge_kt.data.remote.dto.UserDto
import com.example.talent_bridge_kt.domain.model.User
import com.example.talent_bridge_kt.domain.repository.SearchRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source
import com.example.talent_bridge_kt.data.common.COLLECTION_PROFILES


class FirestoreSearchRepository(
    private val db: FirebaseFirestore
) : SearchRepository {

    override suspend fun searchUsers(queryCsv: String, mode: String, limit: Int): List<User> {
        val terms = queryCsv.split(",")
            .map { it.trim().normalizeAsciiLower() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(10)

        if (terms.isEmpty()) return emptyList()
        val termsSet = terms.toSet()
        val col = db.collection("users")

        return if (mode.lowercase() == "any") {
            val snap = col
                .whereEqualTo("isPublic", true)
                .whereArrayContainsAny("skillsOrTopics", terms)
                .limit(400)
                .get(Source.SERVER)
                .await()

            val users = snap.documents.map { UserDto.from(it).toDomain() }

            users.asSequence()
                .filter { it.isPublic }
                .map { u ->
                    val matches = u.skills.count { it in termsSet }
                    Ranked(u, matches)
                }
                .filter { it.matches > 0 }
                .sortedWith(
                    compareByDescending<Ranked> { it.matches }
                        .thenByDescending { it.user.skills.size }

                )
                .map { it.user }
                .take(limit)
                .toList()

        } else {

            val seed = terms.first()
            val snap = col
                .whereEqualTo("isPublic", true)
                .whereArrayContains("skillsOrTopics", seed)
                .limit(500)
                .get(Source.SERVER)
                .await()

            val users = snap.documents.map { UserDto.from(it).toDomain() }

            users.asSequence()
                .filter { it.isPublic }
                .filter { u -> terms.all { t -> u.skills.contains(t) } }
                .sortedWith(
                    compareByDescending<User> { it.skills.size }

                )
                .take(limit)
                .toList()
        }
    }
    private data class Ranked(val user: User, val matches: Int)
    suspend fun getAllProfiles(limit: Int = 200): List<User> {
        val snap = db.collection(COLLECTION_PROFILES)
            .whereEqualTo("isPublic", true)
            .limit(limit.toLong())
            .get(Source.SERVER)
            .await()

        return snap.documents
            .map { UserDto.from(it).toDomain() }
    }

}
