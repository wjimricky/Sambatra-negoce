package com.example.data.local

import com.example.data.model.*

object DemoDataProvider {

    // Profil de l'entreprise Sambatra Négoce (Antananarivo, Anosibe / Périphérie)
    const val COMPANY_NAME = "Sambatra Négoce SARL"
    const val WAREHOUSE_AREA_M2 = 400
    const val STARTUP_CAPITAL_AR = 100_000_000.0 // 60M apport + 40M crédit 4 ans à 15%
    const val MONTHLY_FIXED_CHARGES_AR = 5_000_000.0 // 60M Ar/an en année 1
    const val TREASURY_TARGET_SAFETY_AR = 10_000_000.0 // 2 mois de charges fixes = 10M Ar
    var currentTreasuryAr: Double = 9_000_000.0 // 1.8 mois -> Déclenche Alerte Lundi Matin!

    // Les 8 produits phares selon le modèle Sambatra Négoce (70% Grains, 30% Épices)
    val initialProducts = listOf(
        Product(
            id = "prod_riz",
            code = "RIZ",
            name = "Riz blanc usiné (Makalioka / Tsipala)",
            nameMg = "Vary fotsy madio (Makalioka)",
            type = ProductType.GRAIN,
            abcClass = AbcClass.A, // 30% du CA
            currentStockKg = 18_000.0,
            cmupCostAr = 2_080.0,
            wholesalePriceAr = 2_300.0,
            targetCoverageDays = 30,
            dailyConsumptionKg = 600.0,
            maxMoisturePercent = 14.0,
            minOrderKg = 1_000.0,
            packagingFormat = "Sacs 25 kg et 50 kg",
            warehouseZone = "Zone Grains - Allée A (Palettes)",
            specificRisks = "Charançons, humidité, concurrence riz importé bon marché"
        ),
        Product(
            id = "prod_haricot",
            code = "HAR",
            name = "Haricot sec rouge / blanc (Tsipoy)",
            nameMg = "Tsaramaso maina mena / fotsy",
            type = ProductType.GRAIN,
            abcClass = AbcClass.A, // 18% du CA
            currentStockKg = 9_500.0,
            cmupCostAr = 3_151.0,
            wholesalePriceAr = 3_664.0,
            targetCoverageDays = 30,
            dailyConsumptionKg = 320.0,
            maxMoisturePercent = 13.5,
            minOrderKg = 500.0,
            packagingFormat = "Sacs polypropylène 50 kg",
            warehouseZone = "Zone Grains - Allée B (Palettes)",
            specificRisks = "Bruches très agressives après 60 jours, durcissement au stockage"
        ),
        Product(
            id = "prod_mais",
            code = "MAI",
            name = "Maïs grain sec (Alimentation / Élevage)",
            nameMg = "Katsaka maina",
            type = ProductType.GRAIN,
            abcClass = AbcClass.B, // 12% du CA
            currentStockKg = 25_500.0, // Alerte couverture: 51 jours au lieu de 35j!
            cmupCostAr = 1_380.0,
            wholesalePriceAr = 1_550.0,
            targetCoverageDays = 35,
            dailyConsumptionKg = 500.0,
            maxMoisturePercent = 13.0,
            minOrderKg = 1_000.0,
            packagingFormat = "Sacs 50 kg ou vrac",
            warehouseZone = "Zone Grains - Allée C",
            specificRisks = "Moisissures et aflatoxines si humidité >14%, séparation humaine/animale impérative"
        ),
        Product(
            id = "prod_pois_cap",
            code = "PDC",
            name = "Pois du Cap (Kabaro)",
            nameMg = "Kabaro maina",
            type = ProductType.GRAIN,
            abcClass = AbcClass.B, // 10% du CA
            currentStockKg = 4_200.0,
            cmupCostAr = 3_440.0,
            wholesalePriceAr = 4_050.0,
            targetCoverageDays = 30,
            dailyConsumptionKg = 150.0,
            maxMoisturePercent = 13.0,
            minOrderKg = 500.0,
            packagingFormat = "Sacs 50 kg",
            warehouseZone = "Zone Grains - Allée D",
            specificRisks = "Saison des pluies coupe les pistes d'accès; variabilité calibre entre lots"
        ),
        Product(
            id = "prod_girofle",
            code = "GIR",
            name = "Clous de Girofle (Madagascar Grade 1)",
            nameMg = "Jirofo Madagasikara",
            type = ProductType.SPICE,
            abcClass = AbcClass.A, // 12% du CA, 22% marge!
            currentStockKg = 1_850.0,
            cmupCostAr = 28_000.0,
            wholesalePriceAr = 35_900.0,
            targetCoverageDays = 45,
            dailyConsumptionKg = 40.0,
            maxMoisturePercent = 12.0,
            minOrderKg = 50.0,
            packagingFormat = "Sacs doublés 10 kg / 25 kg",
            warehouseZone = "Zone Épices Sécurisée (Fermée à clé)",
            specificRisks = "Falsification (excès de tiges >5%, clous épuisés), vol (haute valeur), odeurs"
        ),
        Product(
            id = "prod_poivre",
            code = "POI",
            name = "Poivre noir & sauvage (Voatsiperifery)",
            nameMg = "Dipoavatra mainty & Voatsiperifery",
            type = ProductType.SPICE,
            abcClass = AbcClass.B, // 8% du CA, 20% marge
            currentStockKg = 920.0,
            cmupCostAr = 18_000.0,
            wholesalePriceAr = 23_000.0,
            targetCoverageDays = 40,
            dailyConsumptionKg = 22.0,
            maxMoisturePercent = 12.0,
            minOrderKg = 25.0,
            packagingFormat = "Sacs doublés étanches 25 kg",
            warehouseZone = "Zone Épices Sécurisée",
            specificRisks = "Mélange d'origines faux sauvage, propreté microbiologique si séché au sol"
        ),
        Product(
            id = "prod_gingembre",
            code = "GIN",
            name = "Gingembre séché (Tranches & Entier)",
            nameMg = "Sakamalaho maina",
            type = ProductType.SPICE,
            abcClass = AbcClass.C, // 6% du CA
            currentStockKg = 1_400.0,
            cmupCostAr = 7_740.0,
            wholesalePriceAr = 9_000.0,
            targetCoverageDays = 30,
            dailyConsumptionKg = 45.0,
            maxMoisturePercent = 11.5,
            minOrderKg = 100.0,
            packagingFormat = "Sacs 25 kg & sachets 500g",
            warehouseZone = "Zone Épices - Rayonnage E",
            specificRisks = "Séchage insuffisant sur sol nu, fibres excessives, moisissures cachées"
        ),
        Product(
            id = "prod_curcuma",
            code = "CUR",
            name = "Curcuma séché (Tamotamo pur)",
            nameMg = "Tamotamo maina",
            type = ProductType.SPICE,
            abcClass = AbcClass.C, // 4% du CA
            currentStockKg = 850.0,
            cmupCostAr = 5_800.0,
            wholesalePriceAr = 7_200.0,
            targetCoverageDays = 30,
            dailyConsumptionKg = 28.0,
            maxMoisturePercent = 11.0,
            minOrderKg = 50.0,
            packagingFormat = "Fûts hermétiques & sachets 500g",
            warehouseZone = "Zone Épices - Rayonnage F (À l'abri de la lumière)",
            specificRisks = "Colorants artificiels ajoutés pour tricher sur la teinte, taches indélébiles"
        )
    )

