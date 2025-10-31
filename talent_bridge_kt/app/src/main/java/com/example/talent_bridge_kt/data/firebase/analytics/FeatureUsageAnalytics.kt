package com.example.talent_bridge_kt.data.firebase.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Firebase Analytics tracking para uso de features principales
 * 
 * Trackea el uso de las features core:
 * - "Create a project"
 * - "Apply project" 
 * - "Edit a profile"
 */
object FeatureUsageAnalytics {

    /**
     * Trackea cuando un usuario crea un proyecto
     */
    fun logCreateProject(projectId: String? = null) {
        try {
            Firebase.analytics.logEvent("create_project") {
                if (projectId != null) {
                    param("project_id", projectId)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FeatureUsageAnalytics", "Error logging create_project", e)
        }
    }

    /**
     * Trackea cuando un usuario aplica a un proyecto
     * Nota: Este evento también se envía desde ApplicationAnalytics, 
     * pero aquí lo trackeamos como feature usage
     */
    fun logApplyProject(projectId: String? = null) {
        try {
            Firebase.analytics.logEvent("apply_project") {
                if (projectId != null) {
                    param("project_id", projectId)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FeatureUsageAnalytics", "Error logging apply_project", e)
        }
    }

    /**
     * Trackea cuando un usuario edita su perfil
     */
    fun logEditProfile() {
        try {
            Firebase.analytics.logEvent("edit_profile") {
                // No necesitamos parámetros adicionales para este evento
            }
        } catch (e: Exception) {
            android.util.Log.e("FeatureUsageAnalytics", "Error logging edit_profile", e)
        }
    }
}

