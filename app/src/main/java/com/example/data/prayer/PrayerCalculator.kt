package com.example.data.prayer

import android.util.Log
import com.example.data.notification.PrayerLocationAndCalcSettings
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Representation of a calculated prayer time.
 */
data class PrayerTime(
    val nameEn: String,
    val nameAr: String,
    val timeFormatted: String, // "05:05" or "5:05 AM"
    val isPast: Boolean,
    val isNext: Boolean,
    val minutesUntil: Int,
    val rawMinutesOfDay: Int,
    val isPrescribedPrayer: Boolean = true, // False for Sunrise
    val isTomorrow: Boolean = false
)

/**
 * Single Source of Truth for Prayer Times, Solar Positioning, and Qibla calculations in WAQTI.
 *
 * Default Location:
 * City: Mansoura (المنصورة)
 * Governorate: Dakahlia (الدقهلية)
 * Country: Egypt (مصر)
 * Latitude: 31.0409, Longitude: 31.3785
 *
 * Kaaba Coordinates:
 * Latitude: 21.422487, Longitude: 39.826206
 */
object PrayerCalculator {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    // Default city preset: Mansoura, Dakahlia, Egypt
    val DEFAULT_CITY = CityPreset(
        nameAr = "المنصورة، الدقهلية، مصر",
        nameEn = "Mansoura, Dakahlia, Egypt",
        cityNameAr = "المنصورة",
        cityNameEn = "Mansoura",
        governorateAr = "الدقهلية",
        governorateEn = "Dakahlia",
        countryAr = "مصر",
        countryEn = "Egypt",
        latitude = 31.0409,
        longitude = 31.3785,
        gmtOffset = 2.0
    )

