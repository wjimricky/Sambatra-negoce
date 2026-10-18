package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les Lots de Stock et la Traçabilité.
 * Permet l'exécution des algorithmes de déstockage FIFO/FEFO et la surveillance
 * de l'humidité et de la quarantaine selon le Guide de Référence 2026.
 */
@Dao
interface StockLotDao {

    @Query("SELECT * FROM stocks_lots ORDER BY dateReception DESC")
    fun getAllLots(): Flow<List<StockLotEntity>>

    // Déstockage prioritaire FEFO (First Expired, First Out) pour limiter les pertes
    @Query("SELECT * FROM stocks_lots WHERE quantiteDisponibleKg > 0 AND estBloqueQuarantaine = 0 ORDER BY dateLimiteConservation ASC")
    fun getLotsPrioriteFefo(): Flow<List<StockLotEntity>>

    // Déstockage FIFO par produit
    @Query("SELECT * FROM stocks_lots WHERE produitId = :produitId AND quantiteDisponibleKg > 0 AND estBloqueQuarantaine = 0 ORDER BY dateReception ASC")
    fun getLotsDisponiblesFifo(produitId: String): Flow<List<StockLotEntity>>

    // Lots par zone de stockage (Zone A, B, C, Q)
    @Query("SELECT * FROM stocks_lots WHERE zoneStockageEntrepot = :zone AND quantiteDisponibleKg > 0 ORDER BY dateReception DESC")
    fun getLotsByZone(zone: String): Flow<List<StockLotEntity>>

    // Lots sous séquestre ou quarantaine
    @Query("SELECT * FROM stocks_lots WHERE estBloqueQuarantaine = 1 OR decisionControle = 'QUARANTAINE'")
    fun getLotsEnQuarantaine(): Flow<List<StockLotEntity>>

    // Lots en risque d'humidité excessive par rapport au seuil du produit
    @Query("""
        SELECT l.* FROM stocks_lots l 
        INNER JOIN produits p ON l.produitId = p.id 
        WHERE l.humiditeMesureePercent > p.seuilHumiditeMaxPercent 
        AND l.quantiteDisponibleKg > 0 
        ORDER BY (l.humiditeMesureePercent - p.seuilHumiditeMaxPercent) DESC
    """)
    fun getLotsRisqueHumidite(): Flow<List<StockLotEntity>>

    @Query("SELECT * FROM stocks_lots WHERE numeroLot = :numeroLot")
    suspend fun getLotByNumero(numeroLot: String): StockLotEntity?

    @Query("SELECT * FROM stocks_lots WHERE codeQrTracabilite = :qrCode")
    suspend fun getLotByQrCode(qrCode: String): StockLotEntity?

    // Quantité totale disponible par produit
    @Query("SELECT SUM(quantiteDisponibleKg) FROM stocks_lots WHERE produitId = :produitId AND estBloqueQuarantaine = 0")
    fun getTotalStockDisponibleKg(produitId: String): Flow<Double?>

    // Valeur financière totale du stock en magasin (Ar)
    @Query("SELECT SUM(quantiteDisponibleKg * coutUnitaireAchatAr) FROM stocks_lots WHERE estBloqueQuarantaine = 0")
    fun getValeurTotaleStockAr(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: StockLotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lots: List<StockLotEntity>)

    @Update
    suspend fun updateLot(lot: StockLotEntity)

    @Query("UPDATE stocks_lots SET quantiteDisponibleKg = :nouvelleQte WHERE numeroLot = :numeroLot")
    suspend fun updateQuantiteDisponible(numeroLot: String, nouvelleQte: Double)

    @Query("UPDATE stocks_lots SET zoneStockageEntrepot = :newZone WHERE numeroLot = :numeroLot")
    suspend fun updateZone(numeroLot: String, newZone: String)

    @Query("UPDATE stocks_lots SET statutConservation = :nouveauStatut WHERE numeroLot = :numeroLot")
    suspend fun updateStatutConservation(numeroLot: String, nouveauStatut: String)

    @Query("UPDATE stocks_lots SET estBloqueQuarantaine = :estBloque WHERE numeroLot = :numeroLot")
    suspend fun updateQuarantaine(numeroLot: String, estBloque: Boolean)

    @Delete
    suspend fun deleteLot(lot: StockLotEntity)
}