    // Lots de traçabilité réels enregistrés avec le code normalisé [PROD]-[ORIG]-[AAMMJJ]-[N°]
    val initialLots = listOf(
        BatchLot(
            lotNumber = "GIR-ANA-260915-03",
            productId = "prod_girofle",
            productName = "Girofle Clous Grade 1",
            supplierName = "Coopérative Analanjirofo Espoir",
            originRegion = "Fénérive-Est / Analanjirofo",
            receptionDate = "15/09/2026",
            quantityKg = 2_000.0,
            netReceivedKg = 1_980.0,
            moisturePercent = 11.2,
            impuritiesPercent = 4.2, // Conforme < 5%
            bagsCount = 40,
            weighedBagsCount = 6, // 15% pesés (règle >= 10%)
            decision = QualityDecision.CONFORME,
            sealedCounterSampleId = "ECH-GIR-260915-03-SC",
            warehouseZone = "Zone Épices - Box Sécurisé 1",
            storageDays = 30,
            unitCostAr = 28_000.0,
            observations = "Têtes intactes >92%, arôme piquant puissant, scellé conforme."
        ),
        BatchLot(
            lotNumber = "MAI-VAK-261002-01",
            productId = "prod_mais",
            productName = "Maïs grain sec jaune",
            supplierName = "Collecteur Rasoanaivo Antsirabe",
            originRegion = "Vakinankaratra (Betafo)",
            receptionDate = "02/10/2026",
            quantityKg = 15_000.0,
            netReceivedKg = 14_850.0,
            moisturePercent = 12.8,
            impuritiesPercent = 1.9,
            bagsCount = 300,
            weighedBagsCount = 35,
            decision = QualityDecision.CONFORME,
            sealedCounterSampleId = "ECH-MAI-261002-01-SC",
            warehouseZone = "Zone Grains - Palettes 12 à 18",
            storageDays = 13,
            unitCostAr = 1_350.0,
            observations = "Grain sain, tri visuel propre, humidité contrôlée à l'humidimètre."
        ),
        BatchLot(
            lotNumber = "HAR-ALA-260920-02",
            productId = "prod_haricot",
            productName = "Haricot sec rouge Tsipoy",
            supplierName = "Groupement Paysan Ambatondrazaka",
            originRegion = "Alaotra-Mangoro",
            receptionDate = "20/09/2026",
            quantityKg = 8_000.0,
            netReceivedKg = 7_920.0,
            moisturePercent = 13.1,
            impuritiesPercent = 2.4,
            bagsCount = 160,
            weighedBagsCount = 20,
            decision = QualityDecision.CONFORME,
            sealedCounterSampleId = "ECH-HAR-260920-02-SC",
            warehouseZone = "Zone Grains - Palettes 04 à 08",
            storageDays = 25,
            unitCostAr = 2_800.0,
            observations = "Aucun signe de bruche détecté. Écartement 50 cm des murs respecté."
        ),
        BatchLot(
            lotNumber = "PDC-TUL-260810-01",
            productId = "prod_pois_cap",
            productName = "Pois du Cap blanc (Kabaro)",
            supplierName = "Collecteur Sud Négoce",
            originRegion = "Menabe / Sud-Ouest",
            receptionDate = "10/08/2026",
            quantityKg = 3_000.0,
            netReceivedKg = 2_940.0,
            moisturePercent = 14.8, // Trop humide à l'arrivée!
            impuritiesPercent = 3.5,
            bagsCount = 60,
            weighedBagsCount = 10,
            decision = QualityDecision.DECOTE,
            sealedCounterSampleId = "ECH-PDC-260810-01-SC",
            warehouseZone = "Zone Grains - Palettes 22",
            storageDays = 66,
            unitCostAr = 3_250.0,
            observations = "Décote de 6% négociée suite à humidité élevée. Séchage complémentaire effectué."
        ),
        BatchLot(
            lotNumber = "POI-VAT-261010-04",
            productId = "prod_poivre",
            productName = "Poivre sauvage Voatsiperifery",
            supplierName = "Collecteur Forêt Manakara",
            originRegion = "Vatovavy / Fitovinany",
            receptionDate = "10/10/2026",
            quantityKg = 350.0,
            netReceivedKg = 345.0,
            moisturePercent = 11.5,
            impuritiesPercent = 1.1,
            bagsCount = 14,
            weighedBagsCount = 14, // 100% pesé vu la valeur
            decision = QualityDecision.CONFORME,
            sealedCounterSampleId = "ECH-POI-261010-04-SC",
            warehouseZone = "Zone Épices - Armoire Blindée",
            storageDays = 5,
            unitCostAr = 18_000.0,
            observations = "Origine sauvage certifiée par carnet de récolte, saveur résineuse typique."
        )
    )

