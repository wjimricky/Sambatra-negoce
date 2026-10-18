package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

interface ConnectivityObserver {
    val isOnline: Flow<Boolean>
    fun setSimulationOfflineMode(forceOffline: Boolean)
    fun isSimulatingOffline(): Boolean
}

class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Mode simulation hors-ligne pour tests en entrepôt
    private val _isSimulatedOffline = MutableStateFlow(false)

    private val realNetworkFlow: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(false)
            }
        }

        // Vérifier l'état initial
        val activeNetwork = connectivityManager.activeNetwork
        val initialCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isCurrentlyConnected = initialCapabilities != null &&
                initialCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        trySend(isCurrentlyConnected)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    override val isOnline: Flow<Boolean> = combine(realNetworkFlow, _isSimulatedOffline) { realOnline, simulatedOffline ->
        if (simulatedOffline) false else realOnline
    }.distinctUntilChanged()

    override fun setSimulationOfflineMode(forceOffline: Boolean) {
        _isSimulatedOffline.value = forceOffline
    }

    override fun isSimulatingOffline(): Boolean {
        return _isSimulatedOffline.value
    }
}
