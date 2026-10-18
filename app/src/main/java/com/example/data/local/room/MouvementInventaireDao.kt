package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les Mouvements d'Inventaire et le Registre des Pertes & Freintes.
 * Fournit les agrégats nécessaires aux rapports mensuels KPI et au calcul des marges nettes.
 */
@Dao
interface MouvementInventaireDao {

    @Query("SELECT * FROM mouvements_inventaire ORDER BY id DESC")
    fun getAllMouvements(): Flow<List<MouvementInventaireEntity>>

    @Query("SELECT * FROM mouvements_inventaire WHERE numeroLot = :numeroLot ORDER BY id DESC")
    fun getMouvementsForLot(numeroLot: String): Flow<List<MouvementInventaireEntity>>

    @Query("SELECT * FROM mouvements_inventaire WHERE produitId = :produitId ORDER BY id DESC")
    fun getMouvementsForProduit(produitId: String): Flow<List<MouvementInventaireEntity>>

    // Registre exclusif des pertes et freintes (Guide p. 33-34)
    @Query("SELECT * FROM mouvements_inventaire WHERE typeMouvement LIKE 'PERTE_%' ORDER BY id DESC")
    fun getMouvementsPertes(): Flow<List<MouvementInventaireEntity>>

    // Pertes groupées par cause pour le rapport mensuel KPI
    @Query("""
        SELECT typeMouvement, SUM(quantiteKg) as totalKg, SUM(valeurTotaleAr) as totalAr 
        FROM mouvements_inventaire 
        WHERE typeMouvement LIKE 'PERTE_%' 
        GROUP BY typeMouvement
    """)
    fun getSynthesePertesParCause(): Flow<List<PerteSyntheseTuple>>

    // Total des pertes en kilogrammes
    @Query("SELECT SUM(quantiteKg) FROM mouvements_inventaire WHERE typeMouvement LIKE 'PERTE_%'")
    fun getTotalPertesKg(): Flow<Double?>

    // Valeur financière totale des pertes (Ar)
    @Query("SELECT SUM(valeurTotaleAr) FROM mouvements_inventaire WHERE typeMouvement LIKE 'PERTE_%'")
    fun getValeurTotalePertesAr(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMouvement(mouvement: MouvementInventaireEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mouvements: List<MouvementInventaireEntity>)

    @Delete
    suspend fun deleteMouvement(mouvement: MouvementInventaireEntity)
}

/**
 * Tuple pour l'agrégation des pertes par catégorie du Guide 2026
 */
data class PerteSyntheseTuple(
    val typeMouvement: String,
    val totalKg: Double,
    val totalAr: Double
)
