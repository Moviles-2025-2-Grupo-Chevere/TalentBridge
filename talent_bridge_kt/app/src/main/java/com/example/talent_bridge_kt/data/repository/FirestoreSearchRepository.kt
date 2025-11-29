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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import com.example.talent_bridge_kt.data.cache.SearchCache


class FirestoreSearchRepository(
    private val db: FirebaseFirestore
) : SearchRepository {
    

    private val searchCache = SearchCache()

    override suspend fun searchUsers(queryCsv: String, mode: String, limit: Int): List<User> {
        // Optimización: procesar términos con ArrayList en lugar de operaciones encadenadas
        val parts = queryCsv.split(",")
        val termsList = ArrayList<String>(parts.size)
        val termsSet = HashSet<String>()
        
        for (part in parts) {
            val normalized = part.trim().normalizeAsciiLower()
            if (normalized.isNotEmpty() && termsSet.add(normalized) && termsList.size < 10) {
                termsList.add(normalized)
            }
        }

        if (termsList.isEmpty()) return emptyList()
        val col = db.collection("users")

        return if (mode.lowercase() == "any") {
            val snap = col
                .whereEqualTo("isPublic", true)
                .whereArrayContainsAny("skillsOrTopics", termsList)
                .limit(400)
                .get(Source.SERVER)
                .await()

            // Optimización: usar loops en lugar de operaciones encadenadas
            val users = ArrayList<User>(snap.documents.size)
            for (doc in snap.documents) {
                val user = UserDto.from(doc).toDomain()
                if (user.isPublic) {
                    users.add(user)
                }
            }

            // Optimización: calcular matches y rankear en un solo paso
            val ranked = ArrayList<Ranked>(users.size)
            for (u in users) {
                var matches = 0
                for (skill in u.skills) {
                    if (skill in termsSet) matches++
                }
                if (matches > 0) {
                    ranked.add(Ranked(u, matches))
                }
            }

            ranked.sortWith(
                compareByDescending<Ranked> { it.matches }
                    .thenByDescending { it.user.skills.size }
            )

            val result = ArrayList<User>(limit.coerceAtMost(ranked.size))
            for (i in 0 until limit.coerceAtMost(ranked.size)) {
                result.add(ranked[i].user)
            }
            result

        } else {
            val seed = termsList[0]
            val snap = col
                .whereEqualTo("isPublic", true)
                .whereArrayContains("skillsOrTopics", seed)
                .limit(500)
                .get(Source.SERVER)
                .await()

            // Optimización: usar loops en lugar de operaciones encadenadas
            val users = ArrayList<User>(snap.documents.size)
            for (doc in snap.documents) {
                val user = UserDto.from(doc).toDomain()
                if (user.isPublic) {
                    // Verificar que tiene todos los términos
                    var hasAll = true
                    for (term in termsList) {
                        if (term !in user.skills) {
                            hasAll = false
                            break
                        }
                    }
                    if (hasAll) {
                        users.add(user)
                    }
                }
            }

            users.sortWith(compareByDescending<User> { it.skills.size })

            val result = ArrayList<User>(limit.coerceAtMost(users.size))
            for (i in 0 until limit.coerceAtMost(users.size)) {
                result.add(users[i])
            }
            result
        }
    }
    private data class Ranked(val user: User, val matches: Int)
    

    suspend fun searchSingleTerm(
        term: String,
        limit: Int = 200
    ): List<User> = withContext(Dispatchers.IO) {
        val normalizedTerm = term.trim().normalizeAsciiLower()
        if (normalizedTerm.isEmpty()) return@withContext emptyList()
        
        val col = db.collection("users")

        val snap = col
            .whereEqualTo("isPublic", true)
            .limit(400L)
            .get(Source.SERVER)
            .await()
        
        // Optimización: usar loops en lugar de map/filter encadenados
        val matchingUsers = ArrayList<User>(snap.documents.size)
        for (doc in snap.documents) {
            val user = UserDto.from(doc).toDomain()
            if (user.isPublic) {
                // Buscar el término en las habilidades
                for (skill in user.skills) {
                    if (skill == normalizedTerm) {
                        matchingUsers.add(user)
                        break
                    }
                }
            }
        }

        val resultSize = limit.coerceAtMost(matchingUsers.size)
        val result = ArrayList<User>(resultSize)
        for (i in 0 until resultSize) {
            result.add(matchingUsers[i])
        }
        result
    }
    

    data class TermSearchProgress(
        val term: String,
        val status: Status,
        val resultsCount: Int = 0
    ) {
        enum class Status {
            PENDING,
            SEARCHING,
            COMPLETED,
            FAILED
        }
    }
    

    suspend fun searchParallel(
        terms: List<String>,
        limit: Int = 20,
        onProgress: (List<TermSearchProgress>) -> Unit = {}
    ): List<User> = withContext(Dispatchers.IO) {
        supervisorScope {
            val normalizedTerms = terms
                .map { it.trim().normalizeAsciiLower() }
                .filter { it.isNotEmpty() }
                .distinct()
                .take(10)
            
            if (normalizedTerms.isEmpty()) return@supervisorScope emptyList()
            

            val cached = searchCache.get(normalizedTerms)
            if (cached != null && cached.isNotEmpty()) {
                val completedProgress = normalizedTerms.map { term ->
                    TermSearchProgress(term, TermSearchProgress.Status.COMPLETED, cached.size)
                }
                onProgress(completedProgress)
                return@supervisorScope cached
            }
            

            return@supervisorScope searchCache.getOrFetch(normalizedTerms) {

                performSearchParallel(normalizedTerms, limit, onProgress)
            }
        }
    }
    

    private suspend fun performSearchParallel(
        normalizedTerms: List<String>,
        limit: Int,
        onProgress: (List<TermSearchProgress>) -> Unit
    ): List<User> = supervisorScope {
        
        val termsSet = normalizedTerms.toSet()
        val progressMap = mutableMapOf<String, TermSearchProgress>()
        normalizedTerms.forEach { term ->
            progressMap[term] = TermSearchProgress(term, TermSearchProgress.Status.PENDING)
        }
        onProgress(progressMap.values.toList())
        

        val searchJobs = normalizedTerms.map { term ->
            async {
                try {
                    progressMap[term] = TermSearchProgress(term, TermSearchProgress.Status.SEARCHING)
                    onProgress(progressMap.values.toList())
                    
                    val results = searchSingleTerm(term, limit = 400)
                    
                    progressMap[term] = TermSearchProgress(
                        term,
                        TermSearchProgress.Status.COMPLETED,
                        results.size
                    )
                    onProgress(progressMap.values.toList())
                    
                    term to results
                } catch (e: Exception) {
                    progressMap[term] = TermSearchProgress(term, TermSearchProgress.Status.FAILED)
                    onProgress(progressMap.values.toList())
                    term to emptyList<User>()
                }
            }
        }

        val allResults = searchJobs.awaitAll()

        val userMap = mutableMapOf<String, User>()
        val userMatchesMap = mutableMapOf<String, MutableSet<String>>()
        
        // Optimización: evitar toSet() dentro del loop - usar contains directamente
        for ((term, users) in allResults) {
            for (user in users) {
                // Verificar si el término está en las habilidades sin crear Set
                var hasTerm = false
                for (skill in user.skills) {
                    if (skill == term) {
                        hasTerm = true
                        break
                    }
                }
                if (hasTerm) {
                    userMap[user.id] = user
                    val matchesSet = userMatchesMap.getOrPut(user.id) { mutableSetOf() }
                    matchesSet.add(term)
                }
            }
        }

        // Optimización: usar loops en lugar de map/filter encadenados
        val ranked = ArrayList<Ranked>(userMap.size)
        for (user in userMap.values) {
            val matches = userMatchesMap[user.id]?.size ?: 0
            if (matches > 0) {
                ranked.add(Ranked(user, matches))
            }
        }

        ranked.sortWith(
            compareByDescending<Ranked> { it.matches }
                .thenByDescending { it.user.skills.size }
        )
        
        val resultSize = limit.coerceAtMost(ranked.size)
        val result = ArrayList<User>(resultSize)
        for (i in 0 until resultSize) {
            result.add(ranked[i].user)
        }
        result
    }
    
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
