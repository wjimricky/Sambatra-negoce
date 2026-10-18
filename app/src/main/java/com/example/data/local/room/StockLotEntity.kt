package com.example.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité Room pour les Lots en Stock et la Traçabilité selon le Guide 2026.
 * Implémente le codage normalisé [PROD]-[ORIG]-[AAMMJJ]-[N°] (p. 28),
 * le protocole de réception en 5 étapes avec contre-échantillon scellé (p. 22),
 * et les indicateurs de conservation dynamique (humidité, FEFO, alertes).
 */
@Entity(
    tableName = "stocks_lots",
    foreignKeys = [
        ForeignKey(
            entity = ProduitEntity::class,
            parentColumns = ["id"],
            childColumns = ["produitId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["produitId"]),
        Index(value = ["decisionControle"]),
        Index(value = ["zoneStockageEntrepot"]),
        Index(value = ["dateLimiteConservation"])
    ]
)
data class StockLotEntity(
    @PrimaryKey
    val numeroLot: String, // ex: "RIZ-ALA-260920-01", "GIR-ANA-260915-03" (Guide p. 28)
    val produitId: String, // Référence produit ("prod_riz", "prod_poivre", etc.)
    val fournisseurId: String, // Référence fournisseur audité
    val nomFournisseur: String,
    val regionOrigine: String, // Origine certifiée (Alaotra, Analanjirofo, Vakinankaratra)
    val dateReception: String, // Date de réception contradictoire
    
    // --- VOLUMÉTRIE & POIDS ---
    val quantiteInitialeKg: Double, // Poids net pesé à réception
    val quantiteDisponibleKg: Double, // Poids restant disponible en stock
    val nombreSacs: Int,
    val nombreSacsPeses: Int, // Contrôle : pesée obligatoire d'au moins 10% des sacs (Guide p. 22)

    // --- CRITÈRES QUALITÉ & CONTRÔLE RÉCEPTION (Guide 2026 p. 22) ---
    val humiditeMesureePercent: Double, // Relevé de l'humidimètre à la réception
    val impuretesPercent: Double, // Teneur en impuretés / tiges (ex: max 5% girofle, 2% riz)
    val grainsDefectueuxPercent: Double = 0.0, // Grains cassés, piquants ou brisés
    val decisionControle: String, // "CONFORME", "DECOTE", "QUARANTAINE", "REFUSE" (Guide p. 22)
    val identifiantContreEchantillonScelle: String, // ex: "ECH-GIR-260915-03-SC" (Preuve scellée en cas de litige)
    
    // --- CONSERVATION & ZONAGE (Guide 2026 Ch. 8, p. 43) ---
    val zoneStockageEntrepot: String, // "ZONE_A" (Céréales), "ZONE_B" (Épices), "ZONE_C" (Légumineuses), "ZONE_Q" (Quarantaine)
    val coutUnitaireAchatAr: Double, // Prix d'achat rendu entrepôt par kg
    val dateLimiteConservation: String, // Date limite optimale avant dégradation (gestion FIFO/FEFO)
    val statutConservation: String = "OPTIMAL", // "OPTIMAL", "VIGILANCE_HUMIDITE", "QUARANTAINE", "A_DESTOCKER_FEFO"
    val temperatureMesureeC: Double = 23.0, // Température mesurée au cœur de la pile
    val estBloqueQuarantaine: Boolean = false, // Bloqué en cas de doute sanitaire ou litige
    val codeQrTracabilite: String = "", // Identifiant numérique QR Code pour scan caméra
    val observationsControle: String = "" // Remarques du responsable qualité / magasinier
)
