
package com.example.talent_bridge_kt.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.Semaphore

data class PortfolioUploadResult(
    val id: String,
    val url: String,
    val storagePath: String,
    val fileName: String,
    val title: String,
    val bytes: Long
)

class PortfolioRepositorySimple(
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val contentResolver: ContentResolver
) {
    data class Progress(val uri: Uri, val percent: Int)

    suspend fun uploadAll(
        items: List<Pair<Uri, String>>, // Uri and title
        onProgress: (Progress) -> Unit = {}
    ): List<PortfolioUploadResult> = coroutineScope {
        val uid = auth.currentUser?.uid ?: error("No authenticated user.")
        val limiter = Semaphore(3) // Multithreading: máximo 3 uploads simultáneos

        items.map { (uri, title) ->
            async(Dispatchers.IO) {
                limiter.acquire()
                try {
                    uploadOne(uid, uri, title, onProgress)
                } finally {
                    limiter.release()
                }
            }
        }.awaitAll()
    }

    private suspend fun uploadOne(
        uid: String,
        uri: Uri,
        title: String,
        onProgress: (Progress) -> Unit
    ): PortfolioUploadResult = withContext(Dispatchers.IO) {
        val guessedName = resolveFileName(uri) ?: "portfolio_${UUID.randomUUID()}.pdf"
        val path = "portfolios/$uid/$guessedName"
        val ref = storage.reference.child(path)

        val sizeBytes = try { contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0L } catch (_: Exception) { 0L }

        val task = ref.putFile(uri)
        task.addOnProgressListener { s ->
            val p = if (s.totalByteCount > 0) ((s.bytesTransferred * 100) / s.totalByteCount).toInt().coerceIn(0, 100) else 0
            onProgress(Progress(uri, p))
        }
        task.await()

        val url = ref.downloadUrl.await().toString()

        val data = mapOf(
            "userId" to uid,
            "fileName" to guessedName,
            "title" to title,
            "url" to url,
            "storagePath" to path,
            "bytes" to sizeBytes,
            "contentType" to "application/pdf",
            "uploadedAt" to com.google.firebase.Timestamp.now()
        )

        val doc = db.collection("users").document(uid).collection("portfolios").document()
        doc.set(data).await()

        PortfolioUploadResult(
            id = doc.id,
            url = url,
            storagePath = path,
            fileName = guessedName,
            title = title,
            bytes = sizeBytes
        )
    }

    private fun resolveFileName(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && idx != -1) it.getString(idx) else null
            }
        } catch (_: Exception) { null }
    }
}

