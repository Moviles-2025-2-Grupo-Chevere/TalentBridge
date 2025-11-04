package com.example.talent_bridge_kt.data.contacts

import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

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
        val data = mapOf(
            "fromUid" to fromUid,
            "fromName" to fromName,
            "fromEmail" to fromEmail,
            "toUid" to toUid,
            "toName" to toName,
            "toEmail" to toEmail,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("contactRequests")
            .add(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}
