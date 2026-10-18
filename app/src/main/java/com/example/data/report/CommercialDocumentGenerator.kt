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
import com.example.data.model.BatchLot
import com.example.data.model.Client
import com.example.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeliveryNoteItem(
    val lotNumber: String,
    val productName: String,
    val packagingFormat: String,
    val bagsCount: Int,
    val grossWeightKg: Double,
    val tareWeightKg: Double,
    val netWeightKg: Double,
    val unitPriceAr: Double,
    val totalAmountAr: Double
)

data class DeliveryNoteData(
    val deliveryNumber: String,
    val dateFormatted: String,
    val orderReference: String,
    val client: Client,
    val vehiclePlate: String,
    val driverName: String,
    val sealNumber: String,
    val items: List<DeliveryNoteItem>,
    val totalBags: Int,
    val totalNetWeightKg: Double,
    val totalAmountAr: Double,
    val notesAndReservations: String
)

data class PhysicalInventoryLine(
    val productCode: String,
    val productName: String,
    val warehouseZone: String,
    val theoreticalStockKg: Double,
    val actualCountedStockKg: Double,
    val varianceKg: Double,
    val variancePercent: Double,
    val standardAllowedLossPercent: Double, // Ex: 1.2%
    val isVarianceNormal: Boolean,
    val unitValueAr: Double,
    val varianceValueAr: Double,
    val explanation: String
)

data class MonthlyAuditInventoryData(
    val inventoryNumber: String,
    val auditDate: String,
    val auditorName: String,
    val warehouseSupervisorName: String,
    val lines: List<PhysicalInventoryLine>,
    val totalTheoreticalKg: Double,
    val totalActualKg: Double,
    val totalVarianceKg: Double,
    val totalVarianceValueAr: Double,
    val overallShrinkageRatePercent: Double,
    val auditConclusion: String
)

object CommercialDocumentGenerator {

    const val COMPANY_NAME = "Sambatra Négoce SARL"
    const val COMPANY_HEADER = "Commerce de Gros de Céréales, Légumineuses & Épices"
    const val COMPANY_ADDRESS = "Rue Pasteur, Zone Commerciale Anosibe - Antananarivo 101"
    const val COMPANY_CONTACT = "Tél : +261 34 00 111 22 • Email : direction@sambatra-negoce.mg"
    const val COMPANY_LEGAL = "NIF : 4001289345 • STAT : 46301 11 2026 0 10234 • RCS : 2026 B 00412"

    /**
     * Crée les données d'un Bon de Livraison exemplaire pour un client.
     */
    fun createSampleDeliveryNote(client: Client, products: List<Product>, lots: List<BatchLot>): DeliveryNoteData {
        val todayStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
        val deliveryNum = "BL-26-${(100..999).random()}"

        val sampleItems = listOf(
            DeliveryNoteItem(
                lotNumber = "RIZ-ALA-260920-01",
                productName = "Riz blanc usiné Makalioka (Sac 50kg)",
                packagingFormat = "Sacs polypropylène tissé 50kg",
                bagsCount = 20,
                grossWeightKg = 1004.0,
                tareWeightKg = 4.0,
                netWeightKg = 1000.0,
                unitPriceAr = 2300.0,
                totalAmountAr = 2_300_000.0
            ),
            DeliveryNoteItem(
                lotNumber = "HAR-ALA-260920-02",
                productName = "Haricot sec rouge Tsipoy (Sac 50kg)",
                packagingFormat = "Sacs polypropylène tissé 50kg",
                bagsCount = 10,
                grossWeightKg = 502.0,
                tareWeightKg = 2.0,
                netWeightKg = 500.0,
                unitPriceAr = 3660.0,
                totalAmountAr = 1_830_000.0
            )
        )

        return DeliveryNoteData(
            deliveryNumber = deliveryNum,
            dateFormatted = todayStr,
            orderReference = "CMD-26-894",
            client = client,
            vehiclePlate = "7845 TAB (Canter 5T)",
            driverName = "Jean-Baptiste Randria",
            sealNumber = "SC-SAM-98214",
            items = sampleItems,
            totalBags = sampleItems.sumOf { it.bagsCount },
            totalNetWeightKg = sampleItems.sumOf { it.netWeightKg },
            totalAmountAr = sampleItems.sumOf { it.totalAmountAr },
            notesAndReservations = "Marchandise chargée en bon état, exempte d'humidité extérieure. Scellé de benne intact à l'arrivée."
        )
    }

