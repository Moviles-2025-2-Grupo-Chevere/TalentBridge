package com.example.talent_bridge_kt.domain.usecase

import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Resource<Profile> {
        return repository.getProfile()
    }
}
