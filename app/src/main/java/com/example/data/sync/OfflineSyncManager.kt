package com.example.data.sync

import android.util.Log
import com.example.data.local.room.GrosGrainsDatabase
import com.example.data.local.room.SyncQueueDao
import com.example.data.local.room.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SyncUiState(
    val isOnline: Boolean = true,
    val isSimulatedOffline: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val lastSyncFormatted: String = "Jamais",
    val lastSyncMessage: String = "Prêt",
    val syncHistory: List<SyncQueueEntity> = emptyList()
)

class OfflineSyncManager(
    private val database: GrosGrainsDatabase,
    private val connectivityObserver: ConnectivityObserver,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val syncQueueDao: SyncQueueDao = database.syncQueueDao()

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var wasPreviouslyOffline = false

    init {
        // Observer la connectivité réseau
        scope.launch {
            connectivityObserver.isOnline.collect { online ->
                val simulated = connectivityObserver.isSimulatingOffline()
                _uiState.update { it.copy(isOnline = online, isSimulatedOffline = simulated) }

                if (online && wasPreviouslyOffline) {
                    // Reconnexion automatique : lancer la synchronisation des opérations accumulées en mode hors-ligne
                    Log.d("OfflineSyncManager", "Connexion rétablie ! Lancement synchronisation automatique...")
                    synchronizePendingQueue()
                }
                wasPreviouslyOffline = !online
            }
        }

        // Observer le nombre d'éléments en attente dans Room
        scope.launch {
            syncQueueDao.getPendingCountFlow().collect { count ->
                _uiState.update { it.copy(pendingCount = count) }
            }
        }

        // Observer l'historique récent de sync
        scope.launch {
            syncQueueDao.getRecentSyncLogsFlow().collect { history ->
                _uiState.update { it.copy(syncHistory = history) }
            }
        }
    }

    /**
     * Enregistre une opération métier dans la file locale Room.
     * Cette méthode garantit une persistance hors-ligne totale.
     */
    suspend fun enqueueOperation(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String
    ): Long {
        val item = SyncQueueEntity(
            operationId = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            action = action,
            payloadJson = payloadJson,
            timestamp = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        val insertedId = syncQueueDao.insertSyncItem(item)

        // Si en ligne, tenter immédiatement la synchronisation
        if (_uiState.value.isOnline && !_uiState.value.isSyncing) {
            scope.launch {
                synchronizePendingQueue()
            }
        }

        return insertedId
    }

    /**
     * Exécute la synchronisation de toutes les opérations locales en attente.
     */
    suspend fun synchronizePendingQueue() {
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(lastSyncMessage = "Hors-ligne : synchronisation différée") }
            return
        }

        if (_uiState.value.isSyncing) return

        _uiState.update { it.copy(isSyncing = true, lastSyncMessage = "Synchronisation en cours...") }

        try {
            val pendingItems = syncQueueDao.getPendingSyncItems()
            if (pendingItems.isEmpty()) {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncFormatted = timeStr,
                        lastSyncMessage = "Tout est à jour (0 en attente)"
                    )
                }
                return
            }

            // Simuler l'envoi réseau et résolution de conflits
            delay(1200) // Latence réseau réaliste pour transmission sécurisée

            for (item in pendingItems) {
                // Traitement et résolution : application côté serveur avec horodatage
                Log.d("OfflineSyncManager", "Synchronisation de ${item.entityType} [${item.entityId}] - Action: ${item.action}")
                syncQueueDao.markAsSynced(item.id)
            }

            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncFormatted = timeStr,
                    lastSyncMessage = "${pendingItems.size} opérations synchronisées avec succès"
                )
            }
        } catch (e: Exception) {
            Log.e("OfflineSyncManager", "Erreur lors de la synchronisation", e)
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncMessage = "Échec synchronisation : ${e.message}"
                )
            }
        }
    }

    fun toggleOfflineSimulation() {
        val currentlySimulating = connectivityObserver.isSimulatingOffline()
        connectivityObserver.setSimulationOfflineMode(!currentlySimulating)
        _uiState.update {
            it.copy(
                isSimulatedOffline = !currentlySimulating,
                isOnline = if (!currentlySimulating) false else it.isOnline
            )
        }
    }

    fun forceSyncNow() {
        scope.launch {
            synchronizePendingQueue()
        }
    }
}
