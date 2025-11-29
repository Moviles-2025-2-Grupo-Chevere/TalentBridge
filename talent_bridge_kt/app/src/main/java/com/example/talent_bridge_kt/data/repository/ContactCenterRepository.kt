package com.example.talent_bridge_kt.data.repository

import com.example.talent_bridge_kt.domain.model.ContactRequest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class ContactCenterRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun loadRequestsForUser(userId: String): Pair<List<ContactRequest>, List<ContactRequest>> {
        // Recibidas: donde toUid == userId
        val receivedSnap = db.collection("contactRequests")
            .whereEqualTo("toUid", userId)
            .get()
            .await()

        // Enviadas: donde fromUid == userId
        val sentSnap = db.collection("contactRequests")
            .whereEqualTo("fromUid", userId)
            .get()
            .await()

        val received = receivedSnap.documents.mapNotNull { d -> d.toContactRequest() }
        val sent = sentSnap.documents.mapNotNull { d -> d.toContactRequest() }

        return received to sent
    }

    suspend fun markReviewedOnline(requestId: String, reviewTimeMs: Long) {
        val ts = Timestamp(Date(reviewTimeMs))
        val update = mapOf(
            "reviewed" to true,
            "reviewTime" to ts
        )
        db.collection("contactRequests")
            .document(requestId)
            .update(update)
            .await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toContactRequest(): ContactRequest? {
        val id = id
        val fromUid = getString("fromUid") ?: return null
        val toUid = getString("toUid") ?: return null

        val fromName = getString("fromName")
        val fromEmail = getString("fromEmail")
        val toName = getString("toName")
        val toEmail = getString("toEmail")

        val reviewed = getBoolean("reviewed") ?: false

        val contactTime = when (val raw = get("contactRequestTime") ?: get("timestamp")) {
            is Timestamp -> raw.toDate().time
            is Number -> raw.toLong()
            else -> System.currentTimeMillis()
        }

        val reviewTime = when (val raw = get("reviewTime")) {
            is Timestamp -> raw.toDate().time
            is Number -> raw.toLong()
            else -> null
        }

        return ContactRequest(
            id = id,
            fromUid = fromUid,
            fromName = fromName,
            fromEmail = fromEmail,
            toUid = toUid,
            toName = toName,
            toEmail = toEmail,
            reviewed = reviewed,
            contactRequestTime = contactTime,
            reviewTime = reviewTime
        )
    }
}


