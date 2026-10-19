package com.example.data.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.closing.AnnualProfitLossStatement
import com.example.data.closing.OperationalAuditScorecard
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Générateur PDF certifié pour la Clôture Annuelle et le Bilan d'Audit Opérationnel.
 * Conforme aux exigences du Guide de référence 2026 pour Sambatra Négoce SARL.
 */
object AnnualAuditPdfReportGenerator {

    fun generateAnnualClosingPdf(
        context: Context,
        pnl: AnnualProfitLossStatement,
        scorecard: OperationalAuditScorecard
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Format A4 portrait
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Palette
        val darkGreen = Color.rgb(27, 94, 32)
        val deepForest = Color.rgb(18, 55, 23)
        val textDark = Color.rgb(33, 33, 33)
        val textMuted = Color.rgb(100, 116, 139)
        val lightBg = Color.rgb(241, 245, 249)
        val cardBg = Color.rgb(248, 250, 252)
        val accentGold = Color.rgb(217, 119, 6)

        // Entête officiel
        paint.color = deepForest
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SAMBATRA NÉGOCE SARL - COMMERCE DE GROS", 30f, 32f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("NIF : ${pnl.nif}  |  STAT : ${pnl.stat}  |  Siège : Antananarivo - Madagascar", 30f, 48f, paint)
        canvas.drawText("Entrepôt Principal : 400 m² (Ventilation Naturelle & Palettisation 15 cm)", 30f, 62f, paint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date())
        paint.textSize = 8.5f
        canvas.drawText("Édité le : $dateStr  |  Exercice Fiscal : ${pnl.fiscalYear}", 30f, 76f, paint)

        var y = 115f

        // Titre du document
        paint.color = darkGreen
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BILAN ANNUEL D'EXPLOITATION & AUDIT DE GESTION (GUIDE 2026)", 30f, y, paint)
        y += 18f

        // Bandeau Résultat & Score d'Audit
        paint.color = cardBg
        canvas.drawRoundRect(30f, y, 565f, y + 60f, 8f, 8f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(30f, y, 565f, y + 60f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textDark
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CHIFFRE D'AFFAIRES", 42f, y + 20f, paint)
        canvas.drawText("MARGE BRUTE", 185f, y + 20f, paint)
        canvas.drawText("RÉSULTAT NET", 325f, y + 20f, paint)
        canvas.drawText("SCORE D'AUDIT 2026", 450f, y + 20f, paint)

        paint.textSize = 12f
        paint.color = darkGreen
        canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", pnl.grossTurnoverAr)} Ar", 42f, y + 42f, paint)
        canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", pnl.grossMarginAr)} Ar (${pnl.grossMarginRatePercent}%)", 185f, y + 42f, paint)
        canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", pnl.netOperatingProfitAr)} Ar", 325f, y + 42f, paint)

        paint.color = accentGold
        canvas.drawText("${scorecard.overallScore}/100 pts", 450f, y + 42f, paint)

        y += 75f

        // Section 1 : Compte de Résultat d'Exploitation Général
        paint.color = textDark
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. COMPTE D'EXPLOITATION SYNTHÉTIQUE", 30f, y, paint)
        y += 14f

        fun drawPnlLine(label: String, value: String, isBold: Boolean = false, isIndent: Boolean = false, color: Int = textDark) {
            paint.color = color
            paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            val xLabel = if (isIndent) 45f else 32f
            canvas.drawText(label, xLabel, y, paint)
            canvas.drawText(value, 460f, y, paint)
            y += 12f
        }

