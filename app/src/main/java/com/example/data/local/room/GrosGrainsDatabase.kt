package com.example.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de données locale Room pour GrosGrains Pro.
 * Conforme au Guide de Référence 2026 :
 * - Suivi hors-ligne des stocks de grains et d'épices
 * - Traçabilité des lots et déstockage FIFO/FEFO
 * - Enregistrement des mouvements d'inventaire et des pertes classifiées
 * - Surveillance des critères de conservation et seuils de réapprovisionnement
 */
@Database(
    entities = [
        ProduitEntity::class,
        FournisseurEntity::class,
        StockLotEntity::class,
        MouvementInventaireEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GrosGrainsDatabase : RoomDatabase() {

    abstract fun produitDao(): ProduitDao
    abstract fun fournisseurDao(): FournisseurDao
    abstract fun stockLotDao(): StockLotDao
    abstract fun mouvementInventaireDao(): MouvementInventaireDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: GrosGrainsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GrosGrainsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrosGrainsDatabase::class.java,
                    "grosgrains_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialSambatraNegoceData(database)
                    }
                }
            }
        }

        suspend fun populateInitialSambatraNegoceData(database: GrosGrainsDatabase) {
            val produitDao = database.produitDao()
            val fournisseurDao = database.fournisseurDao()
            val stockLotDao = database.stockLotDao()
            val mouvementDao = database.mouvementInventaireDao()

            // 1. Catalogue Produits Grains & Épices (Critères Guide 2026)
            val produits = listOf(
                ProduitEntity(
                    id = "prod_riz",
                    code = "RIZ",
                    nom = "Riz blanc usiné (Makalioka / Tsipala)",
                    nomMalagasy = "Vary fotsy madio (Makalioka)",
                    type = "GRAIN",
                    classeAbc = "A",
                    variete = "Makalioka / Tsipala",
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
                    formatConditionnement = "Sacs 25 kg et 50 kg",
                    zoneEntrepot = "ZONE_A",
                    risquesSpecifiques = "Charançons, condensation sous tôle, concurrence importations"
                ),
                ProduitEntity(
                    id = "prod_haricot",
                    code = "HAR",
                    nom = "Haricot sec rouge / blanc (Tsipoy)",
                    nomMalagasy = "Tsaramaso maina mena / fotsy",
                    type = "GRAIN",
                    classeAbc = "A",
                    variete = "Tsipoy / Lingot blanc",
                    cmupCostAr = 3151.0,
                    prixVenteGrosAr = 3664.0,
                    consoJournaliereMoyenneKg = 320.0,
                    stockSecuriteKg = 4800.0,
                    pointDeCommandeKg = 8000.0,
                    stockAlerteCritiqueKg = 2400.0,
                    couvertureCibleJours = 30,
                    delaiApprovisionnementJours = 10,
                    seuilHumiditeMaxPercent = 13.5,
                    humiditeCiblePercent = 12.0,
                    temperatureStockageMaxC = 23.0,
                    sensibiliteHygroscopique = "MOYENNE",
                    compatibiliteOlfactive = true,
                    dureeConservationMaxJours = 90,
                    tauxFreinteTolerePercent = 0.80,
                    formatConditionnement = "Sacs polypropylène 50 kg",
                    zoneEntrepot = "ZONE_C",
                    risquesSpecifiques = "Bruches très agressives après 60 jours, durcissement du tégument"
                ),
                ProduitEntity(
                    id = "prod_mais",
                    code = "MAI",
                    nom = "Maïs grain sec (Alimentation / Élevage)",
                    nomMalagasy = "Katsaka maina",
                    type = "GRAIN",
                    classeAbc = "B",
                    variete = "Jaune séché",
                    cmupCostAr = 1380.0,
                    prixVenteGrosAr = 1550.0,
                    consoJournaliereMoyenneKg = 500.0,
                    stockSecuriteKg = 6000.0,
                    pointDeCommandeKg = 11000.0,
                    stockAlerteCritiqueKg = 3000.0,
                    couvertureCibleJours = 25,
                    delaiApprovisionnementJours = 10,
                    seuilHumiditeMaxPercent = 13.0,
                    humiditeCiblePercent = 12.0,
                    temperatureStockageMaxC = 25.0,
                    sensibiliteHygroscopique = "MOYENNE",
                    compatibiliteOlfactive = true,
                    dureeConservationMaxJours = 180,
                    tauxFreinteTolerePercent = 0.70,
                    formatConditionnement = "Sacs 50 kg ou vrac",
                    zoneEntrepot = "ZONE_A",
                    risquesSpecifiques = "Moisissures et aflatoxines si humidité >13.5%, séparation usages"
                ),
                ProduitEntity(
                    id = "prod_pois_cap",
                    code = "PDC",
                    nom = "Pois du Cap (Kabaro gros calibre)",
                    nomMalagasy = "Kabaro maina",
                    type = "GRAIN",
                    classeAbc = "B",
                    variete = "Blanc calibré",
                    cmupCostAr = 3440.0,
                    prixVenteGrosAr = 4050.0,
                    consoJournaliereMoyenneKg = 150.0,
                    stockSecuriteKg = 2250.0,
                    pointDeCommandeKg = 4500.0,
                    stockAlerteCritiqueKg = 1200.0,
                    couvertureCibleJours = 30,
                    delaiApprovisionnementJours = 15,
                    seuilHumiditeMaxPercent = 13.0,
                    humiditeCiblePercent = 11.5,
                    temperatureStockageMaxC = 22.0,
                    sensibiliteHygroscopique = "MOYENNE",
                    compatibiliteOlfactive = true,
                    dureeConservationMaxJours = 120,
                    tauxFreinteTolerePercent = 0.60,
                    formatConditionnement = "Sacs 50 kg",
                    zoneEntrepot = "ZONE_C",
                    risquesSpecifiques = "Routes coupées en saison des pluies (Sud), variabilité calibre"
                ),
                ProduitEntity(
                    id = "prod_girofle",
                    code = "GIR",
                    nom = "Clous de Girofle (Madagascar Grade CG1)",
                    nomMalagasy = "Jirofo Madagasikara CG1",
                    type = "SPICE",
                    classeAbc = "A",
                    variete = "Clous entiers têtes présentes",
                    cmupCostAr = 28000.0,
                    prixVenteGrosAr = 35900.0,
                    consoJournaliereMoyenneKg = 40.0,
                    stockSecuriteKg = 600.0,
                    pointDeCommandeKg = 1200.0,
                    stockAlerteCritiqueKg = 300.0,
                    couvertureCibleJours = 45,
                    delaiApprovisionnementJours = 15,
                    seuilHumiditeMaxPercent = 12.0,
                    humiditeCiblePercent = 10.5,
                    temperatureStockageMaxC = 22.0,
                    sensibiliteHygroscopique = "ELEVEE",
                    compatibiliteOlfactive = false, // Arôme eugénol volatil : ne pas mélanger au riz
                    dureeConservationMaxJours = 730,
                    tauxFreinteTolerePercent = 0.80,
                    formatConditionnement = "Sacs doublés polyéthylène 10 kg / 25 kg",
                    zoneEntrepot = "ZONE_B",
                    risquesSpecifiques = "Falsification (tiges >5%, clous épuisés), vol (très haute valeur)"
                ),
                ProduitEntity(
                    id = "prod_poivre",
                    code = "POI",
                    nom = "Poivre noir & sauvage (Voatsiperifery)",
                    nomMalagasy = "Dipoavatra mainty & Voatsiperifery",
                    type = "SPICE",
                    classeAbc = "B",
                    variete = "Voatsiperifery sauvage trié",
                    cmupCostAr = 18000.0,
                    prixVenteGrosAr = 23000.0,
                    consoJournaliereMoyenneKg = 22.0,
                    stockSecuriteKg = 300.0,
                    pointDeCommandeKg = 650.0,
                    stockAlerteCritiqueKg = 150.0,
                    couvertureCibleJours = 45,
                    delaiApprovisionnementJours = 15,
                    seuilHumiditeMaxPercent = 12.0,
                    humiditeCiblePercent = 11.0,
                    temperatureStockageMaxC = 22.0,
                    sensibiliteHygroscopique = "ELEVEE",
                    compatibiliteOlfactive = false, // Pipérine volatile puissante
                    dureeConservationMaxJours = 730,
                    tauxFreinteTolerePercent = 0.80,
                    formatConditionnement = "Sacs étanches 25 kg",
                    zoneEntrepot = "ZONE_B",
                    risquesSpecifiques = "Mélange d'origines faux sauvage, absorption d'humidité de l'air"
                ),
                ProduitEntity(
                    id = "prod_gingembre",
                    code = "GIN",
                    nom = "Gingembre séché (Tranches lavées)",
                    nomMalagasy = "Sakamalaho maina",
                    type = "SPICE",
                    classeAbc = "C",
                    variete = "Tranches séchées lavées",
                    cmupCostAr = 7740.0,
                    prixVenteGrosAr = 9000.0,
                    consoJournaliereMoyenneKg = 45.0,
                    stockSecuriteKg = 450.0,
                    pointDeCommandeKg = 900.0,
                    stockAlerteCritiqueKg = 200.0,
                    couvertureCibleJours = 30,
                    delaiApprovisionnementJours = 10,
                    seuilHumiditeMaxPercent = 11.5,
                    humiditeCiblePercent = 10.0,
                    temperatureStockageMaxC = 22.0,
                    sensibiliteHygroscopique = "ELEVEE",
                    compatibiliteOlfactive = false,
                    dureeConservationMaxJours = 365,
                    tauxFreinteTolerePercent = 0.90,
                    formatConditionnement = "Sacs 25 kg & sachets 500g",
                    zoneEntrepot = "ZONE_B",
                    risquesSpecifiques = "Séchage insuffisant sur sol nu, fibres excessives"
                ),
                ProduitEntity(
                    id = "prod_curcuma",
                    code = "CUR",
                    nom = "Curcuma séché (Tamotamo pur)",
                    nomMalagasy = "Tamotamo maina",
                    type = "SPICE",
                    classeAbc = "C",
                    variete = "Racines séchées pures",
                    cmupCostAr = 5800.0,
                    prixVenteGrosAr = 7200.0,
                    consoJournaliereMoyenneKg = 28.0,
                    stockSecuriteKg = 300.0,
                    pointDeCommandeKg = 600.0,
                    stockAlerteCritiqueKg = 150.0,
                    couvertureCibleJours = 30,
                    delaiApprovisionnementJours = 10,
                    seuilHumiditeMaxPercent = 11.0,
                    humiditeCiblePercent = 10.0,
                    temperatureStockageMaxC = 22.0,
                    sensibiliteHygroscopique = "ELEVEE",
                    compatibiliteOlfactive = false,
                    dureeConservationMaxJours = 365,
                    tauxFreinteTolerePercent = 0.80,
                    formatConditionnement = "Fûts hermétiques & sachets 500g",
                    zoneEntrepot = "ZONE_B",
                    risquesSpecifiques = "Colorants artificiels ajoutés, oxydation à la lumière"
                )
            )
            produitDao.insertAll(produits)

            // 2. Fournisseurs qualifiés Sambatra Négoce
            val fournisseurs = listOf(
                FournisseurEntity(
                    id = "sup_coop_analanjirofo",
                    nom = "Coopérative Analanjirofo Espoir",
                    regionOrigine = "Fénérive-Est / Analanjirofo",
                    contactTelephone = "+261 32 55 123 45",
                    filieresFournies = "Girofle clous & griffes, Poivre noir",
                    noteSelection5 = 4.4,
                    tauxConformiteLotsPercent = 95.0,
                    avancesEnCoursAr = 4000000.0,
                    estSourcePrincipale = true
                ),
                FournisseurEntity(
                    id = "sup_groupement_ambatondrazaka",
                    nom = "Union Rizière d'Alaotra (Ambatondrazaka)",
                    regionOrigine = "Alaotra-Mangoro",
                    contactTelephone = "+261 34 22 345 67",
                    filieresFournies = "Paddy, Riz blanc Tsipala, Haricot",
                    noteSelection5 = 4.3,
                    tauxConformiteLotsPercent = 93.5,
                    avancesEnCoursAr = 0.0,
                    estSourcePrincipale = true
                ),
                FournisseurEntity(
                    id = "sup_collecteur_antsirabe",
                    nom = "Collecte Céréales Vakinankaratra",
                    regionOrigine = "Antsirabe / Betafo",
                    contactTelephone = "+261 32 11 876 54",
                    filieresFournies = "Maïs grain, Haricot blanc",
                    noteSelection5 = 3.8,
                    tauxConformiteLotsPercent = 88.0,
                    avancesEnCoursAr = 1500000.0,
                    estSourcePrincipale = false
                )
            )
            fournisseurDao.insertAll(fournisseurs)

            // 3. Lots initiaux tracés avec métadonnées de conservation Guide 2026
            val lots = listOf(
                StockLotEntity(
                    numeroLot = "RIZ-ALA-260920-01",
                    produitId = "prod_riz",
                    fournisseurId = "sup_groupement_ambatondrazaka",
                    nomFournisseur = "Union Rizière d'Alaotra",
                    regionOrigine = "Alaotra-Mangoro",
                    dateReception = "20/09/2026",
                    quantiteInitialeKg = 25000.0,
                    quantiteDisponibleKg = 18500.0,
                    nombreSacs = 500,
                    nombreSacsPeses = 52,
                    humiditeMesureePercent = 12.8,
                    impuretesPercent = 1.2,
                    grainsDefectueuxPercent = 2.1,
                    decisionControle = "CONFORME",
                    identifiantContreEchantillonScelle = "ECH-RIZ-260920-01-SC",
                    zoneStockageEntrepot = "ZONE_A",
                    coutUnitaireAchatAr = 2050.0,
                    dateLimiteConservation = "20/05/2027",
                    statutConservation = "OPTIMAL",
                    temperatureMesureeC = 22.5,
                    estBloqueQuarantaine = false,
                    codeQrTracabilite = "QR-LOT-RIZ-ALA-260920-01",
                    observationsControle = "Grain translucide d'excellente qualité, pas de trace de charançon."
                ),
                StockLotEntity(
                    numeroLot = "GIR-ANA-260915-03",
                    produitId = "prod_girofle",
                    fournisseurId = "sup_coop_analanjirofo",
                    nomFournisseur = "Coopérative Analanjirofo Espoir",
                    regionOrigine = "Fénérive-Est / Analanjirofo",
                    dateReception = "15/09/2026",
                    quantiteInitialeKg = 2000.0,
                    quantiteDisponibleKg = 1850.0,
                    nombreSacs = 40,
                    nombreSacsPeses = 6,
                    humiditeMesureePercent = 11.2,
                    impuretesPercent = 4.2,
                    grainsDefectueuxPercent = 1.5,
                    decisionControle = "CONFORME",
                    identifiantContreEchantillonScelle = "ECH-GIR-260915-03-SC",
                    zoneStockageEntrepot = "ZONE_B",
                    coutUnitaireAchatAr = 28000.0,
                    dateLimiteConservation = "15/09/2028",
                    statutConservation = "OPTIMAL",
                    temperatureMesureeC = 21.0,
                    estBloqueQuarantaine = false,
                    codeQrTracabilite = "QR-LOT-GIR-ANA-260915-03",
                    observationsControle = "Têtes intactes >92%, arôme piquant puissant. Stocké sous cage sécurisée."
                ),
                StockLotEntity(
                    numeroLot = "POI-ANA-260925-01",
                    produitId = "prod_poivre",
                    fournisseurId = "sup_coop_analanjirofo",
                    nomFournisseur = "Coopérative Analanjirofo Espoir",
                    regionOrigine = "Fénérive-Est / Analanjirofo",
                    dateReception = "25/09/2026",
                    quantiteInitialeKg = 800.0,
                    quantiteDisponibleKg = 720.0,
                    nombreSacs = 32,
                    nombreSacsPeses = 5,
                    humiditeMesureePercent = 11.4,
                    impuretesPercent = 2.0,
                    grainsDefectueuxPercent = 1.0,
                    decisionControle = "CONFORME",
                    identifiantContreEchantillonScelle = "ECH-POI-260925-01-SC",
                    zoneStockageEntrepot = "ZONE_B",
                    coutUnitaireAchatAr = 18000.0,
                    dateLimiteConservation = "25/09/2028",
                    statutConservation = "OPTIMAL",
                    temperatureMesureeC = 21.5,
                    estBloqueQuarantaine = false,
                    codeQrTracabilite = "QR-LOT-POI-ANA-260925-01",
                    observationsControle = "Grains denses, forte teneur en pipérine, sacs polyéthylène doublés."
                ),
                StockLotEntity(
                    numeroLot = "HAR-VAK-260810-02",
                    produitId = "prod_haricot",
                    fournisseurId = "sup_collecteur_antsirabe",
                    nomFournisseur = "Collecte Céréales Vakinankaratra",
                    regionOrigine = "Vakinankaratra (Betafo)",
                    dateReception = "10/08/2026",
                    quantiteInitialeKg = 6000.0,
                    quantiteDisponibleKg = 2100.0,
                    nombreSacs = 120,
                    nombreSacsPeses = 15,
                    humiditeMesureePercent = 13.4,
                    impuretesPercent = 2.8,
                    grainsDefectueuxPercent = 3.2,
                    decisionControle = "DECOTE",
                    identifiantContreEchantillonScelle = "ECH-HAR-260810-02-SC",
                    zoneStockageEntrepot = "ZONE_C",
                    coutUnitaireAchatAr = 3100.0,
                    dateLimiteConservation = "10/11/2026", // Plus de 65 jours : alerte FEFO
                    statutConservation = "A_DESTOCKER_FEFO",
                    temperatureMesureeC = 23.8,
                    estBloqueQuarantaine = false,
                    codeQrTracabilite = "QR-LOT-HAR-VAK-260810-02",
                    observationsControle = "Lot arrivant à 68 jours de magasinage. Risque bruches : à écouler en priorité !"
                ),
                StockLotEntity(
                    numeroLot = "MAI-VAK-261002-01",
                    produitId = "prod_mais",
                    fournisseurId = "sup_collecteur_antsirabe",
                    nomFournisseur = "Collecte Céréales Vakinankaratra",
                    regionOrigine = "Vakinankaratra (Betafo)",
                    dateReception = "02/10/2026",
                    quantiteInitialeKg = 15000.0,
                    quantiteDisponibleKg = 14850.0,
                    nombreSacs = 300,
                    nombreSacsPeses = 35,
                    humiditeMesureePercent = 12.8,
                    impuretesPercent = 1.9,
                    grainsDefectueuxPercent = 1.5,
                    decisionControle = "CONFORME",
                    identifiantContreEchantillonScelle = "ECH-MAI-261002-01-SC",
                    zoneStockageEntrepot = "ZONE_A",
                    coutUnitaireAchatAr = 1350.0,
                    dateLimiteConservation = "02/10/2027",
                    statutConservation = "OPTIMAL",
                    temperatureMesureeC = 22.0,
                    estBloqueQuarantaine = false,
                    codeQrTracabilite = "QR-LOT-MAI-VAK-261002-01",
                    observationsControle = "Grain sain, tri propre, taux d'humidité parfait."
                ),
                StockLotEntity(
                    numeroLot = "PDC-TUL-261005-01",
                    produitId = "prod_pois_cap",
                    fournisseurId = "sup_collecteur_antsirabe",
                    nomFournisseur = "Collecte Céréales Vakinankaratra",
                    regionOrigine = "Sud-Ouest (Tuléar/Morombe)",
                    dateReception = "05/10/2026",
                    quantiteInitialeKg = 3000.0,
                    quantiteDisponibleKg = 3000.0,
                    nombreSacs = 60,
                    nombreSacsPeses = 8,
                    humiditeMesureePercent = 14.2, // > seuil 13.0% : Quarantaine !
                    impuretesPercent = 5.5,
                    grainsDefectueuxPercent = 4.0,
                    decisionControle = "QUARANTAINE",
                    identifiantContreEchantillonScelle = "ECH-PDC-261005-01-SC",
                    zoneStockageEntrepot = "ZONE_Q",
                    coutUnitaireAchatAr = 3300.0,
                    dateLimiteConservation = "05/02/2027",
                    statutConservation = "QUARANTAINE",
                    temperatureMesureeC = 24.5,
                    estBloqueQuarantaine = true,
                    codeQrTracabilite = "QR-LOT-PDC-TUL-261005-01",
                    observationsControle = "Humidité 14.2% au-dessus du seuil (13.0%). Placé en Zone Q pour séchage et arbitrage décote."
                )
            )
            stockLotDao.insertAll(lots)

            // 4. Mouvements initiaux et registre des pertes (Guide 2026 Ch. 6.7, p. 33)
            val mouvements = listOf(
                MouvementInventaireEntity(
                    numeroLot = "RIZ-ALA-260920-01",
                    produitId = "prod_riz",
                    typeMouvement = "ENTREE_ACHAT",
                    quantiteKg = 25000.0,
                    valeurUnitaireAr = 2050.0,
                    valeurTotaleAr = 51250000.0,
                    dateMouvement = "2026-09-20 08:30",
                    numeroPieceRef = "BE-2609-020",
                    tiersConcerne = "Union Rizière d'Alaotra",
                    motifOuCause = "Réception conforme avec bon contradictoire",
                    visaResponsable = "Magasinier + Direction",
                    distanceKmTransport = 260.0,
                    impactCarboneKgCo2 = 185.0
                ),
                MouvementInventaireEntity(
                    numeroLot = "RIZ-ALA-260920-01",
                    produitId = "prod_riz",
                    typeMouvement = "SORTIE_VENTE",
                    quantiteKg = 6400.0,
                    valeurUnitaireAr = 2300.0,
                    valeurTotaleAr = 14720000.0,
                    dateMouvement = "2026-10-02 11:15",
                    numeroPieceRef = "BS-2610-008",
                    tiersConcerne = "Supermarché Score Ankorondrano",
                    motifOuCause = "Commande ferme au comptant",
                    visaResponsable = "Magasinier",
                    distanceKmTransport = 15.0,
                    impactCarboneKgCo2 = 8.5
                ),
                MouvementInventaireEntity(
                    numeroLot = "RIZ-ALA-260920-01",
                    produitId = "prod_riz",
                    typeMouvement = "PERTE_FREINTE_SECHAGE",
                    quantiteKg = 100.0,
                    valeurUnitaireAr = 2050.0,
                    valeurTotaleAr = 205000.0,
                    dateMouvement = "2026-10-15 17:00",
                    numeroPieceRef = "FP-2610-001",
                    tiersConcerne = "Interne Magasin",
                    motifOuCause = "Freinte naturelle de dessiccation hygrométrique (0.4% sur 25 jours)",
                    visaResponsable = "Responsable Qualité",
                    distanceKmTransport = 0.0,
                    impactCarboneKgCo2 = 0.0
                ),
                MouvementInventaireEntity(
                    numeroLot = "GIR-ANA-260915-03",
                    produitId = "prod_girofle",
                    typeMouvement = "ENTREE_ACHAT",
                    quantiteKg = 2000.0,
                    valeurUnitaireAr = 28000.0,
                    valeurTotaleAr = 56000000.0,
                    dateMouvement = "2026-09-15 09:30",
                    numeroPieceRef = "BE-2609-012",
                    tiersConcerne = "Coopérative Analanjirofo",
                    motifOuCause = "Réception conforme échantillonnée scellée",
                    visaResponsable = "Directeur Général",
                    distanceKmTransport = 350.0,
                    impactCarboneKgCo2 = 120.0
                ),
                MouvementInventaireEntity(
                    numeroLot = "GIR-ANA-260915-03",
                    produitId = "prod_girofle",
                    typeMouvement = "SORTIE_VENTE",
                    quantiteKg = 150.0,
                    valeurUnitaireAr = 35900.0,
                    valeurTotaleAr = 5385000.0,
                    dateMouvement = "2026-09-28 14:15",
                    numeroPieceRef = "BS-2609-044",
                    tiersConcerne = "MadaSpices Lab",
                    motifOuCause = "Livraison ferme sous bordereau",
                    visaResponsable = "Magasinier",
                    distanceKmTransport = 25.0,
                    impactCarboneKgCo2 = 12.0
                )
            )
            mouvementDao.insertAll(mouvements)
        }
    }
}
