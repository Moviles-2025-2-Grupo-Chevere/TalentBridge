package com.example.talent_bridge_kt.data.contacts

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ContactRequestRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun sendContactRequest(
        fromUid: String,
        toUid: String,
        fromName: String? = null,
        toName: String? = null,
        fromEmail: String? = null,
        toEmail: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val now = Timestamp.now()
        val data = mapOf(
            "fromUid" to fromUid,
            "fromName" to fromName,
            "fromEmail" to fromEmail,
            "toUid" to toUid,
            "toName" to toName,
            "toEmail" to toEmail,
            // Para compatibilidad histórica: timestamp principal
            "timestamp" to now,
            // Campos usados por Contact Center
            "reviewed" to false,
            "contactRequestTime" to now,
            // Se establecerá cuando se haga review
            "reviewTime" to null
        )

        db.collection("contactRequests")
            .add(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}
