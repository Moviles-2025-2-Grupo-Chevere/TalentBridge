package com.example.talent_bridge_kt

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.talent_bridge_kt.core.conectivity.AndroidConnectivityObserver

class ConnectivityViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ConnectivityViewModel::class.java))
        return ConnectivityViewModel(
            connectivityObserver = AndroidConnectivityObserver(context)
        ) as T
    }
}

