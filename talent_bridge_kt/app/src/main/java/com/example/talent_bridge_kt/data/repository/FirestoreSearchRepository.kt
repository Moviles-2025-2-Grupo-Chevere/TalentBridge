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
        
        val allUsers = snap.documents
            .map { UserDto.from(it).toDomain() }
            .filter { it.isPublic }
        

        val matchingUsers = allUsers.filter { user ->
            user.skills.any { skill -> 

                skill == normalizedTerm
            }
        }
        

        matchingUsers.take(limit)
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
        
        allResults.forEach { (term, users) ->
            users.forEach { user ->
                if (term in user.skills.toSet()) {
                    userMap[user.id] = user
                    userMatchesMap.getOrPut(user.id) { mutableSetOf() }.add(term)
                }
            }
        }

        val ranked = userMap.values.map { user ->
            val matches = userMatchesMap[user.id]?.size ?: 0
            Ranked(user, matches)
        }
            .filter { it.matches > 0 }
            .sortedWith(
                compareByDescending<Ranked> { it.matches }
                    .thenByDescending { it.user.skills.size }
            )
        
        ranked.map { it.user }.take(limit)
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
