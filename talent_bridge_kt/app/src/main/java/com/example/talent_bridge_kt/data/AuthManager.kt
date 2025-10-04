package com.example.talent_bridge_kt.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser


    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido al registrar")
            }
    }


    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido al iniciar sesión")
            }
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
