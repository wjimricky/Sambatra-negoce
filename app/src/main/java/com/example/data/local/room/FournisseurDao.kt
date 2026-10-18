package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FournisseurDao {
    @Query("SELECT * FROM fournisseurs ORDER BY noteSelection5 DESC")
    fun getAllFournisseurs(): Flow<List<FournisseurEntity>>

    @Query("SELECT * FROM fournisseurs WHERE id = :id")
    suspend fun getFournisseurById(id: String): FournisseurEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFournisseur(fournisseur: FournisseurEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fournisseurs: List<FournisseurEntity>)

    @Update
    suspend fun updateFournisseur(fournisseur: FournisseurEntity)

    @Query("UPDATE fournisseurs SET avancesEnCoursAr = :nouvellesAvances WHERE id = :id")
    suspend fun updateAvances(id: String, nouvellesAvances: Double)

    @Delete
    suspend fun deleteFournisseur(fournisseur: FournisseurEntity)
}
