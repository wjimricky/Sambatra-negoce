package com.example.data.weather

import android.util.Log
import com.example.data.model.WarehouseZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object WeatherService {

    private const val TAG = "WeatherService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    // Hubs majeurs du négoce de grains et d'épices à Madagascar
    val LOCATIONS = listOf(
        WarehouseLocation(
            id = "tana",
            nameFr = "Antananarivo (Hub Sambatra Négoce)",
            nameMg = "Antananarivo (Trano lehibe 400 m²)",
            latitude = -18.8792,
            longitude = 47.5079,
            region = "Analamanga (Hautes Terres)",
            description = "Entrepôt central 400 m². Climat tempéré d'altitude, fortes variations thermiques jour/nuit.",
            typicalRisk = "Condensation de toiture sous tôle à l'aube (delta T° > 10°C)."
        ),
        WarehouseLocation(
            id = "tamatave",
            nameFr = "Toamasina / Tamatave (Port Export)",
            nameMg = "Toamasina (Seranantsambo)",
            latitude = -18.1492,
            longitude = 49.4023,
            region = "Atsinanana (Côte Est)",
            description = "Zone maritime tropicale à très haute hygrométrie (>80% HR). Transit export poivre & girofle.",
            typicalRisk = "Réhumidification violente et moisissures des épices non scellées."
        ),
        WarehouseLocation(
            id = "ambatondrazaka",
            nameFr = "Ambatondrazaka (Grenier Riz Alaotra)",
            nameMg = "Ambatondrazaka (Vary Alaotra)",
            latitude = -17.8333,
            longitude = 48.4167,
            region = "Alaotra-Mangoro",
            description = "Bassin principal de production rizicole. Collecte et séchage des paddy.",
            typicalRisk = "Séchage insuffisant des paddy entrants (>14% d'humidité initiale)."
        ),
        WarehouseLocation(
            id = "majunga",
            nameFr = "Mahajanga / Majunga (Côte Ouest)",
            nameMg = "Mahajanga (Morontsiraka Andrefana)",
            latitude = -15.7167,
            longitude = 46.3167,
            region = "Boeny",
            description = "Climat chaud et sec. Grand port de commerce céréalier et légumineuses.",
            typicalRisk = "Échauffement thermique et prolifération accélérée de charançons."
        ),
        WarehouseLocation(
            id = "fianar",
            nameFr = "Fianarantsoa (Corridor Sud Légumineuses)",
            nameMg = "Fianarantsoa (Faritra Atsimo)",
            latitude = -21.4527,
            longitude = 47.0857,
            region = "Haute Matsiatra",
            description = "Plateaux du Sud. Collecte de haricots blancs et pois du Cap (Kabaro).",
            typicalRisk = "Brouillards matinaux et humidité résiduelle dans les sacs en jute."
        )
    )

    /**
     * Récupère les données météo en temps réel via l'API Open-Meteo
     * Si hors-ligne ou erreur, bascule immédiatement sur des relevés météorologiques calibrés.
     */
    suspend fun fetchWeather(location: WarehouseLocation): WeatherRiskAssessment = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}" +
                "&current=temperature_2m,relative_humidity_2m,dew_point_2m,precipitation,weather_code,wind_speed_10m" +
                "&hourly=temperature_2m,relative_humidity_2m&forecast_days=1&timezone=auto"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "GrosGrainsPro-Madagascar/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val current = json.getJSONObject("current")
                    val temp = current.optDouble("temperature_2m", 23.5)
                    val humidity = current.optDouble("relative_humidity_2m", 68.0)
                    val dewPoint = current.optDouble("dew_point_2m", 16.8)
                    val precip = current.optDouble("precipitation", 0.0)
                    val wCode = current.optInt("weather_code", 1)
                    val wind = current.optDouble("wind_speed_10m", 10.0)

                    val hourlyObj = json.optJSONObject("hourly")
                    val hourlyList = mutableListOf<HourlyHumidityForecast>()
                    if (hourlyObj != null) {
                        val times = hourlyObj.optJSONArray("time")
                        val temps = hourlyObj.optJSONArray("temperature_2m")
                        val hums = hourlyObj.optJSONArray("relative_humidity_2m")
                        if (times != null && hums != null) {
                            val count = minOf(times.length(), 12)
                            for (i in 0 until count step 2) {
                                val tStr = times.optString(i)
                                val hour = if (tStr.length >= 16) tStr.substring(11, 16) else "${i}h"
                                hourlyList.add(
                                    HourlyHumidityForecast(
                                        hourLabel = hour,
                                        temperatureC = temps?.optDouble(i, temp) ?: temp,
                                        humidityPct = hums.optDouble(i, humidity)
                                    )
                                )
                            }
                        }
                    }

                    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
                    val weatherData = CurrentWeatherData(
                        location = location,
                        temperatureC = Math.round(temp * 10.0) / 10.0,
                        relativeHumidityPct = Math.round(humidity * 10.0) / 10.0,
                        dewPointC = Math.round(dewPoint * 10.0) / 10.0,
                        precipitationMm = Math.round(precip * 10.0) / 10.0,
                        weatherCode = wCode,
                        windSpeedKmh = Math.round(wind * 10.0) / 10.0,
                        conditionDescription = decodeWeatherCode(wCode),
                        recordedAt = nowStr,
                        isLiveFromNetwork = true
                    )
                    return@withContext computeAssessment(weatherData, hourlyList)
                }
            }
        } catch (e: Exception) {
            try {
                Log.w(TAG, "Météo réseau non disponible (${e.message}), bascule sur données étalonnées Guide 2026")
            } catch (_: Throwable) {
                println("WeatherService: Météo réseau non disponible, bascule sur données étalonnées")
            }
        }

        // Fallback local réaliste et étalonné pour le négoce malgache
        return@withContext getCalibratedAssessment(location)
    }

    private fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Ciel dégagé, soleil franc"
            1, 2 -> "Partiellement nuageux"
            3 -> "Couvert / Nuageux"
            45, 48 -> "Brouillard matinal humide"
            51, 53, 55 -> "Bruine légère"
            61, 63, 65 -> "Pluie battante tropicale"
            80, 81, 82 -> "Averses orageuses soudaines"
            95, 96, 99 -> "Orage d'été et rafales"
            else -> "Conditions variables"
        }
    }

    fun getCalibratedAssessment(location: WarehouseLocation): WeatherRiskAssessment {
        val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
        val (temp, hum, precip, desc) = when (location.id) {
            "tamatave" -> Quad(27.8, 84.5, 3.2, "Averses côtières & très forte hygrométrie marine")
            "ambatondrazaka" -> Quad(25.0, 72.0, 0.4, "Nuageux, humidité de vallée résiduelle")
            "majunga" -> Quad(32.4, 54.0, 0.0, "Ensoleillé, chaleur sèche favorable au stockage")
            "fianar" -> Quad(21.2, 69.5, 0.0, "Ciel voilé, fraîcheur des hauts plateaux")
            else -> Quad(24.5, 66.0, 0.0, "Ensoleillé avec passages nuageux (Antananarivo)")
        }

        val dewPoint = temp - ((100.0 - hum) / 5.0)

        val hourly = listOf(
            HourlyHumidityForecast("06:00", temp - 4, (hum + 12).coerceAtMost(98.0)),
            HourlyHumidityForecast("08:00", temp - 2, (hum + 6).coerceAtMost(95.0)),
            HourlyHumidityForecast("11:00", temp + 1, (hum - 8).coerceAtLeast(40.0)),
            HourlyHumidityForecast("14:00", temp + 3, (hum - 14).coerceAtLeast(35.0)),
            HourlyHumidityForecast("17:00", temp + 1, hum - 3),
            HourlyHumidityForecast("20:00", temp - 2, (hum + 8).coerceAtMost(95.0))
        )

        val weatherData = CurrentWeatherData(
            location = location,
            temperatureC = temp,
            relativeHumidityPct = hum,
            dewPointC = Math.round(dewPoint * 10.0) / 10.0,
            precipitationMm = precip,
            weatherCode = if (precip > 0) 61 else 1,
            windSpeedKmh = 14.0,
            conditionDescription = desc,
            recordedAt = nowStr,
            isLiveFromNetwork = false
        )

        return computeAssessment(weatherData, hourly)
    }

    /**
     * Analyse des risques de dégradation selon les seuils du Guide 2026 (Ch. 5 & Ch. 8)
     */
    private fun computeAssessment(
        weather: CurrentWeatherData,
        hourly: List<HourlyHumidityForecast>
    ): WeatherRiskAssessment {
        val ambientHum = weather.relativeHumidityPct
        val temp = weather.temperatureC
        val dewPoint = weather.dewPointC

        val zoneRisks = mutableListOf<StorageZoneWeatherRisk>()

        // 1. Zone A : Céréales (Riz Blanc, Riz Cargo, Riz Paddy, Maïs)
        // Seuil d'équilibre hygroscopique du riz : HR ambiante < 65% (Optimal), 65-72% (Vigilance), >72% (Critique)
        val zoneALevel = when {
            ambientHum > 72.0 -> StorageRiskLevel.DANGER_CRITIQUE
            ambientHum > 65.0 -> StorageRiskLevel.VIGILANCE
            else -> StorageRiskLevel.OPTIMAL
        }
        val zoneARisks = mutableListOf<String>()
        val zoneAActions = mutableListOf<String>()
        if (zoneALevel == StorageRiskLevel.DANGER_CRITIQUE) {
            zoneARisks.add("Absorption d'humidité par le grain (>14.0% critique) avec risque de pourrissement interne")
            zoneARisks.add("Risque élevé de formation d'aflatoxines (B1/B2) si la température dépasse 24°C")
            zoneARisks.add("Point de rosée (${dewPoint}°C) proche des tôles : risque de condensation tombant sur les sacs supérieurs")
            zoneAActions.add("Fermer hermétiquement toutes les trappes et portes extérieures de l'entrepôt")
            zoneAActions.add("Bâcher le haut des piles de riz avec des bâches étanches inclinées anti-gouttes")
            zoneAActions.add("Suspendre tout déchargement sous la pluie et contrôler les hygrométries des sacs de tête")
        } else if (zoneALevel == StorageRiskLevel.VIGILANCE) {
            zoneARisks.add("Humidité extérieure (${ambientHum}%) supérieure à la cible optimale (60-65%)")
            zoneARisks.add("Lente réhumidification superficielle des sacs en jute extérieurs")
            zoneAActions.add("N'ouvrir la ventilation qu'entre 11h30 et 14h30 lorsque l'air est le plus sec")
            zoneAActions.add("Vérifier l'espace de 80 cm entre les piles et les parois de l'entrepôt (Guide p. 33)")
        } else {
            zoneARisks.add("Conditions hygrométriques idéales pour la conservation longue durée du riz et du maïs")
            zoneAActions.add("Maintenir les registres de ventilation passive ouverts pour aérer le centre des piles")
        }
        zoneRisks.add(
            StorageZoneWeatherRisk(
                zoneId = WarehouseZoneId.ZONE_A,
                zoneName = "Zone A - Céréales & Grands Volumes",
                mainProducts = "Riz Blanc Makalioka, Riz Rouge, Riz Paddy, Maïs",
                maxAllowedAmbientHumidityPct = 65.0,
                currentAmbientHumidityPct = ambientHum,
                riskLevel = zoneALevel,
                degradationRisks = zoneARisks,
                preventiveActions = zoneAActions
            )
        )

        // 2. Zone B : Épices Nobles Sécurisées (Poivre Noir, Clous de Girofle)
        // Les épices sont extrêmement hygroscopiques et sensibles aux huiles essentielles
        // Seuil d'équilibre : HR ambiante < 60% (Optimal), 60-68% (Vigilance), >68% (Danger critique)
        val zoneBLevel = when {
            ambientHum > 68.0 -> StorageRiskLevel.DANGER_CRITIQUE
            ambientHum > 60.0 -> StorageRiskLevel.VIGILANCE
            else -> StorageRiskLevel.OPTIMAL
        }
        val zoneBRisks = mutableListOf<String>()
        val zoneBActions = mutableListOf<String>()
        if (zoneBLevel == StorageRiskLevel.DANGER_CRITIQUE) {
            zoneBRisks.add("Perte irrémédiable de teneur en pipérine (poivre) et eugénol (girofle) par humidification")
            zoneBRisks.add("Risque de développement de moisissures blanches et déclassement de grade export")
            zoneBRisks.add("Ramollissement du grain rendant le calibrage invendable aux transformateurs")
            zoneBActions.add("Maintenir la cage Zone B verrouillée et allumer les déshumidificateurs mobiles")
            zoneBActions.add("Enfermer les sacs dans des suremballages polyéthylène hermétiques thermoscellés")
            zoneBActions.add("Interdire toute aération extérieure tant que l'HR dépasse 62%")
        } else if (zoneBLevel == StorageRiskLevel.VIGILANCE) {
            zoneBRisks.add("Hygrométrie ambiante (${ambientHum}%) au-dessus du seuil de confort des épices (60%)")
            zoneBActions.add("Effectuer un sondage d'humidité sur 5 sacs témoins du poivre et girofle")
            zoneBActions.add("Activer les sachets absorbeurs d'humidité (gel de silice) dans les caisses d'échantillons")
        } else {
            zoneBRisks.add("Excellente stabilité des arômes et huiles essentielles")
            zoneBActions.add("Stockage nominal sécurisé sous surveillance thermo-hygrométrique quotidienne")
        }
        zoneRisks.add(
            StorageZoneWeatherRisk(
                zoneId = WarehouseZoneId.ZONE_B,
                zoneName = "Zone B - Épices & Produits Nobles",
                mainProducts = "Poivre Noir de Madagascar, Clous de Girofle (Grade CG1)",
                maxAllowedAmbientHumidityPct = 60.0,
                currentAmbientHumidityPct = ambientHum,
                riskLevel = zoneBLevel,
                degradationRisks = zoneBRisks,
                preventiveActions = zoneBActions
            )
        )

        // 3. Zone C : Légumineuses (Haricots Blancs, Lingots, Pois du Cap Kabaro)
        // Seuil critique : HR ambiante > 68%
        val zoneCLevel = when {
            ambientHum > 70.0 -> StorageRiskLevel.DANGER_CRITIQUE
            ambientHum > 64.0 -> StorageRiskLevel.VIGILANCE
            else -> StorageRiskLevel.OPTIMAL
        }
        val zoneCRisks = mutableListOf<String>()
        val zoneCActions = mutableListOf<String>()
        if (zoneCLevel == StorageRiskLevel.DANGER_CRITIQUE) {
            zoneCRisks.add("Ramollissement du tégument et augmentation exponentielle du taux d'infestation par bruches")
            zoneCRisks.add("Perte de fermeté à la cuisson entraînant des litiges et décotes clients")
            zoneCActions.add("Prioriser le déstockage FEFO immédiat des lots de Pois du Cap avant perte de qualité")
            zoneCActions.add("Appliquer une surveillance visuelle anti-bruches sous les caillebotis")
        } else {
            zoneCRisks.add("Conservation satisfaisante des téguments et de la texture marchande")
            zoneCActions.add("Contrôle standard de la fermeté et de l'aération entre les palettes")
        }
        zoneRisks.add(
            StorageZoneWeatherRisk(
                zoneId = WarehouseZoneId.ZONE_C,
                zoneName = "Zone C - Légumineuses & Pois",
                mainProducts = "Pois du Cap (Kabaro), Haricots Blancs Lingots",
                maxAllowedAmbientHumidityPct = 64.0,
                currentAmbientHumidityPct = ambientHum,
                riskLevel = zoneCLevel,
                degradationRisks = zoneCRisks,
                preventiveActions = zoneCActions
            )
        )

        // Recommandation d'aération calculée selon la psychrométrie
        val canOpen = ambientHum <= 68.0 && weather.precipitationMm == 0.0
        val aeration = AerationRecommendation(
            canOpenAerationNow = canOpen,
            statusHeadlineFr = if (canOpen) {
                "AÉRATION AUTORISÉE (Air extérieur sec ${ambientHum}%)"
            } else {
                "AÉRATION INTERDITE (Air saturé ${ambientHum}% ou pluie)"
            },
            reasonFr = if (canOpen) {
                "L'air extérieur (${ambientHum}% HR) est suffisamment sec pour évacuer les calories et l'humidité accumulées au cœur des piles."
            } else {
                "Ouvrir les aérations injecterait de la vapeur d'eau directement dans les stocks de grains et d'épices, accélérant les moisissures et la freinte."
            },
            safeAerationWindow = if (ambientHum > 75.0) "Aucune fenêtre favorable aujourd'hui. Maintenir confinement." else "Entre 11h30 et 14h30 (pic de chaleur réduisant l'HR).",
            recommendedActions = if (canOpen) listOf(
                "Ouvrir les registres d'aération nord et sud pour ventilation traversante",
                "Faire tourner les brasseurs d'air pendant 2 heures consécutives",
                "Fermer impérativement avant 16h30 avant la montée de la brume vespérale"
            ) else listOf(
                "Calfeutrer les portes roulantes d'accès quai",
                "Actionner la ventilation interne en circuit fermé sans apport d'air externe",
                "Surveiller le point de rosée (${dewPoint}°C) par rapport à la température des tôles"
            )
        )

        val nowReportStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
        return WeatherRiskAssessment(
            weather = weather,
            hourlyForecast = hourly,
            zoneRisks = zoneRisks,
            aerationRecommendation = aeration,
            generatedAt = nowReportStr
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
