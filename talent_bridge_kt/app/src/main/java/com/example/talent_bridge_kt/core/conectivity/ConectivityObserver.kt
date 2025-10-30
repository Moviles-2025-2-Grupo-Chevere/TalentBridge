package com.example.talent_bridge_kt.core.conectivity

import kotlinx.coroutines.flow.Flow

interface ConectivityObserver {
    val isConnected: Flow<Boolean>
    
    // Método para obtener el estado actual una sola vez
    suspend fun observe(): Flow<Boolean> = isConnected
}