    // Supported presets for manual location selection. Mansoura is first and default.
    val supportedCities = listOf(
        DEFAULT_CITY,
        CityPreset("القاهرة، مصر", "Cairo, Egypt", "القاهرة", "Cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, 2.0),
        CityPreset("الإسكندرية، مصر", "Alexandria, Egypt", "الإسكندرية", "Alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187, 2.0),
        CityPreset("طنطا، الغربية، مصر", "Tanta, Gharbia, Egypt", "طنطا", "Tanta", "الغربية", "Gharbia", "مصر", "Egypt", 30.7865, 31.0004, 2.0),
        CityPreset("الزقازيق، الشرقية، مصر", "Zagazig, Sharqia, Egypt", "الزقازيق", "Zagazig", "الشرقية", "Sharqia", "مصر", "Egypt", 30.5877, 31.5020, 2.0),
        CityPreset("دمياط، مصر", "Damietta, Egypt", "دمياط", "Damietta", "دمياط", "Damietta", "مصر", "Egypt", 31.4175, 31.8144, 2.0),
        CityPreset("بورسعيد، مصر", "Port Said, Egypt", "بورسعيد", "Port Said", "بورسعيد", "Port Said", "مصر", "Egypt", 31.2653, 32.3019, 2.0),
        CityPreset("السويس، مصر", "Suez, Egypt", "السويس", "Suez", "السويس", "Suez", "مصر", "Egypt", 29.9668, 32.5498, 2.0),
        CityPreset("الإسماعيلية، مصر", "Ismailia, Egypt", "الإسماعيلية", "Ismailia", "الإسماعيلية", "Ismailia", "مصر", "Egypt", 30.5965, 32.2715, 2.0),
        CityPreset("الفيوم، مصر", "Faiyum, Egypt", "الفيوم", "Faiyum", "الفيوم", "Faiyum", "مصر", "Egypt", 29.3084, 30.8428, 2.0),
        CityPreset("بني سويف، مصر", "Beni Suef, Egypt", "بني سويف", "Beni Suef", "بني سويف", "Beni Suef", "مصر", "Egypt", 29.0661, 31.0994, 2.0),
        CityPreset("المنيا، مصر", "Minya, Egypt", "المنيا", "Minya", "المنيا", "Minya", "مصر", "Egypt", 28.0871, 30.7618, 2.0),
        CityPreset("أسيوط، مصر", "Asyut, Egypt", "أسيوط", "Asyut", "أسيوط", "Asyut", "مصر", "Egypt", 27.1801, 31.1837, 2.0),
        CityPreset("سوهاج، مصر", "Sohag, Egypt", "سوهاج", "Sohag", "سوهاج", "Sohag", "مصر", "Egypt", 26.5570, 31.6948, 2.0),
        CityPreset("قنا، مصر", "Qena, Egypt", "قنا", "Qena", "قنا", "Qena", "مصر", "Egypt", 26.1551, 32.7160, 2.0),
        CityPreset("الأقصر، مصر", "Luxor, Egypt", "الأقصر", "Luxor", "الأقصر", "Luxor", "مصر", "Egypt", 25.6872, 32.6396, 2.0),
        CityPreset("أسوان، مصر", "Aswan, Egypt", "أسوان", "Aswan", "أسوان", "Aswan", "مصر", "Egypt", 24.0889, 32.8998, 2.0),
        CityPreset("الغردقة، البحر الأحمر، مصر", "Hurghada, Egypt", "الغردقة", "Hurghada", "البحر الأحمر", "Red Sea", "مصر", "Egypt", 27.2579, 33.8116, 2.0),
        CityPreset("شرم الشيخ، جنوب سيناء، مصر", "Sharm El Sheikh, Egypt", "شرم الشيخ", "Sharm El Sheikh", "جنوب سيناء", "South Sinai", "مصر", "Egypt", 27.9158, 34.3299, 2.0),
        CityPreset("مرسى مطروح، مصر", "Marsa Matruh, Egypt", "مرسى مطروح", "Marsa Matruh", "مطروح", "Matruh", "مصر", "Egypt", 31.3543, 27.2373, 2.0),
        CityPreset("مكة المكرمة، السعودية", "Makkah, Saudi Arabia", "مكة المكرمة", "Makkah", "منطقة مكة", "Makkah Region", "السعودية", "Saudi Arabia", 21.3891, 39.8579, 3.0),
        CityPreset("المدينة المنورة، السعودية", "Madinah, Saudi Arabia", "المدينة المنورة", "Madinah", "منطقة المدينة", "Madinah Region", "السعودية", "Saudi Arabia", 24.5247, 39.5692, 3.0),
        CityPreset("القدس الشريف، فلسطين", "Jerusalem, Palestine", "القدس الشريف", "Jerusalem", "القدس", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137, 2.0),
        CityPreset("الرياض، السعودية", "Riyadh, Saudi Arabia", "الرياض", "Riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753, 3.0),
        CityPreset("دبي، الإمارات", "Dubai, UAE", "دبي", "Dubai", "دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, 4.0),
        CityPreset("إسطنبول، تركيا", "Istanbul, Turkey", "إسطنبول", "Istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, 3.0),
        CityPreset("لندن، المملكة المتحدة", "London, UK", "لندن", "London", "إنجلترا", "England", "المملكة المتحدة", "UK", 51.5074, -0.1278, 0.0),
        CityPreset("نيويورك، الولايات المتحدة", "New York, USA", "نيويورك", "New York", "نيويورك", "New York", "الولايات المتحدة", "USA", 40.7128, -74.0060, -5.0)
    )

    data class CityPreset(
        val nameAr: String,
        val nameEn: String,
        val cityNameAr: String,
        val cityNameEn: String,
        val governorateAr: String,
        val governorateEn: String,
        val countryAr: String,
        val countryEn: String,
        val latitude: Double,
        val longitude: Double,
        val gmtOffset: Double
    )

    fun findNearestCity(lat: Double, lon: Double): CityPreset? {
        return supportedCities.minByOrNull { city ->
            val dLat = city.latitude - lat
            val dLon = city.longitude - lon
            dLat * dLat + dLon * dLon
        }
    }

    /**
     * Dynamically calculates prayer times for the specified date and location settings using
     * standard astronomical solar positioning algorithms.
     *
     * Calculates:
     * - Fajr (Prescribed Prayer)
     * - Sunrise (Astronomical event, isPrescribedPrayer = false)
     * - Dhuhr (Prescribed Prayer)
     * - Asr (Prescribed Prayer)
     * - Maghrib (Prescribed Prayer)
     * - Isha (Prescribed Prayer)
     */
    fun getPrayerTimes(
        date: Date = Date(),
        settings: PrayerLocationAndCalcSettings = PrayerLocationAndCalcSettings()
    ): List<PrayerTime> {
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val now = Calendar.getInstance()
        val isToday = (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))
        val currentMinutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val lat = settings.latitude
        val lon = settings.longitude
        val latRad = Math.toRadians(lat)

        // TimeZone offset in hours for the given date (accounts for standard and daylight saving time)
        val tz = TimeZone.getDefault()
        val tzOffsetHours = tz.getOffset(date.time) / 3600000.0

        // 1. Julian Day and Solar Coordinates
        val jd = getJulianDay(year, month, day)
        val d = jd - 2451545.0

        // Mean anomaly of the Sun (degrees)
        val gDeg = (357.529 + 0.98560028 * d) % 360.0
        val gRad = Math.toRadians(if (gDeg < 0) gDeg + 360.0 else gDeg)

        // Mean longitude of the Sun (degrees)
        val qDeg = (280.459 + 0.98564736 * d) % 360.0
        val qNorm = if (qDeg < 0) qDeg + 360.0 else qDeg

        // Ecliptic longitude of the Sun (degrees)
        val lDeg = (qNorm + 1.915 * sin(gRad) + 0.020 * sin(2.0 * gRad)) % 360.0
        val lRad = Math.toRadians(if (lDeg < 0) lDeg + 360.0 else lDeg)

        // Obliquity of the ecliptic (degrees)
        val eDeg = 23.439 - 0.00000036 * d
        val eRad = Math.toRadians(eDeg)

        // Sun's declination delta (radians)
        val sinDelta = sin(eRad) * sin(lRad)
        val deltaRad = asin(sinDelta.coerceIn(-1.0, 1.0))

        // Right ascension (degrees)
        val raY = cos(eRad) * sin(lRad)
        val raX = cos(lRad)
        val raDeg = (Math.toDegrees(atan2(raY, raX)) + 360.0) % 360.0
        val raHours = raDeg / 15.0

        // Equation of Time in hours
        var eqtHours = (qNorm / 15.0) - raHours
        while (eqtHours > 12.0) eqtHours -= 24.0
        while (eqtHours < -12.0) eqtHours += 24.0

        // 2. Solar Noon (Dhuhr) in local standard time
        val solarNoonHours = 12.0 + tzOffsetHours - (lon / 15.0) - eqtHours

        // Precautionary 2-minute delay for Dhuhr
        val dhuhrHours = solarNoonHours + (2.0 / 60.0) + (settings.dhuhrOffset / 60.0)

        // 3. Calculation parameters per method
        // Egyptian General Authority of Survey (الهيئة المصرية العامة للمساحة): Fajr 19.5°, Isha 17.5°
        val methodKey = if (settings.calcMethod.isNotEmpty()) settings.calcMethod else settings.calculationMethod
        val (fajrAngleDeg, ishaAngleDeg, ishaFixedMinsAfterMaghrib) = when (methodKey) {
            "UMM_AL_QURA" -> Triple(18.5, 0.0, 90) // 90 mins after Maghrib
            "MWL" -> Triple(18.0, 17.0, null)
            "ISNA" -> Triple(15.0, 15.0, null)
            else -> Triple(19.5, 17.5, null) // EGYPTIAN_GENERAL_SURVEY (Default)
        }

        // Fajr hour angle
        val fajrHA = calculateHourAngle(-fajrAngleDeg, latRad, deltaRad) ?: 1.5
        val fajrHours = solarNoonHours - fajrHA + (settings.fajrOffset / 60.0)

        // Sunrise (sun altitude -0.8333 degrees: 50 arcminutes refraction & disk radius)
        val sunriseHA = calculateHourAngle(-0.8333, latRad, deltaRad) ?: 1.0
        val sunriseHours = solarNoonHours - sunriseHA

        // Asr: juristic method: Standard (Shafi'i/Maliki/Hanbali) = 1, Hanafi = 2
        val asrFactor = if (settings.asrMethod == "HANAFI") 2.0 else 1.0
        val asrAltRad = atan2(1.0, asrFactor + tan(kotlin.math.abs(latRad - deltaRad)))
        val asrAltDeg = Math.toDegrees(asrAltRad)
        val asrHA = calculateHourAngle(asrAltDeg, latRad, deltaRad) ?: 2.5
        val asrHours = solarNoonHours + asrHA + (settings.asrOffset / 60.0)

        // Maghrib (Sunset: sun altitude -0.8333 degrees)
        val sunsetHA = calculateHourAngle(-0.8333, latRad, deltaRad) ?: 1.0
        val maghribHours = solarNoonHours + sunsetHA + (settings.maghribOffset / 60.0)

        // Isha
        val ishaHours = if (ishaFixedMinsAfterMaghrib != null) {
            maghribHours + (ishaFixedMinsAfterMaghrib / 60.0) + (settings.ishaOffset / 60.0)
        } else {
            val ishaHA = calculateHourAngle(-ishaAngleDeg, latRad, deltaRad) ?: 1.5
            solarNoonHours + ishaHA + (settings.ishaOffset / 60.0)
        }

        val rawFajr = toMinutes(fajrHours)
        val rawSunrise = toMinutes(sunriseHours)
        val rawDhuhr = toMinutes(dhuhrHours)
        val rawAsr = toMinutes(asrHours)
        val rawMaghrib = toMinutes(maghribHours)
        val rawIsha = toMinutes(ishaHours)

        val rawList = listOf(
            PrayerRawData("Fajr", "الفجر", rawFajr, true),
            PrayerRawData("Sunrise", "الشروق", rawSunrise, false),
            PrayerRawData("Dhuhr", "الظهر", rawDhuhr, true),
            PrayerRawData("Asr", "العصر", rawAsr, true),
            PrayerRawData("Maghrib", "المغرب", rawMaghrib, true),
            PrayerRawData("Isha", "العشاء", rawIsha, true)
        )

        // Determine next prescribed prayer
        var nextFound = false

        val results = rawList.map { item ->
            val formatted = formatMinutes(item.rawMinutes, settings.is24HourFormat)
            val isPast = if (isToday) item.rawMinutes < currentMinutesOfDay else false
            val isNext = if (isToday && item.isPrescribed && !isPast && !nextFound) {
                nextFound = true
                true
            } else false

            val minutesUntil = if (isToday) {
                if (item.rawMinutes >= currentMinutesOfDay) {
                    item.rawMinutes - currentMinutesOfDay
                } else {
                    item.rawMinutes + 1440 - currentMinutesOfDay
                }
            } else {
                item.rawMinutes
            }

            PrayerTime(
                nameEn = item.nameEn,
                nameAr = item.nameAr,
                timeFormatted = formatted,
                isPast = isPast,
                isNext = isNext,
                minutesUntil = minutesUntil,
                rawMinutesOfDay = item.rawMinutes,
                isPrescribedPrayer = item.isPrescribed
            )
        }

        Log.d(
            "WAQTI_PRAYER_DEBUG",
            "Calculated dynamic prayer times for Date: $date | Location: ${settings.cityName} (Lat: $lat, Lon: $lon) | Fajr: ${results[0].timeFormatted}, Dhuhr: ${results[2].timeFormatted}, Asr: ${results[3].timeFormatted}, Maghrib: ${results[4].timeFormatted}, Isha: ${results[5].timeFormatted}"
        )

        return results
    }

    /**
     * Dynamically identifies the upcoming prayer.
     * Seamlessly handles:
     * - Daytime prayers
     * - Midnight crossing
     * - After Isha: switches dynamically to Tomorrow's Fajr
     */
    fun getNextPrayer(
        date: Date = Date(),
        settings: PrayerLocationAndCalcSettings = PrayerLocationAndCalcSettings()
    ): PrayerTime {
        val todayTimes = getPrayerTimes(date, settings)
        val nextInToday = todayTimes.firstOrNull { it.isPrescribedPrayer && it.isNext }
        if (nextInToday != null) {
            return nextInToday
        }

        // If no upcoming prayer remains today (i.e. current time is past Isha), switch to Tomorrow's Fajr!
        val tomorrowCal = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimes = getPrayerTimes(tomorrowCal.time, settings)
        val tomorrowFajr = tomorrowTimes.first { it.nameEn == "Fajr" }

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val minutesUntilTomorrowFajr = (1440 - currentMinutes) + tomorrowFajr.rawMinutesOfDay

        val nextTomorrow = PrayerTime(
            nameEn = tomorrowFajr.nameEn,
            nameAr = tomorrowFajr.nameAr,
            timeFormatted = tomorrowFajr.timeFormatted,
            isPast = false,
            isNext = true,
            minutesUntil = minutesUntilTomorrowFajr,
            rawMinutesOfDay = tomorrowFajr.rawMinutesOfDay,
            isPrescribedPrayer = true,
            isTomorrow = true
        )

        Log.d(
            "WAQTI_PRAYER_DEBUG",
            "Current time is after Isha. Next prayer switched to Tomorrow's Fajr: ${nextTomorrow.timeFormatted} (in $minutesUntilTomorrowFajr mins)"
        )

        return nextTomorrow
    }

    /**
     * Calculates the great-circle initial bearing (0° = True North, clockwise to 360°)
     * from any user coordinates to the Holy Kaaba in Makkah (21.422487° N, 39.826206° E).
     *
     * Mathematically exact spherical trigonometry:
     * Δλ = λ_kaaba - λ_user
     * y = sin(Δλ) * cos(φ_kaaba)
     * x = cos(φ_user) * sin(φ_kaaba) - sin(φ_user) * cos(φ_kaaba) * cos(Δλ)
     * θ = atan2(y, x)
     */
    fun calculateQibla(userLat: Double, userLon: Double): Double {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLon)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        val initialBearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(initialBearingRad)
        val normalized = (bearingDeg + 360.0) % 360.0

        Log.d(
            "WAQTI_QIBLA_DEBUG",
            "Calculated Qibla bearing from ($userLat, $userLon) to Kaaba ($KAABA_LATITUDE, $KAABA_LONGITUDE): $normalized°"
        )

        return normalized
    }

    /**
     * Calculates distance to the Holy Kaaba in kilometers using the Haversine geodesic formula.
     */
    fun getDistanceToMakkah(userLat: Double, userLon: Double): Int {
        val earthRadiusKm = 6371.0
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaPhi = Math.toRadians(KAABA_LATITUDE - userLat)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - userLon)

        val a = sin(deltaPhi / 2.0) * sin(deltaPhi / 2.0) +
                cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0) * sin(deltaLambda / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return (earthRadiusKm * c).roundToInt()
    }

    // --- Private Astronomical Helpers ---

    private data class PrayerRawData(
        val nameEn: String,
        val nameAr: String,
        val rawMinutes: Int,
        val isPrescribed: Boolean
    )

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                day.toDouble() + b - 1524.5
    }

    private fun calculateHourAngle(altitudeDeg: Double, latRad: Double, deltaRad: Double): Double? {
        val altRad = Math.toRadians(altitudeDeg)
        val sinAlt = sin(altRad)
        val sinLat = sin(latRad)
        val cosLat = cos(latRad)
        val sinDelta = sin(deltaRad)
        val cosDelta = cos(deltaRad)

        val cosH = (sinAlt - sinLat * sinDelta) / (cosLat * cosDelta)
        if (cosH < -1.0 || cosH > 1.0) return null
        return Math.toDegrees(acos(cosH)) / 15.0
    }

    private fun toMinutes(hours: Double): Int {
        val totalMins = (hours * 60.0).roundToInt()
        return (totalMins % 1440 + 1440) % 1440
    }

    private fun formatMinutes(rawMinutes: Int, is24Hour: Boolean): String {
        val hour = rawMinutes / 60
        val min = rawMinutes % 60
        return if (is24Hour) {
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
    }
}