    // Clients de Sambatra Négoce (Règle des 20% font 80% du CA, max 20-25% par client)
    val initialClients = listOf(
        Client(
            id = "cli_restau_tsinjo",
            name = "Hôtel-Restaurant Le Tsinjo (Analakely)",
            segment = ClientSegment.A,
            contactName = "Chef Patrick Rakoto",
            phone = "+261 34 11 222 33",
            location = "Antananarivo Centre",
            monthlyVolumeKg = 1_800.0,
            monthlyCaAr = 6_500_000.0, // ~16% du CA (conforme <20-25%)
            creditLimitAr = 5_000_000.0,
            currentDebtAr = 4_200_000.0, // ALERTE: retard > 30 jours (cas lundi matin!)
            debtOver30DaysAr = 4_200_000.0,
            averagePaymentDays = 38,
            creditScore25 = 18, // Note 18/25: Plafond 15 jours
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_tsinjo_01",
                    invoiceNumber = "FAC-26-068",
                    dateFacture = "12/07/2026",
                    montantAr = 3_800_000.0,
                    delaiAccordeJours = 15,
                    delaiReelJours = 18,
                    datePaiement = "30/07/2026",
                    statut = PaymentStatus.PAYE_EN_RETARD,
                    commentaire = "Retard de 3 jours, régularisé par chèque."
                ),
                PaymentHistoryEntry(
                    id = "pay_tsinjo_02",
                    invoiceNumber = "FAC-26-085",
                    dateFacture = "10/08/2026",
                    montantAr = 4_500_000.0,
                    delaiAccordeJours = 15,
                    delaiReelJours = 25,
                    datePaiement = "04/09/2026",
                    statut = PaymentStatus.PAYE_EN_RETARD,
                    commentaire = "Retard de 10 jours suite à baisse de fréquentation."
                ),
                PaymentHistoryEntry(
                    id = "pay_tsinjo_03",
                    invoiceNumber = "FAC-26-104",
                    dateFacture = "02/09/2026",
                    montantAr = 4_200_000.0,
                    delaiAccordeJours = 15,
                    delaiReelJours = 44,
                    datePaiement = null,
                    statut = PaymentStatus.EN_RETARD,
                    commentaire = "ÉCHU DEPUIS 38 JOURS ! Livraison bloquée conformément à la règle p. 44."
                )
            )
        ),
        Client(
            id = "cli_grossiste_anosibe",
            name = "Établissements Razafy & Fils (Marché Anosibe)",
            segment = ClientSegment.A,
            contactName = "M. Dera Razafy",
            phone = "+261 32 44 555 66",
            location = "Marché Anosibe, Tana",
            monthlyVolumeKg = 4_500.0,
            monthlyCaAr = 8_200_000.0, // 20% du CA
            creditLimitAr = 8_000_000.0,
            currentDebtAr = 3_100_000.0,
            debtOver30DaysAr = 0.0,
            averagePaymentDays = 14,
            creditScore25 = 23, // Note 23/25: Plafond 30 jours
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_razafy_01",
                    invoiceNumber = "FAC-26-079",
                    dateFacture = "01/08/2026",
                    montantAr = 5_400_000.0,
                    delaiAccordeJours = 30,
                    delaiReelJours = 12,
                    datePaiement = "13/08/2026",
                    statut = PaymentStatus.PAYE_A_TEMPS,
                    commentaire = "Règlement anticipé par virement bancaire."
                ),
                PaymentHistoryEntry(
                    id = "pay_razafy_02",
                    invoiceNumber = "FAC-26-098",
                    dateFacture = "28/08/2026",
                    montantAr = 6_200_000.0,
                    delaiAccordeJours = 30,
                    delaiReelJours = 15,
                    datePaiement = "12/09/2026",
                    statut = PaymentStatus.PAYE_A_TEMPS,
                    commentaire = "Paiement ponctuel exemplaire."
                ),
                PaymentHistoryEntry(
                    id = "pay_razafy_03",
                    invoiceNumber = "FAC-26-118",
                    dateFacture = "05/10/2026",
                    montantAr = 3_100_000.0,
                    delaiAccordeJours = 30,
                    delaiReelJours = 11,
                    datePaiement = null,
                    statut = PaymentStatus.EN_COURS,
                    commentaire = "Facture courante en cours, échéance au 05/11/2026."
                )
            )
        ),
        Client(
            id = "cli_cantine_saint_michel",
            name = "Groupement Cantines Scolaires Saint-Michel",
            segment = ClientSegment.B,
            contactName = "Sœur Marie-Claire",
            phone = "+261 33 77 888 99",
            location = "Amparibe, Antananarivo",
            monthlyVolumeKg = 2_200.0,
            monthlyCaAr = 4_600_000.0,
            creditLimitAr = 3_000_000.0,
            currentDebtAr = 1_800_000.0,
            debtOver30DaysAr = 0.0,
            averagePaymentDays = 15,
            creditScore25 = 19,
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_cantine_01",
                    invoiceNumber = "FAC-26-092",
                    dateFacture = "15/08/2026",
                    montantAr = 2_500_000.0,
                    delaiAccordeJours = 15,
                    delaiReelJours = 14,
                    datePaiement = "29/08/2026",
                    statut = PaymentStatus.PAYE_A_TEMPS,
                    commentaire = "Paiement chèque régulier."
                ),
                PaymentHistoryEntry(
                    id = "pay_cantine_02",
                    invoiceNumber = "FAC-26-114",
                    dateFacture = "01/10/2026",
                    montantAr = 1_800_000.0,
                    delaiAccordeJours = 15,
                    delaiReelJours = 15,
                    datePaiement = null,
                    statut = PaymentStatus.EN_COURS,
                    commentaire = "Échéance au 16/10/2026."
                )
            )
        ),
        Client(
            id = "cli_detaillant_isotry",
            name = "Coopérative Détaillants Isotry (12 boutiques)",
            segment = ClientSegment.B,
            contactName = "Mme Bakoly Raharisoa",
            phone = "+261 34 55 666 77",
            location = "Marché Isotry",
            monthlyVolumeKg = 3_000.0,
            monthlyCaAr = 5_900_000.0,
            creditLimitAr = 2_500_000.0,
            currentDebtAr = 1_200_000.0,
            debtOver30DaysAr = 0.0,
            averagePaymentDays = 7,
            creditScore25 = 20,
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_isotry_01",
                    invoiceNumber = "FAC-26-116",
                    dateFacture = "08/10/2026",
                    montantAr = 1_200_000.0,
                    delaiAccordeJours = 7,
                    delaiReelJours = 8,
                    datePaiement = null,
                    statut = PaymentStatus.EN_COURS,
                    commentaire = "Rotation hebdomadaire courte respectée."
                )
            )
        ),
        Client(
            id = "cli_transformateur_epices",
            name = "MadaSpices Lab (Condiments & Sauces)",
            segment = ClientSegment.B,
            contactName = "Dr. Hery Andriam",
            phone = "+261 32 01 234 56",
            location = "Zone Forello Tanjombato",
            monthlyVolumeKg = 600.0,
            monthlyCaAr = 4_200_000.0,
            creditLimitAr = 3_000_000.0,
            currentDebtAr = 900_000.0,
            debtOver30DaysAr = 0.0,
            averagePaymentDays = 20,
            creditScore25 = 21,
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_madaspices_01",
                    invoiceNumber = "FAC-26-110",
                    dateFacture = "25/09/2026",
                    montantAr = 900_000.0,
                    delaiAccordeJours = 20,
                    delaiReelJours = 21,
                    datePaiement = null,
                    statut = PaymentStatus.EN_COURS,
                    commentaire = "Règlement en attente de visa comptable."
                )
            )
        ),
        Client(
            id = "cli_snack_ambohimangakely",
            name = "Gargotes Réunies Ambohimangakely",
            segment = ClientSegment.C,
            contactName = "M. Justin",
            phone = "+261 34 99 888 11",
            location = "Ambohimangakely",
            monthlyVolumeKg = 500.0,
            monthlyCaAr = 1_100_000.0,
            creditLimitAr = 0.0, // Comptant uniquement
            currentDebtAr = 0.0,
            debtOver30DaysAr = 0.0,
            averagePaymentDays = 0,
            creditScore25 = 11, // < 14: Paiement comptant
            paymentHistory = listOf(
                PaymentHistoryEntry(
                    id = "pay_justin_01",
                    invoiceNumber = "FAC-26-120",
                    dateFacture = "12/10/2026",
                    montantAr = 450_000.0,
                    delaiAccordeJours = 0,
                    delaiReelJours = 0,
                    datePaiement = "12/10/2026",
                    statut = PaymentStatus.PAYE_A_TEMPS,
                    commentaire = "Paiement en espèces au comptoir (règle segment C)."
                )
            )
        )
    )

    // Fournisseurs (Règle d'or: toujours au moins 2 sources qualifiées par produit!)
    val initialSuppliers = listOf(
        Supplier(
            id = "sup_coop_analanjirofo",
            name = "Coopérative Analanjirofo Espoir",
            region = "Fénérive-Est / Sainte-Marie",
            mainProducts = "Girofle clous & griffes, Poivre noir",
            reliabilityScore5 = 4.4, // Grille p. 17
            complianceRatePercent = 95.0,
            currentAdvancesAr = 4_000_000.0, // Tracée et couverte
            phone = "+261 32 55 123 45",
            isPrimarySource = true
        ),
        Supplier(
            id = "sup_collecteur_manakara",
            name = "Sourcing Épices Sud-Est (Manakara)",
            region = "Vatovavy / Fitovinany",
            mainProducts = "Poivre sauvage Voatsiperifery, Gingembre, Curcuma",
            reliabilityScore5 = 4.1,
            complianceRatePercent = 92.0,
            currentAdvancesAr = 2_000_000.0,
            phone = "+261 33 44 987 65",
            isPrimarySource = true
        ),
        Supplier(
            id = "sup_groupement_ambatondrazaka",
            name = "Union Rizière d'Alaotra (Ambatondrazaka)",
            region = "Alaotra-Mangoro",
            mainProducts = "Paddy, Riz blanc Tsipala & Makalioka, Haricot",
            reliabilityScore5 = 4.3,
            complianceRatePercent = 93.5,
            currentAdvancesAr = 0.0,
            phone = "+261 34 22 345 67",
            isPrimarySource = true
        ),
        Supplier(
            id = "sup_collecteur_antsirabe",
            name = "Collecte Céréales Vakinankaratra",
            region = "Antsirabe / Betafo",
            mainProducts = "Maïs grain, Haricot blanc & rouge",
            reliabilityScore5 = 3.8,
            complianceRatePercent = 88.0,
            currentAdvancesAr = 1_500_000.0,
            phone = "+261 32 11 876 54",
            isPrimarySource = false // Source secondaire de dépannage
        ),
        Supplier(
            id = "sup_menabe_kabaro",
            name = "GIE Légumineuses Menabe (Miandrivazo)",
            region = "Menabe",
            mainProducts = "Pois du Cap (Kabaro), Arachides",
            reliabilityScore5 = 3.9,
            complianceRatePercent = 89.0,
            currentAdvancesAr = 0.0,
            phone = "+261 33 99 765 43",
            isPrimarySource = true
        )
    )

    // Les 15 indicateurs du Tableau de Bord selon le Chapitre 12.6
    fun getDashboardKpis(): List<DashboardKpi> {
        return listOf(
            DashboardKpi(
                id = "kpi_ca_mensuel",
                domain = "Ventes",
                title = "Chiffre d'Affaires Mensuel",
                valueDisplay = "41 200 000 Ar",
                targetDisplay = "40 000 000 Ar",
                alertThreshold = "-15% vs moyenne",
                status = KpiStatus.GREEN,
                explanation = "Volume mensuel conforme au modèle Année 1 de Sambatra Négoce."
            ),
            DashboardKpi(
                id = "kpi_concentration_clients",
                domain = "Ventes",
                title = "Concentration Top 5 Clients",
                valueDisplay = "46.5 %",
                targetDisplay = "< 50.0 %",
                alertThreshold = "> 50 %",
                status = KpiStatus.GREEN,
                explanation = "Conforme. Aucun client unique ne dépasse 20 à 25% du CA."
            ),
            DashboardKpi(
                id = "kpi_marge_variable",
                domain = "Marge",
                title = "Marge sur Coût Variable",
                valueDisplay = "14.2 %",
                targetDisplay = "14.0 %",
                alertThreshold = "< 12.0 % (-2 pts)",
                status = KpiStatus.GREEN,
                explanation = "Mix 70% grains / 30% épices apporte la marge cible prévue (p. 80-81)."
            ),
            DashboardKpi(
                id = "kpi_ecart_prix_revient",
                domain = "Marge",
                title = "Écart Prix Revient Réel vs Prévu",
                valueDisplay = "+ 1.8 %",
                targetDisplay = "< 2.0 %",
                alertThreshold = "> 3.0 %",
                status = KpiStatus.GREEN,
                explanation = "Impact transport et manutention maîtrisé sur les derniers lots reçus."
            ),
            DashboardKpi(
                id = "kpi_couverture_stock",
                domain = "Stock",
                title = "Couverture Stock Maïs",
                valueDisplay = "51 Jours",
                targetDisplay = "35 Jours",
                alertThreshold = "> 50 Jours",
                status = KpiStatus.RED, // Voyant ROUGE du cas lundi matin
                explanation = "Alerte : Sur-stock de maïs (51 jours). Risque d'asphyxie trésorerie et moisissure !"
            ),
            DashboardKpi(
                id = "kpi_taux_rupture",
                domain = "Stock",
                title = "Taux de Rupture Produits",
                valueDisplay = "1.2 %",
                targetDisplay = "< 3.0 %",
                alertThreshold = "> 3.0 %",
                status = KpiStatus.GREEN,
                explanation = "Très bonne disponibilité générale sur les 8 références phares."
            ),
            DashboardKpi(
                id = "kpi_taux_perte",
                domain = "Stock",
                title = "Taux de Perte & Freinte",
                valueDisplay = "0.95 %",
                targetDisplay = "< 1.20 %",
                alertThreshold = "> 1.5x freinte normale",
                status = KpiStatus.GREEN,
                explanation = "Entrepôt 400 m² sur palettes bien aéré, palettes espacées de 50 cm des murs."
            ),
            DashboardKpi(
                id = "kpi_conformite_reception",
                domain = "Qualité",
                title = "Lots Conformes à Réception",
                valueDisplay = "94.0 %",
                targetDisplay = "> 90.0 %",
                alertThreshold = "< 90.0 %",
                status = KpiStatus.GREEN,
                explanation = "Application rigoureuse du protocole de réception en 5 étapes avec humidimètre."
            ),
            DashboardKpi(
                id = "kpi_reclamations_clients",
                domain = "Qualité",
                title = "Taux de Réclamations Clients",
                valueDisplay = "0.8 %",
                targetDisplay = "< 2.0 %",
                alertThreshold = "> 2.0 %",
                status = KpiStatus.GREEN,
                explanation = "Seulement 1 réclamation mineure sur 120 livraisons ce mois."
            ),
            DashboardKpi(
                id = "kpi_dso_paiement",
                domain = "Crédit",
                title = "Délai Moyen Paiement Clients (DSO)",
                valueDisplay = "22 Jours",
                targetDisplay = "20 Jours",
                alertThreshold = "> 27 Jours",
                status = KpiStatus.AMBER,
                explanation = "Légère dérive due aux retards de paiement du Restaurant Le Tsinjo."
            ),
            DashboardKpi(
                id = "kpi_creances_plus_30j",
                domain = "Crédit",
                title = "Créances de Plus de 30 Jours",
                valueDisplay = "4 200 000 Ar (19.4%)",
                targetDisplay = "< 15.0 %",
                alertThreshold = "> 15.0 %",
                status = KpiStatus.RED, // Voyant ROUGE du cas lundi matin
                explanation = "Alerte : Le client Restaurant Le Tsinjo doit 4.2M Ar au-delà de 30 jours !"
            ),
            DashboardKpi(
                id = "kpi_tresorerie_mois_charges",
                domain = "Trésorerie",
                title = "Mois de Charges Fixes Couverts",
                valueDisplay = "1.8 Mois (9.0M Ar)",
                targetDisplay = ">= 2.0 Mois (10.0M Ar)",
                alertThreshold = "< 2.0 Mois",
                status = KpiStatus.RED, // Voyant ROUGE du cas lundi matin
                explanation = "Alerte : Trésorerie sous le seuil de sécurité des 2 mois de charges fixes !"
            ),
            DashboardKpi(
                id = "kpi_bfr_jours_ca",
                domain = "Trésorerie",
                title = "BFR en Jours de CA",
                valueDisplay = "48 Jours",
                targetDisplay = "< 50 Jours",
                alertThreshold = "Hausse > 10 jours",
                status = KpiStatus.AMBER,
                explanation = "Stock + créances - dettes = 66M Ar. Doit être surveillé de près."
            ),
            DashboardKpi(
                id = "kpi_livraisons_a_lheure",
                domain = "Logistique",
                title = "Livraisons à l'Heure & Complètes",
                valueDisplay = "96.5 %",
                targetDisplay = ">= 95.0 %",
                alertThreshold = "< 95.0 %",
                status = KpiStatus.GREEN,
                explanation = "Tournées d'Antananarivo préparées la veille avec bons signés."
            ),
            DashboardKpi(
                id = "kpi_accidents_securite",
                domain = "Équipe",
                title = "Accidents & Presque-Accidents",
                valueDisplay = "0 déclaré",
                targetDisplay = "0",
                alertThreshold = "Tout incident",
                status = KpiStatus.GREEN,
                explanation = "EPI portés (gants, chaussures sécurité, masques poussière maïs)."
            )
        )
    }

    // Les 10 études de cas détaillées du Chapitre 15
    val caseStudies = listOf(
        CaseStudy(
            caseNumber = 1,
            title = "Riz : Le rendement de décorticage décide de la marge",
            product = "Riz / Paddy",
            problem = "Offre de 40 tonnes de paddy à 1 300 Ar/kg. Riz blanc vendu 2 300 Ar/kg. Est-ce rentable ?",
            numbers = "Rendement 60% = Marge 2,8% | Rendement 65% = Marge 10,3% | Rendement 68% = Marge 14,3%",
            wrongAction = "Acheter le lot sans tester la décortiqueuse en se fiant au rendement théorique de 68%.",
            whatShouldBeDone = "Demander un essai de décorticage sur 1 tonne avant de s'engager. Négocier le paddy à 1 250 Ar/kg.",
            keyLesson = "Trois points de rendement valent à eux seuls plus de 100 Ar/kg ! Mesurer avant d'acheter.",
            chapter = "Chapitre 15 - Cas 1 (p. 88)"
        ),
        CaseStudy(
            caseNumber = 2,
            title = "Maïs : Le même grain, deux usages, deux exigences",
            product = "Maïs sec",
            problem = "Achat de 20 t de maïs à 1 350 Ar/kg pour transformateur alimentaire. Humidité non mesurée. Rejeté pour moisissures.",
            numbers = "Perte nette sèche de 6 580 000 Ar après revente déclassée à un éleveur à 1 100 Ar/kg.",
            wrongAction = "Mesurer l'humidité sur un seul sac superficiel et mélanger les usages.",
            whatShouldBeDone = "Échantillonner au moins 10 sacs avec humidimètre. Échantillon scellé validé par écrit par l'acheteur.",
            keyLesson = "Le coût d'un humidimètre et d'une heure de contrôle est dérisoire face à 6,6 millions Ar de perte !",
            chapter = "Chapitre 15 - Cas 2 (p. 89)"
        ),
        CaseStudy(
            caseNumber = 3,
            title = "Haricot sec : Le stock « qui montera forcément »",
            product = "Haricot Tsipoy",
            problem = "Achat de 30 tonnes à 2 800 Ar/kg en pariant sur la soudure. Faut-il garder 5 mois ou vendre à 2 mois ?",
            numbers = "Garder 5 mois à 3 300 Ar : Perte -1 140 000 Ar (pertes 6%, traitement, crédit). Vendre à 2 mois : Bénéfice +1 765 000 Ar.",
            wrongAction = "Immobiliser 84M Ar en espérant une hausse de prix qui est mangée par les bruches et le coût financier.",
            whatShouldBeDone = "Calculer le prix de revient à la sortie de stock (coût stockage = 2,5 à 3% par mois). Fixer date de sortie ferme.",
            keyLesson = "La marge se protège au stockage. Vendre vite à marge modérée rapporte plus que spéculer.",
            chapter = "Chapitre 15 - Cas 3 (p. 90)"
        ),
        CaseStudy(
            caseNumber = 4,
            title = "Pois du Cap : Le fournisseur le moins cher n'est pas le moins coûteux",
            product = "Pois du Cap",
            problem = "Fournisseur X éloigné (2 900 Ar + 450 transport) vs Fournisseur Y proche (3 200 Ar + 150 transport).",
            numbers = "Prix rendu affiché identique (3 350 Ar). Coût réel X = 3 535 Ar/kg (pertes 2.5%, crédit 60j). Coût réel Y = 3 440 Ar/kg.",
            wrongAction = "Choisir X pour le prix facial de 2 900 Ar, sans compter les risques de coupures de routes en saison des pluies.",
            whatShouldBeDone = "Choisir Y en source principale (70%), garder X en complément en saison sèche (30%).",
            keyLesson = "Toujours comparer les fournisseurs sur le coût rendu complet et avoir 2 sources par produit.",
            chapter = "Chapitre 15 - Cas 4 (p. 90-91)"
        ),
        CaseStudy(
            caseNumber = 5,
            title = "Arachide : Un test à 150 000 Ar contre une perte de 57 millions",
            product = "Arachides décortiquées",
            problem = "Demande de 15 tonnes d'arachides avec analyse toxines. Deux fournisseurs disponibles à 3 800 Ar/kg.",
            numbers = "Coût test labo : 150 000 Ar. Risque de rejet sans test : 57 000 000 Ar d'achat détruits.",
            wrongAction = "Acheter sans analyse de laboratoire préalable pour économiser 150 000 Ar.",
            whatShouldBeDone = "Faire analyser un échantillon avant tout paiement. Le lot 2 était contaminé aux aflatoxines et refusé net.",
            keyLesson = "Un produit non conforme aux normes sanitaires ne se vend pas au rabais : c'est un poison interdit.",
            chapter = "Chapitre 15 - Cas 5 (p. 91)"
        ),
        CaseStudy(
            caseNumber = 6,
            title = "Girofle : Payer le girofle, recevoir des tiges",
            product = "Clous de Girofle",
            problem = "Achat de 2 000 kg à 28 000 Ar/kg (56M Ar). Spécification max 5% tiges. Le lot reçu en contient 11%.",
            numbers = "120 kg d'excès de tiges payés au prix du girofle = 3 360 000 Ar de perte immédiate !",
            wrongAction = "Accepter le lot parce que le collecteur prétend que 'c'est comme ça partout'.",
            whatShouldBeDone = "Refuser ou appliquer une décote écrite stricte. Payer uniquement sur le poids net de clous après tamisage.",
            keyLesson = "Sur une épice de valeur, 1% d'impureté représente des centaines de milliers d'ariary par tonne.",
            chapter = "Chapitre 15 - Cas 6 (p. 92)"
        ),
        CaseStudy(
            caseNumber = 7,
            title = "Poivre sauvage : La promesse d'origine qui ne se vérifie pas",
            product = "Poivre Voatsiperifery",
            problem = "Vente d'un poivre présenté comme 'sauvage' à 24 000 Ar/kg. Un restaurateur soupçonne un mélange de cultivé.",
            numbers = "Perte directe : 960 000 Ar (retour 120 kg). Perte client fidèle : 8 400 000 Ar de marge annuelle !",
            wrongAction = "Faire confiance verbale à un collecteur sans exiger de traçabilité par lot ni visiter les cueilleurs.",
            whatShouldBeDone = "Contrat avec déclaration d'origine par lot, droit d'audit, échantillon scellé, contre-visite.",
            keyLesson = "Une prime d'origine se paie si elle se prouve. La confiance d'un client vaut plus que la marge d'un lot.",
            chapter = "Chapitre 15 - Cas 7 (p. 92-93)"
        ),
        CaseStudy(
            caseNumber = 8,
            title = "Gingembre & Curcuma : Conditionner pour mieux vendre",
            product = "Gingembre & Curcuma",
            problem = "Vente en vrac à 9 000 Ar/kg (marge 14%). Les clients demandent des sachets de 500g.",
            numbers = "Sachets 500g vendus à 11 000 Ar/kg = Marge de 25,3% (+4,57M Ar/mois sur 3 tonnes). Matériel rentabilisé en 1 mois.",
            wrongAction = "Rester en vrac par habitude et laisser la valeur ajoutée aux intermédiaires.",
            whatShouldBeDone = "Investir dans une soudeuse et balance de précision (2,5M Ar) après avoir testé 5 clients.",
            keyLesson = "Le conditionnement et le calibrage sont le premier levier d'augmentation durable de marge.",
            chapter = "Chapitre 15 - Cas 8 (p. 93)"
        ),
        CaseStudy(
            caseNumber = 9,
            title = "Cannelle : La casse est une perte de valeur directe",
            product = "Cannelle bâtons",
            problem = "1 000 kg achetés à 14 000 Ar/kg. Transport en sacs souples empilés = 8% de casse vendue à 8 000 Ar.",
            numbers = "Perte casse emballage standard : 480 000 Ar. Avec emballage renforcé carton (+150 Ar/kg) : 270 000 Ar.",
            wrongAction = "Économiser 150 Ar/kg d'emballage pour perdre 480 000 Ar par tonne en bâtons brisés.",
            whatShouldBeDone = "Utiliser des cartons rigides pour produits fragiles. L'emballage renforcé se paie lui-même.",
            keyLesson = "Suivre la casse comme un indicateur clé à l'arrivée et à l'expédition.",
            chapter = "Chapitre 15 - Cas 9 (p. 94)"
        ),
        CaseStudy(
            caseNumber = 10,
            title = "Vanille : La prudence absolue du capital (Règle des 10%)",
            product = "Vanille préparée",
            problem = "Proposition de 100 kg de vanille à 200 000 Ar/kg (20M Ar). Marché en surstock et cours instables.",
            numbers = "Si baisse de 40% : Perte de 8M Ar (-14% des fonds propres). Si baisse 60% : -12M Ar et cash bloqué.",
            wrongAction = "Engager 35% de ses fonds propres dans un produit spéculatif sans acheteur ferme.",
            whatShouldBeDone = "Appliquer la règle des 10% des fonds propres max (lot limité à 25 kg = 5M Ar) avec contrat de vente signé d'avance.",
            keyLesson = "Refuser une opportunité est un acte de gestion courageux, pas un manque d'ambition.",
            chapter = "Chapitre 15 - Cas 10 (p. 94-95)"
        )
    )

    // Checklists interactives du Chapitre 12
    val checklistPurchase = listOf(
        ChecklistItem("cp1", "Achat", "Le besoin est confirmé par des ventes ou des contrats (pas seulement opportunité de prix)"),
        ChecklistItem("cp2", "Achat", "Le prix de revient complet rendu entrepôt est calculé et la marge cible est atteignable"),
        ChecklistItem("cp3", "Achat", "Le fournisseur est qualifié, commande test réussie ou bon historique"),
        ChecklistItem("cp4", "Achat", "L'échantillon est validé et conservé ; spécifications écrites dans la commande"),
        ChecklistItem("cp5", "Achat", "Conditions de paiement, transport et réclamation convenues par écrit"),
        ChecklistItem("cp6", "Achat", "La trésorerie disponible permet l'achat sans passer sous le seuil de sécurité (2 mois)"),
        ChecklistItem("cp7", "Achat", "Il reste de la place dans l'entrepôt, dans la zone adaptée"),
        ChecklistItem("cp8", "Achat", "La couverture de stock après achat ne dépasse pas la cible"),
        ChecklistItem("cp9", "Achat", "Un deuxième avis (direction ou gérant) a validé l'achat au-delà du seuil")
    )

    val checklistReception = listOf(
        ChecklistItem("cr1", "Réception", "Bon de livraison et commande concordent (produit, quantité, origine)"),
        ChecklistItem("cr2", "Réception", "Véhicule et sacs en bon état, sans humidité ni odeur anormale"),
        ChecklistItem("cr3", "Réception", "Pesée contradictoire d'au moins 10% des sacs, puis du poids total"),
        ChecklistItem("cr4", "Réception", "Échantillons prélevés dans plusieurs sacs, mesures d'humidité et impuretés"),
        ChecklistItem("cr5", "Réception", "Examen visuel & olfactif : insectes, moisissures, brisures, calibre, odeur"),
        ChecklistItem("cr6", "Réception", "Décision prise et signée : accepté, décote, quarantaine ou refusé"),
        ChecklistItem("cr7", "Réception", "Numéro de lot attribué, étiquettes posées, contre-échantillon scellé conservé"),
        ChecklistItem("cr8", "Réception", "Marchandise placée au bon emplacement (palettes, distance 50 cm des murs)"),
        ChecklistItem("cr9", "Réception", "Fiche de réception signée par magasinier et fournisseur, copie à l'administration")
    )

    val checklistShipping = listOf(
        ChecklistItem("ce1", "Expédition", "Commande confirmée par écrit ; plafond de crédit du client vérifié"),
        ChecklistItem("ce2", "Expédition", "Lot attribué selon rotation FIFO / FEFO ; bon de sortie rempli"),
        ChecklistItem("ce3", "Expédition", "Qualité conforme à la spécification, poids contrôlé, étiquetage correct"),
        ChecklistItem("ce4", "Expédition", "Véhicule propre, chargement protégé contre la pluie, ordre respecté"),
        ChecklistItem("ce5", "Expédition", "Bon de livraison signé par le client, avec réserves éventuelles notées"),
        ChecklistItem("ce6", "Expédition", "Facture émise sous 24 heures ; échéance enregistrée dans la balance âgée")
    )
}
