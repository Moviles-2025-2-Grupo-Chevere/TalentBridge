package com.example.talent_bridge_kt.data.firebase.analytics

import com.google.firebase.Timestamp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Firebase Analytics tracking para aplicaciones a proyectos
 * 
 * Evento: "project_application"
 * Parámetros:
 * - semester: Período de 6 meses (calculado desde fecha base, no el semestre académico del estudiante)
 * - major: Major del usuario
 * - project_id: ID del proyecto
 * - project_title: Título del proyecto (opcional, para contexto)
 */
object ApplicationAnalytics {

    /**
     * Fecha base para calcular períodos de 6 meses
     * Período 1 empieza el 1 de enero de 2020
     */
    private val BASE_DATE = Calendar.getInstance().apply {
        set(2020, Calendar.JANUARY, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Calcula el período de 6 meses desde la fecha base
     * Ejemplo:
     * - Ene 2020 - Jun 2020 = Período 1
     * - Jul 2020 - Dic 2020 = Período 2
     * - Ene 2021 - Jun 2021 = Período 3
     * - etc.
     * 
     * @param date Timestamp de la fecha a calcular (null = fecha actual)
     * @return Número del período de 6 meses
     */
    private fun calculateSemesterPeriod(date: Timestamp? = null): Int {
        val targetDate = date?.toDate() ?: java.util.Date()
        val targetCalendar = Calendar.getInstance().apply {
            time = targetDate
        }
        
        val baseMillis = BASE_DATE.timeInMillis
        val targetMillis = targetCalendar.timeInMillis
        val diffMillis = targetMillis - baseMillis
        
        if (diffMillis < 0) return 1 // Si la fecha es anterior a la base, retorna 1
        
        // Calcular meses transcurridos desde la fecha base
        val diffMonths = (diffMillis / (1000L * 60 * 60 * 24 * 30)).toInt() // Aproximación: 30 días = 1 mes
        
        // Períodos de 6 meses
        val semesterPeriod = (diffMonths / 6) + 1
        
        return semesterPeriod
    }

    /**
     * Envía un evento a Firebase Analytics cuando un usuario aplica a un proyecto
     * 
     * @param semester Período de 6 meses (ignorado, se calcula automáticamente desde la fecha actual)
     * @param major Major del usuario
     * @param projectId ID del proyecto
     * @param projectTitle Título del proyecto (opcional)
     * @param lastLoginAt Timestamp del último login (ignorado, se usa fecha actual para el período)
     */
    fun logProjectApplication(
        semester: Int?,  // Ignorado - ya no se envía
        major: String?,
        projectId: String,
        projectTitle: String? = null,
        lastLoginAt: Timestamp? = null  // Ignorado - ya no se usa
    ) {
        try {
            // Asegurar que major siempre tenga un valor válido
            val majorValue = major?.trim()?.takeIf { it.isNotEmpty() } ?: "unknown"
            
            Firebase.analytics.logEvent("project_application") {
                param("major", majorValue)
                param("project_id", projectId)
                
                // Título del proyecto (opcional, pero útil para análisis)
                projectTitle?.let {
                    param("project_title", it)
                }
            }
            
            // Debug log
            android.util.Log.d("ApplicationAnalytics", 
                "Event sent: major='$majorValue', projectId='$projectId', projectTitle='$projectTitle'")
        } catch (e: Exception) {
            // Error al enviar analytics, pero no es crítico
            android.util.Log.e("ApplicationAnalytics", "Error sending analytics", e)
            e.printStackTrace()
        }
    }
}

