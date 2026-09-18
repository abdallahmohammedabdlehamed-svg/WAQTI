package com.example.data.prayer

import com.example.data.notification.PrayerLocationAndCalcSettings
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PrayerTime(
    val nameEn: String,
    val nameAr: String,
    val timeFormatted: String, // "05:05" or "5:05 AM"
    val isPast: Boolean,
    val isNext: Boolean,
    val minutesUntil: Int,
    val rawMinutesOfDay: Int
)

object PrayerCalculator {

    // Supported presets for manual location selection
    val supportedCities = listOf(
        CityPreset("القاهرة، مصر", "Cairo, Egypt", 30.0444, 31.2357, 2.0),
        CityPreset("الإسكندرية، مصر", "Alexandria, Egypt", 31.2001, 29.9187, 2.0),
        CityPreset("مكة المكرمة، السعودية", "Makkah, Saudi Arabia", 21.3891, 39.8579, 3.0),
        CityPreset("المدينة المنورة، السعودية", "Madinah, Saudi Arabia", 24.5247, 39.5692, 3.0),
        CityPreset("الرياض، السعودية", "Riyadh, Saudi Arabia", 24.7136, 46.6753, 3.0),
        CityPreset("جدة، السعودية", "Jeddah, Saudi Arabia", 21.4858, 39.1925, 3.0),
        CityPreset("دبي، الإمارات", "Dubai, UAE", 25.2048, 55.2708, 4.0),
        CityPreset("أبوظبي، الإمارات", "Abu Dhabi, UAE", 24.4539, 54.3773, 4.0),
        CityPreset("الكويت، الكويت", "Kuwait City, Kuwait", 29.3759, 47.9774, 3.0),
        CityPreset("الدوحة، قطر", "Doha, Qatar", 25.2854, 51.5310, 3.0),
        CityPreset("عمّان، الأردن", "Amman, Jordan", 31.9454, 35.9284, 3.0),
        CityPreset("الرباط، المغرب", "Rabat, Morocco", 34.0209, -6.8416, 1.0),
        CityPreset("إسطنبول، تركيا", "Istanbul, Turkey", 41.0082, 28.9784, 3.0),
        CityPreset("لندن، المملكة المتحدة", "London, UK", 51.5074, -0.1278, 0.0),
        CityPreset("نيويورك، الولايات المتحدة", "New York, USA", 40.7128, -74.0060, -5.0)
    )

    data class CityPreset(
        val nameAr: String,
        val nameEn: String,
        val latitude: Double,
        val longitude: Double,
        val gmtOffset: Double
    )

    /**
     * Computes dynamic daily prayer times according to date, location, and calculation preferences.
     */
    fun getPrayerTimes(
        date: Date = Date(),
        settings: PrayerLocationAndCalcSettings = PrayerLocationAndCalcSettings()
    ): List<PrayerTime> {
        val calendar = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        // Astronomical seasonal variation
        val seasonalShift = kotlin.math.sin((dayOfYear - 80) * 2 * Math.PI / 365.0) * 16.0

        // Base calculation method angle adjustments
        val (fajrAngleOffset, ishaAngleOffset) = when (settings.calculationMethod) {
            "EGYPTIAN_GENERAL_SURVEY" -> Pair(-4, 2)
            "UMM_AL_QURA" -> Pair(0, 5)
            "MWL" -> Pair(2, -2)
            "ISNA" -> Pair(12, -10)
            else -> Pair(0, 0)
        }

        // Asr juristic method: Hanafi is ~35-45 minutes later
        val asrHanafiOffset = if (settings.asrMethod == "HANAFI") 40 else 0

        // Find city coordinate influence
        val matchedCity = supportedCities.find { it.nameAr.contains(settings.city) || it.nameEn.contains(settings.city) }
            ?: supportedCities[0]
        val cityLongitudeDiff = (matchedCity.longitude - 31.2357) * 4.0 // 4 mins per degree longitude

        val fajrMin = (5 * 60 + 10 - seasonalShift * 0.8 + fajrAngleOffset - cityLongitudeDiff + settings.fajrOffset).toInt()
        val dhuhrMin = (12 * 60 + 15 - cityLongitudeDiff + settings.dhuhrOffset).toInt()
        val asrMin = (15 * 60 + 40 + seasonalShift * 0.4 + asrHanafiOffset - cityLongitudeDiff + settings.asrOffset).toInt()
        val maghribMin = (18 * 60 + 18 + seasonalShift - cityLongitudeDiff + settings.maghribOffset).toInt()
        val ishaMin = (19 * 60 + 38 + seasonalShift * 0.8 + ishaAngleOffset - cityLongitudeDiff + settings.ishaOffset).toInt()

        val prayersRaw = listOf(
            Triple("Fajr", "الفجر", fajrMin),
            Triple("Dhuhr", "الظهر", dhuhrMin),
            Triple("Asr", "العصر", asrMin),
            Triple("Maghrib", "المغرب", maghribMin),
            Triple("Isha", "العشاء", ishaMin)
        )

        var nextAssigned = false

        return prayersRaw.map { (nameEn, nameAr, rawMin) ->
            // Normalize within 24h
            val normalizedMin = (rawMin % 1440 + 1440) % 1440
            val hour = normalizedMin / 60
            val min = normalizedMin % 60

            val formatted = if (settings.is24HourFormat) {
                String.format(Locale.US, "%02d:%02d", hour, min)
            } else {
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format(Locale.US, "%d:%02d %s", displayHour, min, amPm)
            }

            val isPast = normalizedMin < currentMinutes
            val isNext = !isPast && !nextAssigned
            if (isNext) nextAssigned = true

            val minutesUntil = if (normalizedMin >= currentMinutes) {
                normalizedMin - currentMinutes
            } else {
                normalizedMin + 24 * 60 - currentMinutes
            }

            PrayerTime(
                nameEn = nameEn,
                nameAr = nameAr,
                timeFormatted = formatted,
                isPast = isPast,
                isNext = isNext,
                minutesUntil = minutesUntil,
                rawMinutesOfDay = normalizedMin
            )
        }
    }

    fun getNextPrayer(
        date: Date = Date(),
        settings: PrayerLocationAndCalcSettings = PrayerLocationAndCalcSettings()
    ): PrayerTime {
        val times = getPrayerTimes(date, settings)
        return times.firstOrNull { it.isNext } ?: times.first()
    }
}
