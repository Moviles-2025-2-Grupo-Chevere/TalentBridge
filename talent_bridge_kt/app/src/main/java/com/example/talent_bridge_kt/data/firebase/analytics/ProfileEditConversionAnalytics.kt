package com.example.talent_bridge_kt.data.firebase.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Firebase Analytics tracking para medir conversión de "Edit Profile" → "Save Changes"
 * 
 * Eventos:
 * - "profile_edit_started": Cuando un usuario hace clic en el botón "Edit profile"
 * - "profile_save_attempted": Cuando un usuario hace clic en "Save" (intento de guardar)
 * - "profile_save_success": Cuando el perfil se guarda exitosamente
 * - "profile_save_failed": Cuando el guardado falla
 * - "profile_edit_cancelled": Cuando el usuario cancela la edición sin guardar
 * 
 * BigQuery Analysis:
 * Para calcular la tasa de conversión:
 * SELECT 
 *   COUNT(DISTINCT CASE WHEN event_name = 'profile_edit_started' THEN user_pseudo_id END) AS users_who_started_editing,
 *   COUNT(DISTINCT CASE WHEN event_name = 'profile_save_success' THEN user_pseudo_id END) AS users_who_saved_successfully,
 *   ROUND(
 *     COUNT(DISTINCT CASE WHEN event_name = 'profile_save_success' THEN user_pseudo_id END) * 100.0 /
 *     NULLIF(COUNT(DISTINCT CASE WHEN event_name = 'profile_edit_started' THEN user_pseudo_id END), 0),
 *     2
 *   ) AS conversion_rate_percent
 * FROM `your-project.analytics_XXXXX.events_*`
 * WHERE _TABLE_SUFFIX BETWEEN '20240101' AND '20241231'
 */
object ProfileEditConversionAnalytics {

    /**
     * Trackea cuando un usuario inicia la edición del perfil (hace clic en "Edit")
     */
    fun logProfileEditStarted() {
        try {
            Firebase.analytics.logEvent("profile_edit_started") {
                // No necesita parámetros adicionales
            }
            
            android.util.Log.d("ProfileEditConversionAnalytics", "Profile edit started")
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditConversionAnalytics", "Error logging profile_edit_started", e)
        }
    }

    /**
     * Trackea cuando un usuario intenta guardar el perfil (hace clic en "Save")
     * 
     * @param hasChanges true si hubo cambios en el perfil, false si no
     */
    fun logProfileSaveAttempted(hasChanges: Boolean = true) {
        try {
            Firebase.analytics.logEvent("profile_save_attempted") {
                param("has_changes", if (hasChanges) "true" else "false")
            }
            
            android.util.Log.d("ProfileEditConversionAnalytics", "Profile save attempted: hasChanges=$hasChanges")
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditConversionAnalytics", "Error logging profile_save_attempted", e)
        }
    }

    /**
     * Trackea cuando el perfil se guarda exitosamente
     * 
     * @param fieldsChanged Lista de campos que fueron modificados (opcional)
     */
    fun logProfileSaveSuccess(fieldsChanged: List<String>? = null) {
        try {
            Firebase.analytics.logEvent("profile_save_success") {
                fieldsChanged?.let {
                    param("fields_changed_count", it.size.toLong())
                    // Opcional: puedes agregar más detalles si necesitas
                }
            }
            
            android.util.Log.d("ProfileEditConversionAnalytics", 
                "Profile save success: fieldsChanged=${fieldsChanged?.size ?: 0}")
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditConversionAnalytics", "Error logging profile_save_success", e)
        }
    }

    /**
     * Trackea cuando el guardado del perfil falla
     * 
     * @param errorMessage Mensaje de error (opcional)
     */
    fun logProfileSaveFailed(errorMessage: String? = null) {
        try {
            Firebase.analytics.logEvent("profile_save_failed") {
                errorMessage?.let {
                    // Firebase Analytics tiene límite de 100 caracteres para parámetros de string
                    param("error_type", it.take(100))
                }
            }
            
            android.util.Log.d("ProfileEditConversionAnalytics", 
                "Profile save failed: errorMessage=$errorMessage")
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditConversionAnalytics", "Error logging profile_save_failed", e)
        }
    }

    /**
     * Trackea cuando un usuario cancela la edición sin guardar
     */
    fun logProfileEditCancelled() {
        try {
            Firebase.analytics.logEvent("profile_edit_cancelled") {
                // No necesita parámetros adicionales
            }
            
            android.util.Log.d("ProfileEditConversionAnalytics", "Profile edit cancelled")
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditConversionAnalytics", "Error logging profile_edit_cancelled", e)
        }
    }
}