    /**
     * Génère la fiche d'audit et inventaire contradictoire de fin de mois.
     */
    fun createSampleMonthlyAuditData(products: List<Product>): MonthlyAuditInventoryData {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

        val lines = listOf(
            PhysicalInventoryLine(
                productCode = "RIZ",
                productName = "Riz blanc usiné Makalioka",
                warehouseZone = "Zone A - Palettes 01 à 06",
                theoreticalStockKg = 18_185.0,
                actualCountedStockKg = 18_000.0,
                varianceKg = -185.0,
                variancePercent = -1.02,
                standardAllowedLossPercent = 1.20,
                isVarianceNormal = true,
                unitValueAr = 2080.0,
                varianceValueAr = -384_800.0,
                explanation = "Freinte de séchage normale constatée (hygrométrie passée de 13.9% à 13.2%)."
            ),
            PhysicalInventoryLine(
                productCode = "HAR",
                productName = "Haricot sec rouge Tsipoy",
                warehouseZone = "Zone C - Palettes 04 à 08",
                theoreticalStockKg = 9_598.0,
                actualCountedStockKg = 9_500.0,
                varianceKg = -98.0,
                variancePercent = -1.02,
                standardAllowedLossPercent = 1.50,
                isVarianceNormal = true,
                unitValueAr = 3151.0,
                varianceValueAr = -308_798.0,
                explanation = "Poussières et écart de triage à la réception. Inférieur au seuil max de 1.5%."
            ),
            PhysicalInventoryLine(
                productCode = "MAI",
                productName = "Maïs grain sec jaune",
                warehouseZone = "Zone A - Palettes 12 à 18",
                theoreticalStockKg = 25_662.0,
                actualCountedStockKg = 25_500.0,
                varianceKg = -162.0,
                variancePercent = -0.63,
                standardAllowedLossPercent = 1.80,
                isVarianceNormal = true,
                unitValueAr = 1380.0,
                varianceValueAr = -223_560.0,
                explanation = "Freinte et manipulation vrac. Stock très propre, aucune attaque de rongeur."
            ),
            PhysicalInventoryLine(
                productCode = "GIR",
                productName = "Clous de Girofle CG3",
                warehouseZone = "Zone B - Box Blindé 1",
                theoreticalStockKg = 1_851.8,
                actualCountedStockKg = 1_850.0,
                varianceKg = -1.8,
                variancePercent = -0.10,
                standardAllowedLossPercent = 0.50,
                isVarianceNormal = true,
                unitValueAr = 28_000.0,
                varianceValueAr = -50_400.0,
                explanation = "Écart minime de pesée au gramme près sur sacs étanches. Scellés vérifiés."
            )
        )

        val totalTheo = lines.sumOf { it.theoreticalStockKg }
        val totalActual = lines.sumOf { it.actualCountedStockKg }
        val totalVar = lines.sumOf { it.varianceKg }
        val totalVarVal = lines.sumOf { it.varianceValueAr }
        val avgLossRate = if (totalTheo > 0) Math.abs(totalVar / totalTheo) * 100.0 else 0.0

        return MonthlyAuditInventoryData(
            inventoryNumber = "INV-FIN-2026-10",
            auditDate = todayStr,
            auditorName = "Commissaire aux Comptes / Réviseur Indépendant",
            warehouseSupervisorName = "Chef de Dépôt Sambatra",
            lines = lines,
            totalTheoreticalKg = totalTheo,
            totalActualKg = totalActual,
            totalVarianceKg = totalVar,
            totalVarianceValueAr = totalVarVal,
            overallShrinkageRatePercent = avgLossRate,
            auditConclusion = "Conforme aux normes du Guide 2026. Taux global de freinte constaté (${String.format(Locale.US, "%.2f", avgLossRate)}%) très inférieur au plafond réglementaire de 1.20%. Aucun coulage suspect."
        )
    }

