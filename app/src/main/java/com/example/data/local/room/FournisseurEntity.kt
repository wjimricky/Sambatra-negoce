package com.example.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour les Fournisseurs & Sourcing.
 * Grille de sélection sur 5 critères (p. 17) et suivi des avances de trésorerie (p. 19).
 */
@Entity(tableName = "fournisseurs")
data class FournisseurEntity(
    @PrimaryKey
    val id: String, // ex: "sup_analanjirofo"
    val nom: String, // "Coopérative Analanjirofo Espoir"
    val regionOrigine: String, // "Analanjirofo / Fénérive-Est"
    val contactTelephone: String, // "+261 32 55 123 45"
    val filieresFournies: String, // "Girofle clous, Poivre noir"
    val noteSelection5: Double, // Grille de notation sur 5 (Qualité 25%, Prix 20%, Délais 20%...)
    val tauxConformiteLotsPercent: Double, // % de lots acceptés sans réclamation
    val avancesEnCoursAr: Double, // Montant des avances d'argent en cours (règle de prudence p. 19)
    val estSourcePrincipale: Boolean // Règle d'or : avoir toujours au moins 2 sources par produit majeur
)
