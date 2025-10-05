package com.example.talent_bridge_kt.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

object AuthManager {

    private const val TAG = "AuthManager"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    val currentUser: FirebaseUser?
        get() = auth.currentUser


    fun isLoggedIn(): Boolean = auth.currentUser != null

    private fun randomNumericId(length: Int = 8): String =
        (1..length).joinToString("") { Random.nextInt(0, 10).toString() }


    private fun addEvent(type: String, uid: String, onDone: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        val eventDoc = mapOf(
            "meta" to mapOf(
                "uid" to uid,
                "type" to type,
                "ts" to FieldValue.serverTimestamp(),
                "language" to "kt"
            )
        )
        db.collection("events").add(eventDoc)
            .addOnSuccessListener { onDone?.invoke() }
            .addOnFailureListener { e -> onError?.invoke(e.message ?: "Error al guardar evento") }
    }

    fun register(
        email: String,
        password: String,
        displayName: String = "",
        idDigits: Int = 8,
        defaultIsPublic: Boolean = true,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                createUserProfile(
                    displayName = displayName,
                    email = email,
                    idDigits = idDigits,
                    isPublic = defaultIsPublic,
                    onSuccess = {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            addEvent(type = "signup", uid = uid)
                        }
                        onSuccess()
                    },
                    onError = { msg -> onError("Cuenta creada pero falló el perfil: $msg") }
                )
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido al registrar")
            }
    }

    private fun createUserProfile(
        displayName: String,
        email: String,
        idDigits: Int,
        isPublic: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("UID no disponible")
        val numericId = randomNumericId(idDigits)

        val profile = hashMapOf(
            "description" to "",
            "displayName" to displayName,
            "email" to email,
            "headline" to "",
            "id" to numericId,
            "isPublic" to isPublic,
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "lastPortfolioUpdateAt" to "",
            "linkedin" to "",
            "location" to "",
            "major" to "",
            "mobileNumber" to "",
            "photoUrl" to "",
            "projects" to emptyList<String>(),
            "skillsOrTopics" to emptyList<String>(),

            
        )

        db.collection("users").document(uid).set(profile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al crear perfil") }
    }


    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    addEvent(type = "login", uid = uid)
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onError(e.message ?: "Log in error") }
    }

    /**
     * Envía correo de recuperación de contraseña.
     */
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al enviar correo de recuperación")
            }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        auth.signOut()
    }
}
