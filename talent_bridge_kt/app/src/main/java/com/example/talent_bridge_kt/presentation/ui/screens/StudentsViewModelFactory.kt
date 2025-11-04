package com.example.talent_bridge_kt.presentation.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.talent_bridge_kt.presentation.ui.viewmodel.StudentsViewModel

fun studentsVmFactory(app: Application): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StudentsViewModel::class.java))
            return StudentsViewModel(app) as T
        }
    }
