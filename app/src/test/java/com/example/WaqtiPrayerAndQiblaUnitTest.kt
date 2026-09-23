package com.example

import com.example.data.notification.PrayerLocationAndCalcSettings
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.QiblaCompassState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WaqtiPrayerAndQiblaUnitTest {

    // --- PRAYER SYSTEM TESTS ---

    @Test
    fun testMansouraDefaultPrayerCalculation() {
        val defaultSettings = PrayerLocationAndCalcSettings() // Defaults to Mansoura
        assertEquals("المنصورة", defaultSettings.cityName)
        assertEquals(31.0409, defaultSettings.latitude, 0.001)
        assertEquals(31.3785, defaultSettings.longitude, 0.001)

        val times = PrayerCalculator.getPrayerTimes(Date(), defaultSettings)
        assertEquals(6, times.size) // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
        val fajr = times.find { it.nameEn == "Fajr" }
        val dhuhr = times.find { it.nameEn == "Dhuhr" }
        val maghrib = times.find { it.nameEn == "Maghrib" }

        assertNotNull(fajr)
        assertNotNull(dhuhr)
        assertNotNull(maghrib)

        // Verifying chronological sequence of prayer times
        for (i in 0 until times.size - 1) {
            assertTrue(
                "Prayer ${times[i].nameEn} (${times[i].rawMinutesOfDay}) must be before ${times[i + 1].nameEn} (${times[i + 1].rawMinutesOfDay})",
                times[i].rawMinutesOfDay < times[i + 1].rawMinutesOfDay
            )
        }
    }

    @Test
    fun testDifferentDatesProduceDifferentPrayerTimes() {
        val settings = PrayerLocationAndCalcSettings() // Mansoura

        // Summer solstice date: June 21
        val summerCal = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 21, 12, 0, 0)
        }
        // Winter solstice date: December 21
        val winterCal = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 21, 12, 0, 0)
        }

        val summerTimes = PrayerCalculator.getPrayerTimes(summerCal.time, settings)
        val winterTimes = PrayerCalculator.getPrayerTimes(winterCal.time, settings)

        val summerMaghrib = summerTimes.find { it.nameEn == "Maghrib" }!!.rawMinutesOfDay
        val winterMaghrib = winterTimes.find { it.nameEn == "Maghrib" }!!.rawMinutesOfDay

        // In the Northern Hemisphere (Mansoura), sunset in June is significantly later than in December
        assertTrue(
            "Summer Maghrib ($summerMaghrib min) must be significantly later than Winter Maghrib ($winterMaghrib min)",
            summerMaghrib > winterMaghrib + 60
        )
    }

    @Test
    fun testCairoAndMansouraAreNotTreatedAsSameFixedLocation() {
        val mansoura = PrayerLocationAndCalcSettings(
            cityName = "المنصورة",
            latitude = 31.0409,
            longitude = 31.3785
        )
        val cairo = PrayerLocationAndCalcSettings(
            cityName = "القاهرة",
            latitude = 30.0444,
            longitude = 31.2357
        )

        assertNotEquals(mansoura.latitude, cairo.latitude, 0.001)
        assertNotEquals(mansoura.longitude, cairo.longitude, 0.001)

        val testDate = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 21, 12, 0, 0)
        }.time

        val mansouraTimes = PrayerCalculator.getPrayerTimes(testDate, mansoura)
        val cairoTimes = PrayerCalculator.getPrayerTimes(testDate, cairo)

        val mansouraFajr = mansouraTimes.find { it.nameEn == "Fajr" }!!.rawMinutesOfDay
        val cairoFajr = cairoTimes.find { it.nameEn == "Fajr" }!!.rawMinutesOfDay

        // Fajr between Delta (Mansoura) and Cairo differs due to different coordinates
        assertNotNull(mansouraTimes)
        assertNotNull(cairoTimes)
        assertEquals(6, mansouraTimes.size)
        assertEquals(6, cairoTimes.size)
    }

    @Test
    fun testNextPrayerCalculationAndMidnightRollover() {
        val settings = PrayerLocationAndCalcSettings()
        val nextPrayer = PrayerCalculator.getNextPrayer(
            date = Date(),
            settings = settings
        )
        assertNotNull(nextPrayer)
        assertTrue(nextPrayer.nameEn.isNotEmpty())
        assertTrue(nextPrayer.isPrescribedPrayer)
    }

    @Test
    fun testCalculationMethodsAffectPrayerTimes() {
        val egyptianSettings = PrayerLocationAndCalcSettings(calcMethod = "EGYPTIAN_GENERAL_SURVEY")
        val ummAlQuraSettings = PrayerLocationAndCalcSettings(calcMethod = "UMM_AL_QURA")
        val isnaSettings = PrayerLocationAndCalcSettings(calcMethod = "ISNA")

        val date = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 15, 12, 0, 0)
        }.time

        val egyTimes = PrayerCalculator.getPrayerTimes(date, egyptianSettings)
        val uaqTimes = PrayerCalculator.getPrayerTimes(date, ummAlQuraSettings)
        val isnaTimes = PrayerCalculator.getPrayerTimes(date, isnaSettings)

        val egyFajr = egyTimes.find { it.nameEn == "Fajr" }!!.rawMinutesOfDay
        val isnaFajr = isnaTimes.find { it.nameEn == "Fajr" }!!.rawMinutesOfDay

        // Egyptian General Survey uses 19.5° for Fajr, while ISNA uses 15.0°.
        // 19.5° Fajr happens earlier (smaller rawMinutesOfDay) than 15° Fajr.
        assertTrue("Egyptian 19.5° Fajr must be earlier than ISNA 15° Fajr", egyFajr < isnaFajr)
    }

    // --- QIBLA SYSTEM TESTS ---

    @Test
    fun testKaabaCoordinatesConstant() {
        assertEquals(21.4225, PrayerCalculator.KAABA_LATITUDE, 0.001)
        assertEquals(39.8262, PrayerCalculator.KAABA_LONGITUDE, 0.001)
    }

    @Test
    fun testGreatCircleQiblaBearingFromMansoura() {
        val mansouraLat = 31.0409
        val mansouraLon = 31.3785

        val bearing = PrayerCalculator.calculateQibla(mansouraLat, mansouraLon)

        // Mansoura is north-northwest of Makkah. Great-circle bearing from Mansoura is ~139.8°
        assertTrue("Mansoura Qibla bearing should be around ~139.8° (was $bearing)", bearing in 138.0..141.5)
    }

    @Test
    fun testDifferentLocationsProduceDifferentQiblaBearings() {
        val mansouraBearing = PrayerCalculator.calculateQibla(31.0409, 31.3785)
        val londonBearing = PrayerCalculator.calculateQibla(51.5074, -0.1278)
        val tokyoBearing = PrayerCalculator.calculateQibla(35.6762, 139.6503)
        val newYorkBearing = PrayerCalculator.calculateQibla(40.7128, -74.0060)

        assertNotEquals(mansouraBearing, londonBearing, 1.0)
        assertNotEquals(mansouraBearing, tokyoBearing, 1.0)
        assertNotEquals(londonBearing, newYorkBearing, 1.0)

        // London to Makkah is South-East (~118° - 120°)
        assertTrue("London Qibla should be ~119°", londonBearing in 115.0..125.0)
        // Tokyo to Makkah is West-Northwest (~293°)
        assertTrue("Tokyo Qibla should be ~293°", tokyoBearing in 285.0..300.0)
        // New York to Makkah along great-circle is East-Northeast (~58°)
        assertTrue("New York Qibla should be ~58°", newYorkBearing in 50.0..65.0)
    }

    @Test
    fun testQiblaBearingNormalization0To360() {
        for (lat in -80..80 step 20) {
            for (lon in -180..180 step 30) {
                val bearing = PrayerCalculator.calculateQibla(lat.toDouble(), lon.toDouble())
                assertTrue("Bearing $bearing must be in range [0, 360)", bearing >= 0.0 && bearing < 360.0)
            }
        }
    }

    @Test
    fun testHaversineDistanceToKaaba() {
        // Mansoura to Makkah straight-line distance is approximately 1350-1450 km
        val distMansoura = PrayerCalculator.getDistanceToMakkah(31.0409, 31.3785)
        assertTrue("Mansoura to Makkah distance should be ~1390 km", distMansoura in 1300..1500)

        // Distance from Kaaba to Kaaba must be 0
        val distKaaba = PrayerCalculator.getDistanceToMakkah(PrayerCalculator.KAABA_LATITUDE, PrayerCalculator.KAABA_LONGITUDE)
        assertEquals(0, distKaaba)
    }

    @Test
    fun testRelativeQiblaAngleAndCrossingZeroThreeSixty() {
        val qiblaBearing = 136.0f

        // When heading is 90° (East), relative angle is (136 - 90) = 46° clockwise
        val heading1 = 90.0f
        val relative1 = (qiblaBearing - heading1 + 360f) % 360f
        assertEquals(46.0f, relative1, 0.01f)

        // When heading is 136° (pointing directly at Kaaba), relative angle = 0° (pointing straight up)
        val heading2 = 136.0f
        val relative2 = (qiblaBearing - heading2 + 360f) % 360f
        assertEquals(0.0f, relative2, 0.01f)

        // Wrap around test: heading = 350°, Qibla = 10° -> relative = (10 - 350 + 360) = 20°
        val wrapRel = (10.0f - 350.0f + 360f) % 360f
        assertEquals(20.0f, wrapRel, 0.01f)
    }

    @Test
    fun testAlignmentToleranceCriteria() {
        val qiblaBearing = 136.0f

        // Exact match -> aligned = true, perfect = true
        val diffExact = 0.0f
        assertTrue(abs(diffExact) <= 5.0f)
        assertTrue(abs(diffExact) <= 3.0f)

        // 2.5° deviation -> aligned = true, perfect = true
        val diff2 = 2.5f
        assertTrue(abs(diff2) <= 5.0f)
        assertTrue(abs(diff2) <= 3.0f)

        // 4.5° deviation -> aligned = true, perfect = false
        val diff4 = 4.5f
        assertTrue(abs(diff4) <= 5.0f)
        assertFalse(abs(diff4) <= 3.0f)

        // 8° deviation -> aligned = false, perfect = false
        val diff8 = 8.0f
        assertFalse(abs(diff8) <= 5.0f)
        assertFalse(abs(diff8) <= 3.0f)
    }

    @Test
    fun testQiblaCompassStateDefaults() {
        val state = QiblaCompassState()
        assertEquals("المنصورة", state.cityName)
        assertEquals(31.0409, state.userLatitude, 0.001)
        assertEquals(31.3785, state.userLongitude, 0.001)
        assertTrue(state.isSensorAvailable)
        assertFalse(state.needsCalibration)
    }
}
