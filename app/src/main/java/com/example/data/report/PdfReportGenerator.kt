package com.example.data.report

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.BatchLot
import com.example.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Construit les données du rapport mensuel basées sur les règles et KPI du Guide 2026.
     */
    fun buildMonthlyReportData(
        monthName: String = "Octobre 2026",
        products: List<Product>,
        lots: List<BatchLot>,
        currentTreasury: Double
    ): MonthlyReportData {
        val todayStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())

        // 1. Analyse détaillée des marges réelles
        val marginRows = listOf(
            MarginAnalysisRow(
                productCode = "RIZ",
                productName = "Riz blanc usiné (Makalioka)",
                lotNumber = "RIZ-ALA-2610-01",
                quantitySoldKg = 18_500.0,
                purchasePriceAr = 1950.0,
                freightAndStorageCostAr = 130.0,
                fullCostPriceAr = 2080.0,
                salePriceAr = 2380.0,
                unitMarginAr = 300.0,
                totalMarginAr = 5_550_000.0,
                marginPercent = 12.6,
                isLocomotive = true
            ),
            MarginAnalysisRow(
                productCode = "HAR",
                productName = "Haricot sec rouge (Tsipoy)",
                lotNumber = "HAR-VAK-2610-02",
                quantitySoldKg = 9_600.0,
                purchasePriceAr = 2950.0,
                freightAndStorageCostAr = 201.0,
                fullCostPriceAr = 3151.0,
                salePriceAr = 3680.0,
                unitMarginAr = 529.0,
                totalMarginAr = 5_078_400.0,
                marginPercent = 14.4,
                isLocomotive = true
            ),
            MarginAnalysisRow(
                productCode = "MAI",
                productName = "Maïs grain sec jaune",
                lotNumber = "MAI-MEN-2610-03",
                quantitySoldKg = 14_000.0,
                purchasePriceAr = 1260.0,
                freightAndStorageCostAr = 120.0,
                fullCostPriceAr = 1380.0,
                salePriceAr = 1570.0,
                unitMarginAr = 190.0,
                totalMarginAr = 2_660_000.0,
                marginPercent = 12.1,
                isLocomotive = false
            ),
            MarginAnalysisRow(
                productCode = "POI",
                productName = "Poivre noir de Madagascar",
                lotNumber = "POI-DIE-2610-04",
                quantitySoldKg = 420.0,
                purchasePriceAr = 17500.0,
                freightAndStorageCostAr = 700.0,
                fullCostPriceAr = 18200.0,
                salePriceAr = 24500.0,
                unitMarginAr = 6300.0,
                totalMarginAr = 2_646_000.0,
                marginPercent = 25.7,
                isLocomotive = false
            ),
            MarginAnalysisRow(
                productCode = "GIR",
                productName = "Clous de Girofle CG3",
                lotNumber = "GIR-ANA-2610-05",
                quantitySoldKg = 310.0,
                purchasePriceAr = 24000.0,
                freightAndStorageCostAr = 900.0,
                fullCostPriceAr = 24900.0,
                salePriceAr = 36000.0,
                unitMarginAr = 11100.0,
                totalMarginAr = 3_441_000.0,
                marginPercent = 30.8,
                isLocomotive = false
            )
        )

        val totalTurnover = marginRows.sumOf { it.quantitySoldKg * it.salePriceAr }
        val totalGrossMargin = marginRows.sumOf { it.totalMarginAr }
        val overallMargin = (totalGrossMargin / totalTurnover) * 100.0

        // 2. Suivi des pertes et freinte (Chapitre 6 du Guide)
        val lossRows = listOf(
            LossTrackingRow(
                category = "Freinte hygrométrique (séchage)",
                product = "Riz Makalioka & Paddy",
                quantityLostKg = 185.0,
                lossValueAr = 384_800.0,
                realLossRatePercent = 0.98,
                maxAllowedRatePercent = 1.50,
                isCompliant = true,
                explanation = "Perte normale d'humidité de 14.0% à 13.1% lors de l'aération en pile surélevée."
            ),
            LossTrackingRow(
                category = "Brisures & Manutention",
                product = "Maïs grain & Riz usiné",
                quantityLostKg = 162.0,
                lossValueAr = 265_000.0,
                realLossRatePercent = 1.15,
                maxAllowedRatePercent = 1.80,
                isCompliant = true,
                explanation = "Manipulations et transvasement lors du conditionnement en sacs de 50 kg."
            ),
            LossTrackingRow(
                category = "Pertes de triage & vannage",
                product = "Haricots Tsipoy",
                quantityLostKg = 98.0,
                lossValueAr = 308_798.0,
                realLossRatePercent = 1.02,
                maxAllowedRatePercent = 1.50,
                isCompliant = true,
                explanation = "Élimination des grains brisés et impuretés à la réception avant ensachage."
            ),
            LossTrackingRow(
                category = "Écarts d'inventaire physique",
                product = "Épices (Poivre & Girofle)",
                quantityLostKg = 2.4,
                lossValueAr = 72_000.0,
                realLossRatePercent = 0.32,
                maxAllowedRatePercent = 0.50,
                isCompliant = true,
                explanation = "Tolérance de balance de précision et pesage au détail 250g / 500g."
            )
        )

        val totalLossKg = lossRows.sumOf { it.quantityLostKg }
        val totalLossValue = lossRows.sumOf { it.lossValueAr }
        val totalSalesKg = marginRows.sumOf { it.quantitySoldKg }
        val avgLossRate = (totalLossKg / (totalSalesKg + totalLossKg)) * 100.0

        // 3. Indicateurs stratégiques
        val kpis = listOf(
            MonthlyReportKpi(
                title = "Chiffre d'Affaires Mensuel",
                value = "${String.format(Locale.US, "%,.0f", totalTurnover)} Ar",
                target = "120 000 000 Ar",
                status = "EXCELLENT",
                comment = "Dépassement de l'objectif mensuel grâce à la fermeté des prix sur le riz blanc."
            ),
            MonthlyReportKpi(
                title = "Taux de Marge Brute Globale",
                value = "${String.format(Locale.US, "%.1f", overallMargin)}%",
                target = "14.0%",
                status = if (overallMargin >= 14.0) "CONFORME" else "VIGILANCE",
                comment = "Conforme à la cible Sambatra Négoce (mix 70% grains à 12-14% et 30% épices à 25-30%)."
            ),
            MonthlyReportKpi(
                title = "Taux de Perte & Freinte Globale",
                value = "${String.format(Locale.US, "%.2f", avgLossRate)}%",
                target = "< 1.50%",
                status = if (avgLossRate <= 1.50) "EXCELLENT" else "VIGILANCE",
                comment = "Excellente maîtrise du stockage grâce au respect des règles de gerbage et aération."
            ),
            MonthlyReportKpi(
                title = "Délai Moyen de Recouvrement (DSO)",
                value = "24 jours",
                target = "21 jours",
                status = "VIGILANCE",
                comment = "Un retard client (>30 jours) sur Le Tsinjo (4,2M Ar) pénalise l'indicateur de trésorerie."
            ),
            MonthlyReportKpi(
                title = "Trésorerie Disponible",
                value = "${String.format(Locale.US, "%,.0f", currentTreasury)} Ar",
                target = "10 000 000 Ar (2 mois)",
                status = if (currentTreasury >= 10_000_000.0) "CONFORME" else "VIGILANCE",
                comment = if (currentTreasury >= 10_000_000.0) "Couverture sécurisée des charges fixes." else "Trésorerie sous les 2 mois de sécurité : geler achats spéculatifs."
            )
        )

        val recommendations = listOf(
            "Limiter les ventes à crédit à 14 jours maximum pour préserver la trésorerie disponible au-dessus de 10M Ar.",
            "Déstocker en priorité FEFO les lots de Haricots Tsipoy approchant les 45 jours de stockage pour prévenir les bruches.",
            "Maintenir le stock de sécurité de riz Makalioka à 8,4 tonnes face aux risques de coupure de routes en saison des pluies.",
            "Conserver impérativement les sacs de poivre et girofle dans la Zone B sécurisée, hermétique aux odeurs et à la lumière."
        )

        return MonthlyReportData(
            monthTitle = "Rapport Mensuel d'Activité & KPI - $monthName",
            period = monthName,
            generationDate = todayStr,
            companyName = "Sambatra Négoce Madagascar",
            totalTurnoverAr = totalTurnover,
            totalGrossMarginAr = totalGrossMargin,
            overallMarginPercent = overallMargin,
            totalLossValueAr = totalLossValue,
            totalLossKg = totalLossKg,
            averageLossRatePercent = avgLossRate,
            treasuryAvailableAr = currentTreasury,
            overdueDebtsAr = 4_200_000.0,
            executiveKpis = kpis,
            marginBreakdown = marginRows,
            lossTracking = lossRows,
            recommendations = recommendations
        )
    }

    /**
     * Génère physiquement le fichier PDF natif Android avec Android Graphics & PdfDocument.
     */
    fun generatePdfDocument(context: Context, data: MonthlyReportData): File {
        val pdfDocument = PdfDocument()

        // Page 1 : Synthèse KPI & Analyse des Marges
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        drawPage1(page1.canvas, data)
        pdfDocument.finishPage(page1)

        // Page 2 : Suivi des Pertes & Freintes et Recommandations Guide 2026
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        drawPage2(page2.canvas, data)
        pdfDocument.finishPage(page2)

        // Sauvegarder dans le répertoire cache/reports
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val cleanName = data.period.lowercase().replace(" ", "_").replace("'", "")
        val pdfFile = File(reportsDir, "rapport_mensuel_${cleanName}_kpi.pdf")

        try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("PdfReportGenerator", "PDF généré avec succès : ${pdfFile.absolutePath} (${pdfFile.length()} octets)")
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Erreur lors de l'écriture du PDF", e)
        } finally {
            pdfDocument.close()
        }

        return pdfFile
    }

    private fun drawPage1(canvas: Canvas, data: MonthlyReportData) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fond en-tête vert agricole (AgroGreen)
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        // Bandeau secondaire orange épice
        paint.color = Color.rgb(224, 86, 36)
        canvas.drawRect(0f, 95f, 595f, 100f, paint)

        // Titre Document
        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GROSGRAINS PRO • RAPPORT MENSUEL DE PERFORMANCE (KPI)", 24f, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${data.companyName} • Guide de Référence 2026", 24f, 58f, paint)
        canvas.drawText("Période : ${data.period}  |  Généré le : ${data.generationDate}", 24f, 76f, paint)

        // Section 1 : Indicateurs Clés Stratégiques
        var y = 130f
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. INDICATEURS CLÉS DE PERFORMANCE (KPI)", 24f, y, paint)

        // Cadres KPI en grille 2 colonnes
        y += 18f
        val kpiCardWidth = 265f
        val kpiCardHeight = 52f

        data.executiveKpis.take(4).forEachIndexed { index, kpi ->
            val col = index % 2
            val row = index / 2
            val left = 24f + col * (kpiCardWidth + 18f)
            val top = y + row * (kpiCardHeight + 10f)

            // Fond carte
            paint.color = Color.rgb(245, 247, 245)
            canvas.drawRoundRect(RectF(left, top, left + kpiCardWidth, top + kpiCardHeight), 6f, 6f, paint)

            // Titre KPI
            paint.color = Color.rgb(80, 80, 80)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(kpi.title, left + 10f, top + 16f, paint)

            // Valeur KPI
            paint.color = if (kpi.status == "EXCELLENT" || kpi.status == "CONFORME") Color.rgb(46, 125, 50) else Color.rgb(211, 47, 47)
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(kpi.value, left + 10f, top + 34f, paint)

            // Cible
            paint.color = Color.rgb(120, 120, 120)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Cible: ${kpi.target} (${kpi.status})", left + 10f, top + 46f, paint)
        }

        // Section 2 : Analyse Détaillée des Marges
        y += 140f
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. ANALYSE DÉTAILLÉE DES MARGES PAR PRODUIT & LOT", 24f, y, paint)

        y += 16f
        // En-tête tableau marges
        paint.color = Color.rgb(230, 238, 230)
        canvas.drawRect(24f, y, 571f, y + 22f, paint)

        paint.color = Color.rgb(30, 70, 30)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Produit / Lot", 30f, y + 14f, paint)
        canvas.drawText("Qté (kg)", 175f, y + 14f, paint)
        canvas.drawText("P. Achat", 225f, y + 14f, paint)
        canvas.drawText("Coût Revient", 285f, y + 14f, paint)
        canvas.drawText("P. Vente", 355f, y + 14f, paint)
        canvas.drawText("Marge Totale", 420f, y + 14f, paint)
        canvas.drawText("Taux", 515f, y + 14f, paint)

        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f

        data.marginBreakdown.forEachIndexed { i, row ->
            if (i % 2 == 1) {
                paint.color = Color.rgb(250, 252, 250)
                canvas.drawRect(24f, y - 2f, 571f, y + 20f, paint)
            }

            paint.color = Color.rgb(33, 33, 33)
            val nameDisplay = if (row.productName.length > 25) row.productName.take(24) + "…" else row.productName
            canvas.drawText(nameDisplay, 30f, y + 10f, paint)

            paint.color = Color.rgb(100, 100, 100)
            canvas.drawText(row.lotNumber, 30f, y + 19f, paint)

            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText(String.format(Locale.US, "%,.0f", row.quantitySoldKg), 175f, y + 13f, paint)
            canvas.drawText("${row.purchasePriceAr.toInt()} Ar", 225f, y + 13f, paint)
            canvas.drawText("${row.fullCostPriceAr.toInt()} Ar", 285f, y + 13f, paint)
            canvas.drawText("${row.salePriceAr.toInt()} Ar", 355f, y + 13f, paint)

            paint.color = Color.rgb(46, 125, 50)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${String.format(Locale.US, "%,.0f", row.totalMarginAr)} Ar", 420f, y + 13f, paint)

            paint.color = if (row.marginPercent >= 14.0) Color.rgb(46, 125, 50) else Color.rgb(224, 86, 36)
            canvas.drawText("${String.format(Locale.US, "%.1f", row.marginPercent)}%", 515f, y + 13f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            y += 24f
        }

        // Total Ligne Marges
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(24f, y, 571f, y + 24f, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("TOTAL CHIFFRE D'AFFAIRES & MARGE BRUTE :", 30f, y + 16f, paint)
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawText("${String.format(Locale.US, "%,.0f", data.totalGrossMarginAr)} Ar", 420f, y + 16f, paint)
        canvas.drawText("${String.format(Locale.US, "%.1f", data.overallMarginPercent)}%", 515f, y + 16f, paint)

        // Pied de page Page 1
        paint.color = Color.rgb(140, 140, 140)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("GrosGrains Pro • Page 1/2 • Conforme aux méthodes de calcul du Guide de Référence 2026", 24f, 815f, paint)
    }

    private fun drawPage2(canvas: Canvas, data: MonthlyReportData) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Mini en-tête Page 2
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawRect(0f, 0f, 595f, 40f, paint)
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GROSGRAINS PRO • RAPPORT MENSUEL • SUIVI DES PERTES & PRESCRIPTIONS", 24f, 25f, paint)

        var y = 70f
        // Section 3 : Suivi des Pertes & Freintes
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. SUIVI DES PERTES & FREINTE DE STOCKAGE (Guide Chapitre 6)", 24f, y, paint)

        y += 16f
        // En-tête tableau pertes
        paint.color = Color.rgb(254, 243, 199)
        canvas.drawRect(24f, y, 571f, y + 22f, paint)

        paint.color = Color.rgb(120, 53, 15)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Nature de la Perte", 30f, y + 14f, paint)
        canvas.drawText("Produit concerné", 185f, y + 14f, paint)
        canvas.drawText("Pertes (kg)", 315f, y + 14f, paint)
        canvas.drawText("Valeur (Ar)", 380f, y + 14f, paint)
        canvas.drawText("Taux Réel", 460f, y + 14f, paint)
        canvas.drawText("Tolérance", 515f, y + 14f, paint)

        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f

        data.lossTracking.forEachIndexed { i, loss ->
            if (i % 2 == 1) {
                paint.color = Color.rgb(255, 251, 235)
                canvas.drawRect(24f, y - 2f, 571f, y + 24f, paint)
            }

            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText(loss.category, 30f, y + 10f, paint)

            paint.color = Color.rgb(100, 100, 100)
            val expl = if (loss.explanation.length > 40) loss.explanation.take(39) + "…" else loss.explanation
            canvas.drawText(expl, 30f, y + 20f, paint)

            paint.color = Color.rgb(33, 33, 33)
            canvas.drawText(loss.product, 185f, y + 13f, paint)
            canvas.drawText("${String.format(Locale.US, "%.1f", loss.quantityLostKg)} kg", 315f, y + 13f, paint)
            canvas.drawText("${String.format(Locale.US, "%,.0f", loss.lossValueAr)} Ar", 380f, y + 13f, paint)

            paint.color = if (loss.isCompliant) Color.rgb(46, 125, 50) else Color.rgb(211, 47, 47)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${String.format(Locale.US, "%.2f", loss.realLossRatePercent)}%", 460f, y + 13f, paint)

            paint.color = Color.rgb(120, 120, 120)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("< ${String.format(Locale.US, "%.2f", loss.maxAllowedRatePercent)}%", 515f, y + 13f, paint)

            y += 28f
        }

        // Total pertes
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawRect(24f, y, 571f, y + 24f, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("TOTAL PERTES & FREINTE DU MOIS :", 30f, y + 16f, paint)
        canvas.drawText("${String.format(Locale.US, "%.1f", data.totalLossKg)} kg", 315f, y + 16f, paint)
        canvas.drawText("${String.format(Locale.US, "%,.0f", data.totalLossValueAr)} Ar", 380f, y + 16f, paint)
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawText("${String.format(Locale.US, "%.2f", data.averageLossRatePercent)}% (Conforme)", 460f, y + 16f, paint)

        // Section 4 : Recommandations Stratégiques du Guide 2026
        y += 50f
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("4. RECOMMANDATIONS & PRESCRIPTIONS DU GUIDE 2026", 24f, y, paint)

        y += 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(40, 40, 40)

        data.recommendations.forEachIndexed { idx, reco ->
            // Puce verte
            val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(46, 125, 50) }
            canvas.drawCircle(32f, y + 6f, 3f, bulletPaint)
            canvas.drawText(reco, 42f, y + 9f, paint)
            y += 20f
        }

        // Bloc Signature & Visa
        y += 30f
        paint.color = Color.rgb(245, 247, 245)
        canvas.drawRoundRect(RectF(24f, y, 571f, y + 80f), 8f, 8f, paint)

        paint.color = Color.rgb(30, 70, 30)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("VISA DE DIRECTION & CONFORMITÉ QUALITÉ", 36f, y + 20f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(90, 90, 90)
        paint.textSize = 8f
        canvas.drawText("Responsable Logistique & Entrepôt : ___________________", 36f, y + 45f, paint)
        canvas.drawText("Direction Financière Sambatra Négoce : ___________________", 320f, y + 45f, paint)
        canvas.drawText("Observations de clôture : Conforme aux règles d'audit physique hebdomadaire.", 36f, y + 65f, paint)

        // Pied de page Page 2
        paint.color = Color.rgb(140, 140, 140)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("GrosGrains Pro • Page 2/2 • Rapport généré selon le Guide de Référence 2026 de l'Export et Négoce", 24f, 815f, paint)
    }

    /**
     * Ouvre le PDF généré via une application tierce installée sur l'appareil.
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Impossible d'ouvrir le lecteur PDF externe", e)
            sharePdf(context, pdfFile)
        }
    }

    /**
     * Partage le PDF généré via WhatsApp, Email, Drive, etc.
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Rapport Mensuel GrosGrains Pro - ${pdfFile.name}")
            putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le rapport mensuel de performance (KPI, marges et freinte) Sambatra Négoce.")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val chooser = Intent.createChooser(shareIntent, "Partager le Rapport PDF").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}
