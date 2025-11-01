package com.example.talent_bridge_kt.data.analytics

import com.example.talent_bridge_kt.domain.model.Profile
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object ProfileAnalytics {

    fun pushUserProperties(profile: Profile) {
        val fa = Firebase.analytics
        fa.setUserProperty(
            "up_has_linkedin",
            if (!profile.linkedin.isNullOrBlank()) "yes" else "no"
        )

        fa.setUserProperty("up_skills_count", profile.tags.size.toString())
    }

    fun logProfileSaved(profile: Profile) {
        Firebase.analytics.logEvent("profile_saved") {
            param("has_linkedin", if (!profile.linkedin.isNullOrBlank()) "yes" else "no")
            param("skills_count", profile.tags.size.toLong())
        }
    }

    fun logProjectsUpdated(projectCount: Int) {
        Firebase.analytics.logEvent("projects_updated") {
            param("project_count", projectCount.toLong())
        }
    }

}
