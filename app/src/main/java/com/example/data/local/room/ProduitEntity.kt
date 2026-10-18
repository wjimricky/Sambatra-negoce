package com.example.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour les Produits (Grains et Épices).
 * Gère les paramètres de gestion de stock, les seuils de sécurité et les critères
 * stricts de conservation issus du Guide de Référence 2026 (Ch. 5, 6 & 8).
 */
@Entity(tableName = "produits")
data class ProduitEntity(
    @PrimaryKey
    val id: String, // ex: "prod_riz", "prod_girofle", "prod_poivre"
    val code: String, // ex: "RIZ", "HAR", "MAI", "PDC", "GIR", "POI", "GIN", "CUR"
    val nom: String, // ex: "Riz blanc usiné (Makalioka)"
    val nomMalagasy: String, // ex: "Vary fotsy madio (Makalioka)"
    val type: String, // "GRAIN" (Céréale/Légumineuse) ou "SPICE" (Épice noble)
    val classeAbc: String, // "A" (80% valeur), "B" (15%), "C" (5%) (Guide Ch. 6.2, p. 31)
    val variete: String, // Variété spécifique (Makalioka, Tsipoy, Voatsiperifery, CG1)
    val cmupCostAr: Double, // Coût Moyen Unitaire Pondéré (Guide Ch. 6.8, p. 34)
    val prixVenteGrosAr: Double, // Prix de vente de gros conseillé rendu entrepôt
    val consoJournaliereMoyenneKg: Double, // CMJ (Consommation Moyenne Journalière en kg)
    
    // --- SEUILS DE SÉCURITÉ & RÉAPPROVISIONNEMENT (Guide 2026 Ch. 6.3, p. 31) ---
    val stockSecuriteKg: Double, // Stock de sécurité minimal (ex: CMJ x 14 jours)
    val pointDeCommandeKg: Double, // Point de commande (CMJ x délai d'approvisionnement + sécurité)
    val stockAlerteCritiqueKg: Double = 0.0, // Seuil d'urgence absolue de rupture
    val couvertureCibleJours: Int = 30, // Couverture optimale de stock en jours
    val delaiApprovisionnementJours: Int = 10, // Délai moyen de réassort fournisseur

    // --- CRITÈRES DE CONSERVATION & QUALITÉ (Guide 2026 Ch. 5, p. 21-26 & Ch. 8) ---
    val seuilHumiditeMaxPercent: Double, // Humidité maximale tolérée à l'humidimètre (ex: 14.0% riz, 12.0% poivre)
    val humiditeCiblePercent: Double = 12.5, // Taux d'humidité cible optimal de stockage
    val temperatureStockageMaxC: Double = 25.0, // Température ambiante maximale tolérée dans l'entrepôt
    val sensibiliteHygroscopique: String = "MOYENNE", // "ELEVEE" (épices), "MOYENNE" (riz/maïs), "FAIBLE"
    val compatibiliteOlfactive: Boolean = true, // False si épice à odeur volatile forte (nécessite isolement étanche)
    val dureeConservationMaxJours: Int = 180, // Durée maximale de stockage avant risque de détérioration/bruches
    val tauxFreinteTolerePercent: Double = 0.50, // Seuil de freinte naturelle tolérée (Guide p. 32)
    
    // --- CONDITIONNEMENT & EMPLACEMENT ---
    val formatConditionnement: String, // "Sacs 25 kg", "Sacs 50 kg", "Sacs étanches 10 kg"
    val zoneEntrepot: String, // "ZONE_A" (Céréales), "ZONE_B" (Épices sécurisées), "ZONE_C" (Légumineuses)
    val risquesSpecifiques: String // Principaux risques de dégradation (bruches, moisissures, condensation, vol)
)