        drawPnlLine("Chiffre d'Affaires Brut Global (${String.format(Locale.FRENCH, "%,.0f", pnl.volumeTotalSoldKg)} kg)", "${String.format(Locale.FRENCH, "%,.0f", pnl.grossTurnoverAr)} Ar", isBold = true)
        drawPnlLine("Coût d'Achat des Marchandises Vendues (CAMV au CMUP)", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.costOfGoodsSoldAr)} Ar", isIndent = true)
        drawPnlLine("Marge Commerciale Brute Réalisée", "${String.format(Locale.FRENCH, "%,.0f", pnl.grossMarginAr)} Ar (${pnl.grossMarginRatePercent}%)", isBold = true, color = darkGreen)
        drawPnlLine("Frais de transport & logistique régionale", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.logisticsAndTransportAr)} Ar", isIndent = true)
        drawPnlLine("Coût d'immobilisation stockage (2.5%/mois)", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.totalStorageCarryingCostAr)} Ar", isIndent = true)
        drawPnlLine("Perte sur freinte réelle constatée", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.realShrinkageLossAr)} Ar", isIndent = true)
        drawPnlLine("Créances irrécouvrables passées en pertes", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.badDebtsWriteOffAr)} Ar", isIndent = true)
        drawPnlLine("Total Charges Variables d'Exploitation", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.totalVariableExpensesAr)} Ar", isBold = true)
        drawPnlLine("Marge sur Coûts Variables (MCV)", "${String.format(Locale.FRENCH, "%,.0f", pnl.contributionMarginAr)} Ar (${pnl.contributionMarginPercent}%)", isBold = true, color = darkGreen)
        drawPnlLine("Charges Fixes Annuelles (Entrepôt, Salaires, Admin)", "- ${String.format(Locale.FRENCH, "%,.0f", pnl.totalFixedExpensesAr)} Ar", isBold = true)
        drawPnlLine("Excédent Brut d'Exploitation (EBE / EBITDA)", "${String.format(Locale.FRENCH, "%,.0f", pnl.operatingIncomeAr)} Ar", isBold = true, color = darkGreen)
        drawPnlLine("RÉSULTAT NET COMPTABLE (Après impôt 20%)", "${String.format(Locale.FRENCH, "%,.0f", pnl.netOperatingProfitAr)} Ar (${pnl.netProfitMarginPercent}%)", isBold = true, color = deepForest)

        y += 10f

        // Seuil de Rentabilité
        paint.color = lightBg
        canvas.drawRoundRect(30f, y, 565f, y + 36f, 6f, 6f, paint)
        paint.color = textDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SEUIL DE RENTABILITÉ (POINT MORT) :", 40f, y + 16f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("CA Seuil : ${String.format(Locale.FRENCH, "%,.0f", pnl.breakEvenTurnoverAr)} Ar  |  Tonnage Seuil : ${pnl.breakEvenVolumeTons} Tonnes  |  Atteint en ${pnl.breakEvenDays} Jours", 40f, y + 28f, paint)

        y += 48f

        // Section 2 : Décomposition Analytique par Produit
        paint.color = textDark
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. PERFORMANCE DES 8 COMMODITÉS", 30f, y, paint)
        y += 14f

        // Table Header
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRect(30f, y, 565f, y + 16f, paint)
        paint.color = textDark
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Produit", 35f, y + 11f, paint)
        canvas.drawText("Volume (kg)", 175f, y + 11f, paint)
        canvas.drawText("CA (Ar)", 265f, y + 11f, paint)
        canvas.drawText("Marge Brute (Ar)", 365f, y + 11f, paint)
        canvas.drawText("Taux", 480f, y + 11f, paint)
        canvas.drawText("Part Marge", 520f, y + 11f, paint)
        y += 20f

        pnl.productBreakdowns.take(8).forEach { prod ->
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            canvas.drawText(prod.productName, 35f, y, paint)
            canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", prod.volumeSoldKg)} kg", 175f, y, paint)
            canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", prod.turnoverAr)} Ar", 265f, y, paint)
            canvas.drawText("${String.format(Locale.FRENCH, "%,.0f", prod.grossMarginAr)} Ar", 365f, y, paint)
            canvas.drawText("${String.format(Locale.FRENCH, "%.1f", prod.grossMarginRatePercent)}%", 480f, y, paint)
            canvas.drawText("${prod.shareOfTotalMarginPercent}%", 525f, y, paint)
            y += 11f
        }

        y += 10f

        // Section 3 : Audit de Conformité Guide 2026
        paint.color = textDark
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. AUDIT DES RÈGLES D'OR DU GUIDE 2026", 30f, y, paint)
        y += 14f

        scorecard.verifiedRules.forEach { rule ->
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = if (rule.isCompliant) darkGreen else Color.RED
            val statusSymbol = if (rule.isCompliant) "[CONFORME]" else "[VIGILANCE]"
            canvas.drawText("$statusSymbol ${rule.ruleTitle} (${rule.referenceChapter})", 32f, y, paint)
            y += 10f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = textMuted
            canvas.drawText("Cible : ${rule.expectedTarget}  |  Réalisé : ${rule.achievedValue} - ${rule.comment}", 40f, y, paint)
            y += 12f
        }

        y += 8f

        // Conclusion & Signature
        paint.color = cardBg
        canvas.drawRoundRect(30f, y, 565f, y + 42f, 6f, 6f, paint)
        paint.color = textDark
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Appréciation Générale : ${scorecard.generalAuditorAppreciation}", 36f, y + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Statut Officiel : ${scorecard.certificationGrade.name} - Certifié pour l'exercice 2026", 36f, y + 26f, paint)
        canvas.drawText("Direction Générale Sambatra Négoce SARL", 380f, y + 36f, paint)

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "annual_reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val file = File(outputDir, "Bilan_Annuel_Sambatra_2026.pdf")

        try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            Log.d("AnnualAuditPdf", "PDF annuel généré avec succès : ${file.absolutePath}")
            return file
        } catch (e: Exception) {
            Log.e("AnnualAuditPdf", "Erreur lors de la génération PDF", e)
            pdfDocument.close()
            return null
        }
    }

    fun openOrSharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Bilan Annuel d'Exploitation 2026 - Sambatra Négoce SARL")
            putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le Bilan Annuel certifié conforme au Guide 2026.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Partager le Bilan Annuel PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
