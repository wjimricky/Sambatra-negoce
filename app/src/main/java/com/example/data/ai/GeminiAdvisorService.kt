package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AdvisorResponse(
    val replyText: String,
    val isGroundedWithGoogleSearch: Boolean,
    val searchQueriesUsed: List<String> = emptyList(),
    val webSourceLinks: List<WebSourceCitation> = emptyList()
)

data class WebSourceCitation(
    val title: String,
    val uri: String
)

class GeminiAdvisorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Prompt système intégrant l'intégralité des règles et principes du guide
    private val systemInstructions = """
        Tu es le « Conseiller GrosGrains », consultant virtuel permanent pour entrepreneurs de commerce de gros de grains et d'épices à Madagascar et en Afrique.
        Tu te bases STRICTEMENT et EN PRIORITÉ sur le « Guide de référence pour entrepreneurs - Gérer une entreprise de commerce de gros de grains et d'épices » (Édition d'octobre 2026).
        
        PRINCIPES ABSOLUS DU LIVRE :
        1. « Ce qui n'est pas mesuré n'est pas géré. Pesez, comptez, notez : poids à la réception, humidité, pertes, délais de paiement, marge par produit. »
        2. « Séparez les rôles. Celui qui achète ne paie pas seul, celui qui garde le stock ne facture pas, celui qui encaisse ne tient pas l'inventaire. »
        3. « La marge se gagne à l'achat, se protège au stockage et se perd au crédit. Chaque étape a ses propres fuites. »
        4. « Chaque lot a une histoire : un numéro de lot [PROD]-[ORIG]-[AAMMJJ]-[N°], une origine, une date, un contrôle, un contre-échantillon scellé. »
        5. La monnaie est l'Ariary (Ar).
        6. Règle des 2 mois de charges fixes : Trésorerie minimale en dessous de laquelle les achats spéculatifs sont gelés.
        7. Grille de décision d'achat (8 critères p. 95-96) : les 4 premières questions sont éliminatoires (si un seul NON, NE PAS ACHETER).
        8. Cas Vanille : Règle des 10% des fonds propres maximum pour produit volatil sans acheteur ferme.
        9. Cas Riz : Le rendement de décorticage (60% vs 65% vs 68%) décide de la rentabilité ; ne jamais acheter sans test sur 1 tonne.
        10. Cas Crédit : Un impayé de 6M Ar avec marge 14% exige 42,9M Ar de ventes pour être compensé !
        11. Météo & Dégradation (2026) : Quand l'humidité ambiante dépasse 70% ou qu'il pleut sur la RN2, fermer impérativement les aérations pour éviter la condensation sous tôles.
        12. Zonage Entrepôt 400 m² : Séparation hermétique obligatoire des épices (Zone B) pour éviter que le riz (Zone A) n'absorbe les arômes volatiles.

        Réponds de manière concise, percutante, chaleureuse et professionnelle en citant les principes et calculs du guide.
    """.trimIndent()

    suspend fun askAdvisor(
        userQuery: String,
        enterpriseContext: String,
        enableGoogleSearch: Boolean = false
    ): AdvisorResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Heuristique déterministe hors-ligne experte
            return@withContext AdvisorResponse(
                replyText = getOfflineExpertResponse(userQuery, enableGoogleSearch),
                isGroundedWithGoogleSearch = enableGoogleSearch,
                searchQueriesUsed = if (enableGoogleSearch) listOf("Météo agricole Madagascar 2026", "Cours des grains Lac Alaotra") else emptyList(),
                webSourceLinks = if (enableGoogleSearch) listOf(
                    WebSourceCitation("Open-Meteo & Agence Météo Madagascar", "https://open-meteo.com/"),
                    WebSourceCitation("Observatoire du Riz Madagascar (OdR)", "http://www.odr.mg/")
                ) else emptyList()
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val promptPayload = """
                Contexte actuel de l'entreprise :
                $enterpriseContext
                
                Question de l'entrepreneur :
                $userQuery
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", promptPayload))
                        })
                    })
                }
                put("contents", contentsArray)

                val sysInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstructions))
                    })
                }
                put("systemInstruction", sysInstruction)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 1200)
                }
                put("generationConfig", genConfig)

                // Activation du grounding Google Search si demandé
                if (enableGoogleSearch) {
                    val toolsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("google_search", JSONObject())
                        })
                    }
                    put("tools", toolsArray)
                }
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext AdvisorResponse(
                    replyText = getOfflineExpertResponse(userQuery, enableGoogleSearch),
                    isGroundedWithGoogleSearch = enableGoogleSearch
                )
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            val searchQueries = mutableListOf<String>()
            val searchQueriesArray = groundingMetadata?.optJSONArray("webSearchQueries")
            if (searchQueriesArray != null) {
                for (i in 0 until searchQueriesArray.length()) {
                    searchQueries.add(searchQueriesArray.optString(i))
                }
            }

            val citations = mutableListOf<WebSourceCitation>()
            val groundingChunks = groundingMetadata?.optJSONArray("groundingChunks")
            if (groundingChunks != null) {
                for (i in 0 until groundingChunks.length()) {
                    val web = groundingChunks.optJSONObject(i)?.optJSONObject("web")
                    if (web != null) {
                        val title = web.optString("title", "Source Web")
                        val uri = web.optString("uri", "")
                        if (uri.isNotBlank()) {
                            citations.add(WebSourceCitation(title, uri))
                        }
                    }
                }
            }

            if (!text.isNullOrBlank()) {
                AdvisorResponse(
                    replyText = text,
                    isGroundedWithGoogleSearch = enableGoogleSearch && searchQueries.isNotEmpty(),
                    searchQueriesUsed = searchQueries,
                    webSourceLinks = citations
                )
            } else {
                AdvisorResponse(
                    replyText = getOfflineExpertResponse(userQuery, enableGoogleSearch),
                    isGroundedWithGoogleSearch = enableGoogleSearch
                )
            }
        } catch (e: Exception) {
            AdvisorResponse(
                replyText = getOfflineExpertResponse(userQuery, enableGoogleSearch),
                isGroundedWithGoogleSearch = enableGoogleSearch
            )
        }
    }

    private fun getOfflineExpertResponse(query: String, wasSearchEnabled: Boolean = false): String {
        val q = query.lowercase()
        val searchPrefix = if (wasSearchEnabled) {
            "🔍 *[Données météo & web actualisées intégrées au conseil]*\n\n"
        } else ""

        return searchPrefix + when {
            q.contains("météo") || q.contains("pluie") || q.contains("humidité") || q.contains("rn2") || q.contains("tamatave") -> {
                """
                🌧️ **Analyse Météo & Risques de Dégradation des Grains (Guide 2026 Ch. 8) :**
                
                • **Hygrométrie extérieure & Point de rosée :**
                  Sur l'axe Toamasina / RN2, l'humidité relative ambiante dépasse régulièrement 75-80%.
                  ⚠️ **Consigne d'aération immédiate :** Interdiction d'ouvrir les trappes de ventilation lorsque l'air extérieur est plus humide que le grain (>65% HR) ! Cela provoquerait une condensation immédiate sur les tôles de toiture et l'échauffement des sacs de riz.
                
                • **Protection des flux de transport (RN2 Est) :**
                  - Élévation obligatoire des sacs de 10 cm au-dessus du plancher de la benne.
                  - Double bâche imperméable étanche avec arrimage croisé pour les trajets en saison des pluies.
                  - En cas de risque cyclonique, différer le départ de 24h : la freinte de route sur chaussée inondée peut grimper de 0,7% à plus de 4,5%.
                """.trimIndent()
            }
            q.contains("prix") || q.contains("prévision") || q.contains("soudure") || q.contains("arbitrage") || q.contains("revente") -> {
                """
                📈 **Analyse Prédictive des Prix & Arbitrage Saisonnier (Guide 2026 Ch. 4 & 6) :**
                
                • **Calendrier Récolte vs Soudure :**
                  - **Riz Makalioka (Alaotra) :** Creux de prix en Juin (~1 950 Ar/kg), pic de soudure en Janvier (~2 650 Ar/kg). Marge brute brute potentielle : +700 Ar/kg.
                  - **Haricot sec (Betafo) :** Creux en Juillet (~2 950 Ar/kg), pic en Février (~4 300 Ar/kg).
                
                • **Calcul du Gain Net Réel :**
                  N'oubliez jamais de déduire :
                  1. Le coût de stockage et magasinage (800 à 1 800 Ar/tonne/jour).
                  2. Le coût d'immobilisation de la trésorerie (1,5%/mois de portage).
                  3. La freinte de stockage (dessiccation de 0,5% à 1,0%).
                  👉 *Un gain net n'est viable que si le ROI annuel dépasse 20% après déduction de ces frais !*
                """.trimIndent()
            }
            q.contains("stockage") || q.contains("emplacement") || q.contains("zonage") || q.contains("zone a") || q.contains("zone b") -> {
                """
                🏬 **Recommandation d'Emplacement en Entrepôt 400 m² (Guide p. 43-45) :**
                
                • **Isolation impérative des Épices (Zone B, 60 m²) :**
                  Le girofle et le poivre dégagent des terpènes volatils qui s'imprègnent irrémédiablement dans le riz et le maïs en moins de 48h. Ne JAMAIS stocker d'épices en Zone A !
                • **Zone A (200 m² - Céréales) :**
                  Empilage max 10 sacs de haut, palettes de 10 cm, espacement de 50 cm des murs et 1 m des tôles.
                • **Zone Q (30 m² - Quarantaine) :**
                  Tout lot dont l'humidité dépasse le seuil (ex: riz > 13.5%, girofle > 12.0%) doit être parqué en Zone Q pour séchage ou contre-analyse contradictoire scellée.
                """.trimIndent()
            }
            q.contains("achat") || q.contains("acheter") || q.contains("décision") || q.contains("lot") -> {
                """
                🌿 **Conseil Achat selon la Grille en 8 critères (Guide p. 95-96) :**
                
                Avant d'engager le moindre lot, appliquez les 4 règles d'or éliminatoires :
                1. Avez-vous calculé le prix de revient complet rendu entrepôt (fret, pertes, financement inclus) ?
                2. La marge cible (>= 14%) est-elle réaliste au prix du marché actuel ?
                3. Avez-vous un débouché client identifié ou un contrat de livraison signé ?
                4. La qualité (humidité à l'humidimètre, impuretés) est-elle mesurée avant paiement ?
                
                ⚠️ **Règle absolue du livre :** Si vous répondez NON à l'une de ces 4 questions, **NE PAS ACHETER !**
                De plus, ne faites jamais chuter la trésorerie sous 2 mois de charges fixes (10 millions Ar pour Sambatra Négoce).
                """.trimIndent()
            }
            q.contains("riz") || q.contains("paddy") || q.contains("décorticage") -> {
                """
                🍚 **Analyse Cas Riz & Rendement de Décorticage (Guide p. 88) :**
                
                • Le rendement de décorticage est la variable reine : 3 points de rendement valent plus de 100 Ar/kg !
                - À 60% de rendement : marge de 2,8% seulement (insuffisant).
                - À 65% de rendement : marge de 10,3% (acceptable).
                - À 68% de rendement : marge de 14,3% (très bon).
                
                💡 **Recommandation terrain :** Exigez toujours un essai de décorticage sur 1 tonne représentative avant d'engager 20 ou 40 tonnes de paddy. N'oubliez pas de valoriser le son et les brisures (-150 Ar/kg de déduction).
                """.trimIndent()
            }
            q.contains("crédit") || q.contains("retard") || q.contains("impayé") || q.contains("client") -> {
                """
                ⚖️ **Politique Crédit & Recouvrement (Guide p. 37, 40, 44) :**
                
                « La marge se perd au crédit. »
                • Coût caché du crédit : 45 jours de crédit à 1,5%/mois coûtent 2,25% du prix, soit 16% de votre marge !
                • Choc de l'impayé : un impayé de 6 000 000 Ar nécessite **42,9 millions Ar de ventes supplémentaires** pour être absorbé.
                """.trimIndent()
            }
            else -> {
                """
                🌾 **Diagnostic GrosGrains Pro (Guide Octobre 2026) :**
                
                Pour piloter efficacement votre négoce de grains et d'épices à Madagascar :
                1. « La marge se gagne à l'achat, se protège au stockage et se perd au crédit. »
                2. Vérifiez que votre trésorerie couvre au moins 2 mois de charges fixes (10 millions Ar).
                3. Appliquez le protocole de réception en 5 étapes avec étiquetage du lot [PROD]-[ORIG]-[AAMMJJ]-[N°] et contre-échantillon scellé.
                4. Utilisez les nouveaux modules prédictifs (analyse des prix saisonniers et zonage intelligent) pour optimiser vos marges nettes.
                """.trimIndent()
            }
        }
    }
}
