package com.example

import com.example.data.local.room.MouvementInventaireEntity
import com.example.data.local.room.ProduitEntity
import com.example.data.local.room.StockLotEntity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomConservationAndSecurityTest {

    @Test
    fun testProduitEntity_SafetyStockAndReorderPoint_Guide2026() {
        // Riz Makalioka : CMJ = 600 kg, Délai = 10 jours, Sécurité = 14 jours (8400 kg)
        // Point de commande = (600 * 10) + 8400 = 14400 kg
        val riz = ProduitEntity(
            id = "prod_riz",
            code = "RIZ",
            nom = "Riz blanc usiné (Makalioka)",
            nomMalagasy = "Vary fotsy madio",
            type = "GRAIN",
            classeAbc = "A",
            variete = "Makalioka",
            cmupCostAr = 2080.0,
            prixVenteGrosAr = 2300.0,
            consoJournaliereMoyenneKg = 600.0,
            stockSecuriteKg = 8400.0,
            pointDeCommandeKg = 14400.0,
            stockAlerteCritiqueKg = 4200.0,
            couvertureCibleJours = 35,
            delaiApprovisionnementJours = 10,
            seuilHumiditeMaxPercent = 14.0,
            humiditeCiblePercent = 12.5,
            temperatureStockageMaxC = 25.0,
            sensibiliteHygroscopique = "MOYENNE",
            compatibiliteOlfactive = true,
            dureeConservationMaxJours = 240,
            tauxFreinteTolerePercent = 0.50,
            formatConditionnement = "Sacs 50 kg",
            zoneEntrepot = "ZONE_A",
            risquesSpecifiques = "Charançons, humidité"
        )

        assertEquals(14400.0, riz.pointDeCommandeKg, 0.1)
        assertEquals(8400.0, riz.stockSecuriteKg, 0.1)
        assertEquals(14.0, riz.seuilHumiditeMaxPercent, 0.01)
        assertTrue(riz.compatibiliteOlfactive)
    }

    @Test
    fun testStockLotEntity_ExcessiveMoistureFlagsQuarantine() {
        // Pois du Cap reçu avec 14.2% d'humidité (Seuil max Guide 2026 = 13.0%)
        val lotPoisDuCap = StockLotEntity(
            numeroLot = "PDC-TUL-261005-01",
            produitId = "prod_pois_cap",
            fournisseurId = "sup_collecteur_antsirabe",
            nomFournisseur = "Collecte Céréales Vakinankaratra",
            regionOrigine = "Sud-Ouest",
            dateReception = "05/10/2026",
            quantiteInitialeKg = 3000.0,
            quantiteDisponibleKg = 3000.0,
            nombreSacs = 60,
            nombreSacsPeses = 8,
            humiditeMesureePercent = 14.2,
            impuretesPercent = 5.5,
            decisionControle = "QUARANTAINE",
            identifiantContreEchantillonScelle = "ECH-PDC-261005-01-SC",
            zoneStockageEntrepot = "ZONE_Q",
            coutUnitaireAchatAr = 3300.0,
            dateLimiteConservation = "05/02/2027",
            statutConservation = "QUARANTAINE",
            estBloqueQuarantaine = true,
            codeQrTracabilite = "QR-LOT-PDC-TUL-261005-01"
        )

        assertTrue("Un lot avec humidité non conforme doit être bloqué en quarantaine", lotPoisDuCap.estBloqueQuarantaine)
        assertEquals("ZONE_Q", lotPoisDuCap.zoneStockageEntrepot)
        assertEquals("QUARANTAINE", lotPoisDuCap.statutConservation)
    }

    @Test
    fun testStockLotEntity_SpiceVolatileIncompatibilityFlag() {
        // Poivre noir Voatsiperifery : épice volatile
        val poivre = ProduitEntity(
            id = "prod_poivre",
            code = "POI",
            nom = "Poivre noir Voatsiperifery",
            nomMalagasy = "Dipoavatra mainty",
            type = "SPICE",
            classeAbc = "B",
            variete = "Sauvage",
            cmupCostAr = 18000.0,
            prixVenteGrosAr = 23000.0,
            consoJournaliereMoyenneKg = 22.0,
            stockSecuriteKg = 300.0,
            pointDeCommandeKg = 650.0,
            seuilHumiditeMaxPercent = 12.0,
            sensibiliteHygroscopique = "ELEVEE",
            compatibiliteOlfactive = false, // Incompatible avec céréales neutres
            formatConditionnement = "Sacs étanches 25 kg",
            zoneEntrepot = "ZONE_B",
            risquesSpecifiques = "Pipérine volatile"
        )

        assertFalse("Les épices nobles à arômes volatils doivent avoir compatibiliteOlfactive = false", poivre.compatibiliteOlfactive)
        assertEquals("ZONE_B", poivre.zoneEntrepot)
        assertEquals("ELEVEE", poivre.sensibiliteHygroscopique)
    }

    @Test
    fun testMouvementInventaireEntity_LossDecomposition_Guide2026() {
        val perteFreinte = MouvementInventaireEntity(
            numeroLot = "RIZ-ALA-260920-01",
            produitId = "prod_riz",
            typeMouvement = "PERTE_FREINTE_SECHAGE",
            quantiteKg = 100.0,
            valeurUnitaireAr = 2050.0,
            valeurTotaleAr = 205000.0,
            dateMouvement = "2026-10-15 17:00",
            numeroPieceRef = "FP-2610-001",
            tiersConcerne = "Interne Magasin",
            motifOuCause = "Freinte naturelle de séchage (0.4% sur 25 jours)",
            visaResponsable = "Responsable Qualité"
        )

        assertEquals("PERTE_FREINTE_SECHAGE", perteFreinte.typeMouvement)
        assertEquals(205000.0, perteFreinte.valeurTotaleAr, 0.01)
        assertTrue(perteFreinte.typeMouvement.startsWith("PERTE_"))
    }
}
