package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les Produits (Grains & Épices).
 * Inclut les requêtes de surveillance des seuils de réapprovisionnement et stocks de sécurité.
 */
@Dao
interface ProduitDao {

    @Query("SELECT * FROM produits ORDER BY classeAbc ASC, nom ASC")
    fun getAllProduits(): Flow<List<ProduitEntity>>

    @Query("SELECT * FROM produits WHERE type = :type ORDER BY nom ASC")
    fun getProduitsByType(type: String): Flow<List<ProduitEntity>>

    @Query("SELECT * FROM produits WHERE id = :id")
    suspend fun getProduitById(id: String): ProduitEntity?

    @Query("SELECT * FROM produits WHERE code = :code")
    suspend fun getProduitByCode(code: String): ProduitEntity?

    // Produits nécessitant un réapprovisionnement : stock total disponible <= point de commande
    @Query("""
        SELECT p.* FROM produits p 
        LEFT JOIN (
            SELECT produitId, SUM(quantiteDisponibleKg) as totalDispo 
            FROM stocks_lots 
            WHERE estBloqueQuarantaine = 0 
            GROUP BY produitId
        ) s ON p.id = s.produitId 
        WHERE COALESCE(s.totalDispo, 0) <= p.pointDeCommandeKg 
        ORDER BY (COALESCE(s.totalDispo, 0) / p.pointDeCommandeKg) ASC
    """)
    fun getProduitsPointDeCommandeAtteint(): Flow<List<ProduitEntity>>

    // Produits en alerte critique de stock de sécurité
    @Query("""
        SELECT p.* FROM produits p 
        LEFT JOIN (
            SELECT produitId, SUM(quantiteDisponibleKg) as totalDispo 
            FROM stocks_lots 
            WHERE estBloqueQuarantaine = 0 
            GROUP BY produitId
        ) s ON p.id = s.produitId 
        WHERE COALESCE(s.totalDispo, 0) <= p.stockSecuriteKg 
        ORDER BY (COALESCE(s.totalDispo, 0) / p.stockSecuriteKg) ASC
    """)
    fun getProduitsStockSecuriteAlerte(): Flow<List<ProduitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduit(produit: ProduitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(produits: List<ProduitEntity>)

    @Update
    suspend fun updateProduit(produit: ProduitEntity)

    @Query("UPDATE produits SET cmupCostAr = :newCmup, prixVenteGrosAr = :newPrixVente WHERE id = :id")
    suspend fun updateCmupAndPrice(id: String, newCmup: Double, newPrixVente: Double)

    @Delete
    suspend fun deleteProduit(produit: ProduitEntity)
}
