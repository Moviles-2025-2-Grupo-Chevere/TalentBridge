package com.example.talent_bridge_kt.domain.usecase

import android.net.Uri
import com.example.talent_bridge_kt.domain.repository.ProfileRepository
import com.example.talent_bridge_kt.domain.util.Resource

class UploadAvatarUseCase(
    private val repository: ProfileRepository
) {
    /**
     * @return Resource<String> con la URL remota de la imagen subida
     */
    suspend operator fun invoke(localImage: Uri): Resource<String> {
        return repository.uploadAvatar(localImage)
    }
}
