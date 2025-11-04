package com.example.talent_bridge_kt.data.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun uploadProjectImage(projectId: String, uri: Uri): String {

        val ref = storage.reference
            .child("project_images")
            .child(projectId)


        ref.putFile(uri).await()


        return ref.downloadUrl.await().toString()
    }
}
