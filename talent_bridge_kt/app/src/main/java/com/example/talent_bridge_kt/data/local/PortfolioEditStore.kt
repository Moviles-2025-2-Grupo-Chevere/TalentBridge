package com.example.talent_bridge_kt.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val Context.portfolioEditDataStore by preferencesDataStore(name = "portfolio_edit_prefs")

data class PendingPortfolioEdit(
    val portfolioId: String,
    val title: String? = null  // null means no change
)

class PortfolioEditStore(private val context: Context) {

    companion object {
        private val KEY_LOCAL_PORTFOLIOS = stringPreferencesKey("local_portfolios_json")
        private val KEY_PENDING_EDITS_QUEUE = stringPreferencesKey("pending_portfolio_edits_queue")
        private val KEY_PENDING_DELETES_QUEUE = stringPreferencesKey("pending_portfolio_deletes_queue")
    }

    suspend fun saveLocalPortfolios(portfoliosJson: String) = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.edit { prefs ->
            prefs[KEY_LOCAL_PORTFOLIOS] = portfoliosJson
        }
    }

    suspend fun getLocalPortfolios(): String? = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.data.first()[KEY_LOCAL_PORTFOLIOS]
    }

    suspend fun addPendingEdit(edit: PendingPortfolioEdit) = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.edit { prefs ->
            val currentQueue = prefs[KEY_PENDING_EDITS_QUEUE] ?: "[]"
            val queueArray = JSONArray(currentQueue)
            
            val editObj = JSONObject().apply {
                put("portfolioId", edit.portfolioId)
                edit.title?.let { put("title", it) }
                put("createdAt", System.currentTimeMillis())
            }
            queueArray.put(editObj)
            
            prefs[KEY_PENDING_EDITS_QUEUE] = queueArray.toString()
        }
    }

    suspend fun getPendingEdits(): List<PendingPortfolioEdit> = withContext(Dispatchers.IO) {
        val queueJson = context.portfolioEditDataStore.data.first()[KEY_PENDING_EDITS_QUEUE] ?: return@withContext emptyList()
        
        try {
            val queueArray = JSONArray(queueJson)
            val result = ArrayList<PendingPortfolioEdit>(queueArray.length())
            for (i in 0 until queueArray.length()) {
                val obj = queueArray.getJSONObject(i)
                result.add(
                    PendingPortfolioEdit(
                        portfolioId = obj.getString("portfolioId"),
                        title = obj.optString("title").takeIf { obj.has("title") }
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clearPendingEdits() = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_EDITS_QUEUE)
        }
    }

    suspend fun applyLocalEdit(portfolioId: String, title: String?) = withContext(Dispatchers.IO) {
        val localPortfoliosJson = getLocalPortfolios() ?: return@withContext
        
        try {
            val portfoliosArray = JSONArray(localPortfoliosJson)
            val updatedArray = JSONArray()
            
            for (i in 0 until portfoliosArray.length()) {
                val portfolioObj = portfoliosArray.getJSONObject(i)
                if (portfolioObj.getString("id") == portfolioId) {
                    val updatedObj = JSONObject()
                    val keys = portfolioObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        updatedObj.put(key, portfolioObj.get(key))
                    }
                    title?.let { updatedObj.put("title", it) }
                    updatedArray.put(updatedObj)
                } else {
                    updatedArray.put(portfolioObj)
                }
            }
            
            saveLocalPortfolios(updatedArray.toString())
        } catch (e: Exception) {
            // Error applying edit
        }
    }

    suspend fun addPendingDelete(portfolioId: String, storagePath: String?) = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.edit { prefs ->
            val currentQueue = prefs[KEY_PENDING_DELETES_QUEUE] ?: "[]"
            val queueArray = JSONArray(currentQueue)
            
            val deleteObj = JSONObject().apply {
                put("portfolioId", portfolioId)
                storagePath?.let { put("storagePath", it) }
                put("createdAt", System.currentTimeMillis())
            }
            queueArray.put(deleteObj)
            
            prefs[KEY_PENDING_DELETES_QUEUE] = queueArray.toString()
        }
    }

    suspend fun getPendingDeletes(): List<Pair<String, String?>> = withContext(Dispatchers.IO) {
        val queueJson = context.portfolioEditDataStore.data.first()[KEY_PENDING_DELETES_QUEUE] ?: return@withContext emptyList()
        
        try {
            val queueArray = JSONArray(queueJson)
            val result = ArrayList<Pair<String, String?>>(queueArray.length())
            for (i in 0 until queueArray.length()) {
                val obj = queueArray.getJSONObject(i)
                val portfolioId = obj.getString("portfolioId")
                val storagePath = obj.optString("storagePath").takeIf { obj.has("storagePath") }
                result.add(portfolioId to storagePath)
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clearPendingDeletes() = withContext(Dispatchers.IO) {
        context.portfolioEditDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_DELETES_QUEUE)
        }
    }

    suspend fun removePendingDeletes(portfolioIds: Set<String>) = withContext(Dispatchers.IO) {
        val currentQueue = context.portfolioEditDataStore.data.first()[KEY_PENDING_DELETES_QUEUE] ?: return@withContext
        if (portfolioIds.isEmpty()) return@withContext
        
        try {
            val queueArray = JSONArray(currentQueue)
            val updatedArray = JSONArray()
            
            for (i in 0 until queueArray.length()) {
                val obj = queueArray.getJSONObject(i)
                val portfolioId = obj.getString("portfolioId")
                if (portfolioId !in portfolioIds) {
                    updatedArray.put(obj)
                }
            }
            
            context.portfolioEditDataStore.edit { prefs ->
                if (updatedArray.length() > 0) {
                    prefs[KEY_PENDING_DELETES_QUEUE] = updatedArray.toString()
                } else {
                    prefs.remove(KEY_PENDING_DELETES_QUEUE)
                }
            }
        } catch (e: Exception) {
            // Error processing queue
        }
    }

    suspend fun removeLocalPortfolio(portfolioId: String) = withContext(Dispatchers.IO) {
        val localPortfoliosJson = getLocalPortfolios() ?: return@withContext
        
        try {
            val portfoliosArray = JSONArray(localPortfoliosJson)
            val updatedArray = JSONArray()
            
            for (i in 0 until portfoliosArray.length()) {
                val portfolioObj = portfoliosArray.getJSONObject(i)
                if (portfolioObj.getString("id") != portfolioId) {
                    updatedArray.put(portfolioObj)
                }
            }
            
            saveLocalPortfolios(updatedArray.toString())
        } catch (e: Exception) {
            // Error removing portfolio
        }
    }
}

