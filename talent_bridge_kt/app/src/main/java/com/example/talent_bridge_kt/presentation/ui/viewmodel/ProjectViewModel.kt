package com.example.talent_bridge_kt.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_bridge_kt.data.repository.FirestoreProjectsRepository
import com.example.talent_bridge_kt.domain.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// ProjectsViewModelSimple.kt
class ProjectsViewModel(
    private val repo: FirestoreProjectsRepository = FirestoreProjectsRepository()
) : ViewModel() {

    val projects = MutableStateFlow<List<Project>>(emptyList())
    val loading  = MutableStateFlow(false)
    val error    = MutableStateFlow<String?>(null)

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        loading.value = true
        error.value = null
        try {
            projects.value = repo.fetchAllProjects()
        } catch (e: Exception) {
            error.value = e.message ?: "Error loading projects"
        } finally {
            loading.value = false
        }
    }
}
