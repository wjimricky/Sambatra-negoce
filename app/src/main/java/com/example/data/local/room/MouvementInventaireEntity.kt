package com.example.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité Room pour les Mouvements d'Inventaire et le Registre des Pertes & Freintes.
 * Règle d'or p. 33 & 50 : Toute entrée/sortie doit être justifiée par un bon numéroté
 * et les pertes rigoureusement classées par cause selon les 5 catégories du Guide 2026 :
 * 1. Freinte naturelle de séchage / hygrométrie
 * 2. Écarts de tri et calibrage
 * 3. Casse et percement sacs en manutention
 * 4. Pertes biologiques (insectes, bruches, rongeurs)
 * 5. Écarts d'inventaire / pesée
 */
@Entity(
    tableName = "mouvements_inventaire",
    foreignKeys = [
        ForeignKey(
            entity = StockLotEntity::class,
            parentColumns = ["numeroLot"],
            childColumns = ["numeroLot"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["numeroLot"]),
        Index(value = ["dateMouvement"]),
        Index(value = ["typeMouvement"])
    ]
)
data class MouvementInventaireEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroLot: String, // Référence du lot traçable
    val produitId: String, // Référence du produit
    val typeMouvement: String, // "ENTREE_ACHAT", "SORTIE_VENTE", "TRANSFERT_ZONE", "PERTE_FREINTE_SECHAGE", "PERTE_ECART_TRI", "PERTE_CASSE_MANUTENTION", "PERTE_BIOLOGIQUE_NUISIBLE", "AJUSTEMENT_INVENTAIRE"
    val quantiteKg: Double, // Quantité du mouvement en kg
    val valeurUnitaireAr: Double = 0.0, // Valeur unitaire au kg (prix de revient ou prix de vente)
    val valeurTotaleAr: Double = 0.0, // Valeur financière totale du mouvement
    val dateMouvement: String, // Date et heure de l'opération (ex: "2026-10-17 08:30")
    val numeroPieceRef: String, // N° de Bon d'entrée (BE), Bon de sortie (BS), Facture ou Fiche d'inventaire
    val tiersConcerne: String, // Client, fournisseur, transporteur ou "Interne Magasin"
    val motifOuCause: String, // Justification ou cause décomposée de l'écart (Guide p. 34)
    val visaResponsable: String, // Signature / visa du responsable (contrôle interne et séparation des rôles)
    val distanceKmTransport: Double = 0.0, // Distance parcourue pour mouvements logistiques (km)
    val impactCarboneKgCo2: Double = 0.0 // Estimation d'émission CO2 du mouvement (Guide 2026 Durabilité)
)
