package com.example.talent_bridge_kt.domain.repository
import com.example.talent_bridge_kt.domain.model.User

interface SearchRepository {
    suspend fun searchUsers(
        queryCsv: String,
        mode: String = "any",
        limit: Int = 20
    ): List<User>
}