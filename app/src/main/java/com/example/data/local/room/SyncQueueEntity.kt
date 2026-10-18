package com.example.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité de file de synchronisation hors-ligne pour la persistance locale Room.
 * Garantit que toute modification de stock, réception de lot, mouvement ou paiement
 * est enregistrée localement immédiatement, et synchronisée automatiquement au rétablissement du réseau.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationId: String,
    val entityType: String, // "STOCK_LOT", "INVENTORY_MOVEMENT", "CLIENT_PAYMENT", "QUALITY_RECEPTION", "ZONING_MOVE"
    val entityId: String,
    val action: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING", // "PENDING", "SYNCED", "FAILED"
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
