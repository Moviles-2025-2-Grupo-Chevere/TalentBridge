package com.example.talent_bridge_kt.core.conectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidConnectivityObserver(
    private val context: Context,
): ConectivityObserver {

    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    override val isConnected: Flow<Boolean>
        get() = callbackFlow {
            val callback = object : NetworkCallback() {
                override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) {
                    trySend(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                }
                override fun onUnavailable() { trySend(false) }
                override fun onLost(network: Network) { trySend(false) }
                override fun onAvailable(network: Network) { trySend(true) }
            }

            try {
                connectivityManager.registerDefaultNetworkCallback(callback)
            } catch (e: SecurityException) {
                trySend(false)
                close(e)
                return@callbackFlow
            }

            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }

}
