package com.example

import com.example.data.logistics.LogisticsOptimizer
import com.example.data.logistics.WarningSeverity
import com.example.data.weather.WeatherService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogisticsAndWeatherTest {

    @Test
    fun testLogistics_IncompatibleSpicesAndGrainsDetected() {
        // CMD-001 is Rice (Grain), CMD-003 is Pepper (Spice)
        // Tested on V-10T which has no insulated compartment
        val plan = LogisticsOptimizer.buildOptimizedTrip(
            vehicleId = "V-10T",
            corridorId = "rn2",
            selectedOrderIds = listOf("CMD-001", "CMD-003")
        )

        val hasSanitaryWarning = plan.compatibilityWarnings.any {
            it.level == WarningSeverity.CRITICAL_BLOCKING && it.title.contains("Épices & Céréales")
        }
        assertTrue("Le mélange d'épices et de céréales sans compartiment hermétique doit déclencher une alerte bloquante", hasSanitaryWarning)
    }

    @Test
    fun testLogistics_InsulatedVehicleResolvesSpiceIncompatibility() {
        // Tested on V-3T5 (Camionnette Isotherme)
        val plan = LogisticsOptimizer.buildOptimizedTrip(
            vehicleId = "V-3T5",
            corridorId = "urbain_tana",
            selectedOrderIds = listOf("CMD-001", "CMD-004") // Rice + Clove
        )

        val hasBlockingWarning = plan.compatibilityWarnings.any {
            it.level == WarningSeverity.CRITICAL_BLOCKING
        }
        assertFalse("Le compartiment isotherme doit lever le blocage sanitaire", hasBlockingWarning)
    }

    @Test
    fun testLogistics_OverloadDetection() {
        // Pick-up max payload is 1200 kg
        // Order CMD-001 is 2500 kg
        val plan = LogisticsOptimizer.buildOptimizedTrip(
            vehicleId = "V-1T2",
            corridorId = "urbain_tana",
            selectedOrderIds = listOf("CMD-001")
        )

        assertTrue("La surcharge doit être détectée", plan.weightUtilizationPct > 100.0)
        val hasOverloadWarning = plan.compatibilityWarnings.any {
            it.level == WarningSeverity.CRITICAL_BLOCKING && it.title.contains("Surcharge")
        }
        assertTrue("Alerte bloquante de surcharge attendue", hasOverloadWarning)
    }

    @Test
    fun testLogistics_RoadShrinkageCalculation() {
        // On RN2, baseline shrinkage is 0.75%
        val plan = LogisticsOptimizer.buildOptimizedTrip(
            vehicleId = "V-10T",
            corridorId = "rn2",
            selectedOrderIds = listOf("CMD-002") // 4000 kg
        )

        // 4000 kg * 0.75% = 30.0 kg
        assertEquals(30.0, plan.estimatedRoadShrinkageKg, 0.1)
    }

    @Test
    fun testWeatherAssessment_HumidTamataveDetectsCriticalRiskAndForbidsAeration() = runBlocking {
        val tamatave = WeatherService.LOCATIONS.first { it.id == "tamatave" }
        val assessment = WeatherService.fetchWeather(tamatave)

        assertNotNull(assessment)
        assertTrue("L'humidité à Tamatave doit être élevée (>70%)", assessment.weather.relativeHumidityPct >= 70.0)
        // High humidity must forbid aeration to prevent indoor condensation
        assertFalse("L'aération doit être interdite en air saturé maritime", assessment.aerationRecommendation.canOpenAerationNow)
    }
}
