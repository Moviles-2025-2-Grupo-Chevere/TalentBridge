package com.example.talent_bridge_kt.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.talent_bridge_kt.domain.repository.SearchRepository

class SearchViewModelFactory(
    private val searchRepo: SearchRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(searchRepo) as T
    }
}
