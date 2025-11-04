
package com.example.talent_bridge_kt.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.talent_bridge_kt.domain.model.ResumeLanguage
import com.example.talent_bridge_kt.domain.model.ResumeUploadResult
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

class ResumeRepositorySimple(
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val contentResolver: ContentResolver
) {
    data class Progress(val uri: Uri, val percent: Int)

    suspend fun uploadAll(
        items: List<Pair<Uri, ResumeLanguage>>,
        onProgress: (Progress) -> Unit = {}
    ): List<ResumeUploadResult> = coroutineScope {
        val uid = auth.currentUser?.uid ?: error("No authenticated user.")
        val limiter = Semaphore(3)

        items.map { (uri, lang) ->
            async(Dispatchers.IO) {
                limiter.acquire()
                try {
                    uploadOne(uid, uri, lang, onProgress)
                } finally {
                    limiter.release()
                }
            }
        }.awaitAll()
    }

    private suspend fun uploadOne(
        uid: String,
        uri: Uri,
        language: ResumeLanguage,
        onProgress: (Progress) -> Unit
    ): ResumeUploadResult = withContext(Dispatchers.IO) {
        val guessedName = resolveFileName(uri) ?: "cv_${language.name.lowercase()}_${UUID.randomUUID()}.pdf"
        val path = "resumes/$uid/${language.name}/$guessedName"
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
            "url" to url,
            "storagePath" to path,
            "language" to language.name,
            "bytes" to sizeBytes,
            "contentType" to "application/pdf",
            "uploadedAt" to com.google.firebase.Timestamp.now()
        )

        val doc = db.collection("users").document(uid).collection("resumes").document()
        doc.set(data).await()

        ResumeUploadResult(
            id = doc.id,
            url = url,
            storagePath = path,
            fileName = guessedName,
            language = language,
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
