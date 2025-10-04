package com.example.talent_bridge_kt.data.fake

import android.net.Uri
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.model.Project
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource
import kotlinx.coroutines.delay

class FakeProfileRepository : ProfileRepository {

    private var current = Profile(
        id = "u1",
        name = "Luciana Perez",
        email = "lucianaperez@gmail.com",
        linkedin = "lucianap23",
        phone = null,
        avatarUrl = null,
        tags = listOf("Diseño", "UI/UX", "AI"),
        bio = "Interesado en proyectos con paga con relación a la IA.",
        headline = "Android Dev",
        isPublic = true,
        location = "Bogotá",
        projects = listOf(
            Project(
                title = "ML",
                description = "Clasificador de imágenes on-device",
                skills = listOf("ML", "TensorFlow Lite")
            )
        )
    )

    override suspend fun getProfile(): Resource<Profile> {
        delay(300)
        return Resource.Success(current)
    }

    override suspend fun updateProfile(profile: Profile): Resource<Profile> {
        delay(300)
        current = profile
        return Resource.Success(current)
    }

    override suspend fun uploadAvatar(localImage: Uri): Resource<String> {
        delay(500)
        val fakeUrl = "https://picsum.photos/seed/${localImage.hashCode()}/512"
        current = current.copy(avatarUrl = fakeUrl)
        return Resource.Success(fakeUrl)
    }
}
