package com.example.talent_bridge_kt.domain.usecase

import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): Resource<Profile> {
        // Aquí puedes insertar validaciones de dominio si deseas
        return repository.updateProfile(profile)
    }
}
