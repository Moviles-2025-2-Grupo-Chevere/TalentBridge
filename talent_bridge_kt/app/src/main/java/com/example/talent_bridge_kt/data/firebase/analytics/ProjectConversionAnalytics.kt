package com.example.talent_bridge_kt.data.firebase.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Firebase Analytics tracking para medir conversión de "Save for Later" → "Apply"
 * 
 * Eventos:
 * - "project_saved": Cuando un usuario guarda un proyecto para después
 * - "project_application": Cuando un usuario aplica a un proyecto (con parámetro was_saved)
 * 
 * BigQuery Analysis:
 * Para calcular la tasa de conversión:
 * SELECT 
 *   COUNT(DISTINCT CASE WHEN event_name = 'project_application' AND was_saved = true THEN user_pseudo_id END) * 100.0 /
 *   COUNT(DISTINCT CASE WHEN event_name = 'project_saved' THEN user_pseudo_id END) as conversion_rate
 * FROM `your-project.analytics_XXXXX.events_*`
 * WHERE _TABLE_SUFFIX BETWEEN '20240101' AND '20241231'
 */
object ProjectConversionAnalytics {

    /**
     * Trackea cuando un usuario guarda un proyecto para después
     * 
     * @param projectId ID del proyecto guardado
     * @param projectTitle Título del proyecto (opcional)
     * @param sourceScreen Pantalla desde donde se guardó (opcional, para análisis)
     */
    fun logProjectSaved(
        projectId: String,
        projectTitle: String? = null,
        sourceScreen: String? = null
    ) {
        try {
            Firebase.analytics.logEvent("project_saved") {
                param("project_id", projectId)
                
                projectTitle?.let {
                    param("project_title", it)
                }
                
                sourceScreen?.let {
                    param("source_screen", it)
                }
            }
            
            android.util.Log.d("ProjectConversionAnalytics", 
                "Project saved: projectId='$projectId', projectTitle='$projectTitle'")
        } catch (e: Exception) {
            android.util.Log.e("ProjectConversionAnalytics", "Error logging project_saved", e)
        }
    }

    /**
     * Trackea cuando un usuario aplica a un proyecto
     * Incluye información sobre si el proyecto estaba guardado previamente
     * 
     * @param projectId ID del proyecto
     * @param projectTitle Título del proyecto (opcional)
     * @param wasSaved true si el proyecto estaba guardado antes de aplicar, false si no
     * @param sourceScreen Pantalla desde donde se aplicó (opcional, para análisis)
     */
    fun logProjectApplication(
        projectId: String,
        projectTitle: String? = null,
        wasSaved: Boolean,
        sourceScreen: String? = null
    ) {
        try {
            Firebase.analytics.logEvent("project_application") {
                param("project_id", projectId)
                param("was_saved", if (wasSaved) "true" else "false")
                
                projectTitle?.let {
                    param("project_title", it)
                }
                
                sourceScreen?.let {
                    param("source_screen", it)
                }
            }
            
            android.util.Log.d("ProjectConversionAnalytics", 
                "Project application: projectId='$projectId', wasSaved=$wasSaved")
        } catch (e: Exception) {
            android.util.Log.e("ProjectConversionAnalytics", "Error logging project_application", e)
        }
    }

    /**
     * Trackea cuando un usuario elimina un proyecto guardado (unsave)
     * 
     * @param projectId ID del proyecto
     * @param projectTitle Título del proyecto (opcional)
     */
    fun logProjectUnsaved(
        projectId: String,
        projectTitle: String? = null
    ) {
        try {
            Firebase.analytics.logEvent("project_unsaved") {
                param("project_id", projectId)
                
                projectTitle?.let {
                    param("project_title", it)
                }
            }
            
            android.util.Log.d("ProjectConversionAnalytics", 
                "Project unsaved: projectId='$projectId'")
        } catch (e: Exception) {
            android.util.Log.e("ProjectConversionAnalytics", "Error logging project_unsaved", e)
        }
    }
}