    /**
     * Génère un fichier PDF pour le Bon de Livraison avec l'API standard Android Canvas/PdfDocument.
     */
    fun generateDeliveryNotePdf(context: Context, data: DeliveryNoteData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Format A4 portrait
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Fond et en-tête
        paint.color = Color.rgb(22, 101, 52) // Vert Agro
        canvas.drawRect(0f, 0f, 595f, 65f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText(COMPANY_NAME.uppercase(Locale.FRENCH), 28f, 28f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(COMPANY_HEADER, 28f, 44f, paint)
        canvas.drawText(COMPANY_LEGAL, 28f, 57f, paint)

        // Titre du document
        paint.color = Color.rgb(30, 41, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        canvas.drawText("BON DE LIVRAISON & CONTRÔLE DE TRANSPORT", 28f, 95f, paint)

        paint.textSize = 10f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("N° Document : ${data.deliveryNumber}  •  Date : ${data.dateFormatted}  •  Réf. Commande : ${data.orderReference}", 28f, 112f, paint)

        // Cadres Client & Transport
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(28f, 125f, 300f, 215f, 8f, 8f, paint)
        canvas.drawRoundRect(310f, 125f, 567f, 215f, 8f, 8f, paint)

        paint.color = Color.rgb(22, 101, 52)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("DESTINATAIRE / CLIENT", 38f, 142f, paint)
        canvas.drawText("TRANSPORT & SÉCURITÉ", 320f, 142f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 9.5f
        canvas.drawText(data.client.name, 38f, 158f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Contact : ${data.client.contactName}", 38f, 172f, paint)
        canvas.drawText("Tél : ${data.client.phone}", 38f, 186f, paint)
        canvas.drawText("Lieu : ${data.client.location}", 38f, 200f, paint)

        canvas.drawText("Véhicule : ${data.vehiclePlate}", 320f, 158f, paint)
        canvas.drawText("Chauffeur : ${data.driverName}", 320f, 172f, paint)
        canvas.drawText("N° Scellé Benne : ${data.sealNumber}", 320f, 186f, paint)
        canvas.drawText("Conditionnement : Sacs intacts, sans humidité", 320f, 200f, paint)

        // Tableau des articles
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(28f, 230f, 567f, 252f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("N° LOT (Traçabilité)", 34f, 245f, paint)
        canvas.drawText("DÉSIGNATION MARCHANDISE", 160f, 245f, paint)
        canvas.drawText("SACS", 340f, 245f, paint)
        canvas.drawText("POIDS NET (KG)", 385f, 245f, paint)
        canvas.drawText("P.U. (AR)", 465f, 245f, paint)
        canvas.drawText("TOTAL (AR)", 515f, 245f, paint)

        var y = 270f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        data.items.forEach { item ->
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(item.lotNumber, 34f, y, paint)
            canvas.drawText(item.productName.take(30), 160f, y, paint)
            canvas.drawText("${item.bagsCount}", 345f, y, paint)
            canvas.drawText("${item.netWeightKg.toInt()} kg", 390f, y, paint)
            canvas.drawText("${item.unitPriceAr.toInt()} Ar", 465f, y, paint)
            canvas.drawText("${item.totalAmountAr.toInt()} Ar", 515f, y, paint)

            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(28f, y + 8f, 567f, y + 8f, paint)
            y += 24f
        }

        // Totaux
        paint.color = Color.rgb(22, 101, 52)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("TOTAL CHARGEMENT :", 280f, y + 15f, paint)
        canvas.drawText("${data.totalBags} sacs  •  ${data.totalNetWeightKg.toInt()} kg", 400f, y + 15f, paint)

        paint.textSize = 12f
        paint.color = Color.rgb(194, 65, 12)
        canvas.drawText("MONTANT TOTAL : ${String.format(Locale.US, "%,.0f", data.totalAmountAr).replace(',', ' ')} ARIARY", 280f, y + 36f, paint)

        // Clauses sanitaires et de réception
        paint.color = Color.rgb(254, 242, 242)
        canvas.drawRoundRect(28f, y + 55f, 567f, y + 125f, 6f, 6f, paint)

        paint.color = Color.rgb(153, 27, 27)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("CLAUSE OBLIGATOIRE DE CONTRÔLE CONTRADICTOIRE À RÉCEPTION (Guide 2026 p. 22-23)", 36f, y + 70f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7.8f
        paint.color = Color.rgb(69, 10, 10)
        canvas.drawText("1. Le destinataire a l'obligation de peser contradictoirement au moins 10% des sacs au hasard avant déchargement complet.", 36f, y + 84f, paint)
        canvas.drawText("2. En cas de non-conformité (humidité > seuil, sacs souillés, tare anormale), mentionner les réserves précises sur ce bon.", 36f, y + 96f, paint)
        canvas.drawText("3. Aucune réclamation sur le poids ou l'état physique ne sera admise après signature sans réserve et départ du camion.", 36f, y + 108f, paint)

        // Visas
        val signY = y + 145f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(28f, signY, 280f, signY + 90f, 6f, 6f, paint)
        canvas.drawRoundRect(310f, signY, 567f, signY + 90f, 6f, 6f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("VISA EXPÉDITEUR / CHAUFFEUR", 38f, signY + 18f, paint)
        canvas.drawText("VISA CLIENT / RÉCEPTIONNAIRE", 320f, signY + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Date, Heure & Signature :", 38f, signY + 32f, paint)
        canvas.drawText("Date, Cachet & Signature 'Bon pour accord' :", 320f, signY + 32f, paint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "documents").apply { mkdirs() }
        val outputFile = File(outputDir, "BL_${data.deliveryNumber}.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        return outputFile
    }

    /**
     * Ouvre ou partage le document généré.
     */
    fun shareDocument(context: Context, file: File, title: String = "Partager le document") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Log.e("CommercialDoc", "Impossible de partager le document", e)
        }
    }
}
