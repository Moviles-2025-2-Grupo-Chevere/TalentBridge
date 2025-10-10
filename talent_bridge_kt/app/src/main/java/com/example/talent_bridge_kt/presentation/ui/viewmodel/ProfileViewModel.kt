package com.example.talent_bridge_kt.presentation.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.model.Project
import com.example.talent_bridge_kt.domain.usecase.GetProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UpdateProfileUseCase
import com.example.talent_bridge_kt.domain.usecase.UploadAvatarUseCase
import com.example.talent_bridge_kt.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.talent_bridge_kt.data.analytics.ProfileAnalytics

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Ready(val profile: Profile, val message: String? = null) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val uploadAvatar: UploadAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun load() = viewModelScope.launch {
        _uiState.value = ProfileUiState.Loading
        when (val res = getProfile()) {
            is Resource.Success -> {
                _uiState.value = ProfileUiState.Ready(res.data)
                ProfileAnalytics.pushUserProperties(res.data)   // <- AQUÍ
            }
            is Resource.Error   -> _uiState.value = ProfileUiState.Error(res.message)
        }
    }
    fun refresh() = viewModelScope.launch {
        _uiState.value = ProfileUiState.Loading
        when (val res = getProfile()) {
            is Resource.Success -> _uiState.value = ProfileUiState.Ready(res.data)
            is Resource.Error   -> _uiState.value = ProfileUiState.Error(res.message)
        }
    }

    fun onAvatarPicked(uri: Uri) = viewModelScope.launch {
        when (val res = uploadAvatar(uri)) {
            is Resource.Success -> {
                val current = (uiState.value as? ProfileUiState.Ready)?.profile
                if (current != null) {
                    _uiState.value = ProfileUiState.Ready(current.copy(avatarUrl = res.data))
                } else refresh()
            }
            is Resource.Error -> _uiState.value = ProfileUiState.Error(res.message)
        }
    }

    fun update(profile: Profile) = viewModelScope.launch {
        val before = (uiState.value as? ProfileUiState.Ready)?.profile
        val projectsChanged = before?.projects != profile.projects

        when (val res = updateProfile(profile)) {
            is Resource.Success -> {
                _uiState.value = ProfileUiState.Ready(res.data, "Saved")
                if (projectsChanged) {
                    com.example.talent_bridge_kt.data.analytics.ProfileAnalytics
                        .logProjectsUpdated(res.data.projects.size)
                }
            }
            is Resource.Error -> _uiState.value = ProfileUiState.Error(res.message)
        }
    }


    fun addTag(tag: String) {
        val cur = (uiState.value as? ProfileUiState.Ready)?.profile ?: return
        if (tag.isBlank() || cur.tags.any { it.equals(tag, ignoreCase = true) }) return
        update(cur.copy(tags = cur.tags + tag.trim()))
    }

    fun removeTag(tag: String) {
        val cur = (uiState.value as? ProfileUiState.Ready)?.profile ?: return
        update(cur.copy(tags = cur.tags.filterNot { it.equals(tag, ignoreCase = true) }))
    }

    fun addProject(project: Project) {
        val cur = (uiState.value as? ProfileUiState.Ready)?.profile ?: return
        update(cur.copy(projects = cur.projects + project))
    }

    fun removeProject(projectId: String) {
        val cur = (uiState.value as? ProfileUiState.Ready)?.profile ?: return
        update(cur.copy(projects = cur.projects.filterNot { it.id == projectId }))
    }
}
