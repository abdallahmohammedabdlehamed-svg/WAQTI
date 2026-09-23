package com.example.data.prayer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.notification.PrayerLocationAndCalcSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistent preferences for WAQTI location and prayer calculation configurations.
 * Guarantees that the selected location persists across app restarts, reboots, and updates.
 *
 * Primary default:
 * City: Mansoura (المنصورة)
 * Governorate: Dakahlia (الدقهلية)
 * Country: Egypt (مصر)
 * Latitude: 31.0409, Longitude: 31.3785
 */
class WaqtiLocationPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<PrayerLocationAndCalcSettings> = _settingsFlow.asStateFlow()

    init {
        logCurrentLocation("Initial load")
    }

    fun getSettings(): PrayerLocationAndCalcSettings {
        return _settingsFlow.value
    }

    fun saveSettings(settings: PrayerLocationAndCalcSettings) {
        prefs.edit().apply {
            putBoolean(KEY_AUTO_LOCATION, settings.isAutoLocation)
            putString(KEY_COUNTRY_FULL, settings.country)
            putString(KEY_COUNTRY_NAME, settings.countryName)
            putString(KEY_GOVERNORATE, settings.governorate)
            putString(KEY_CITY_NAME, settings.cityName)
            putString(KEY_CITY_FULL, settings.city)
            putFloat(KEY_LATITUDE, settings.latitude.toFloat())
            putFloat(KEY_LONGITUDE, settings.longitude.toFloat())
            putString(KEY_CALC_METHOD, settings.calculationMethod)
            putString(KEY_ASR_METHOD, settings.asrMethod)
            putInt(KEY_FAJR_OFFSET, settings.fajrOffset)
            putInt(KEY_DHUHR_OFFSET, settings.dhuhrOffset)
            putInt(KEY_ASR_OFFSET, settings.asrOffset)
            putInt(KEY_MAGHRIB_OFFSET, settings.maghribOffset)
            putInt(KEY_ISHA_OFFSET, settings.ishaOffset)
            putBoolean(KEY_FAJR_ENABLED, settings.fajrEnabled)
            putBoolean(KEY_DHUHR_ENABLED, settings.dhuhrEnabled)
            putBoolean(KEY_ASR_ENABLED, settings.asrEnabled)
            putBoolean(KEY_MAGHRIB_ENABLED, settings.maghribEnabled)
            putBoolean(KEY_ISHA_ENABLED, settings.ishaEnabled)
            putBoolean(KEY_24_HOUR_FORMAT, settings.is24HourFormat)
            putString(KEY_LOCATION_SOURCE, settings.locationSource)
            apply()
        }
        _settingsFlow.value = settings
        logCurrentLocation("Settings updated and persisted")
    }

    fun updateLocation(
        cityNameAr: String,
        cityNameEn: String,
        governorateAr: String,
        governorateEn: String,
        countryAr: String,
        countryEn: String,
        latitude: Double,
        longitude: Double,
        source: String = "MANUAL",
        isArabic: Boolean = true
    ) {
        val current = _settingsFlow.value
        val newSettings = current.copy(
            isAutoLocation = (source == "GPS"),
            cityName = if (isArabic) cityNameAr else cityNameEn,
            governorate = if (isArabic) governorateAr else governorateEn,
            countryName = if (isArabic) countryAr else countryEn,
            city = if (isArabic) "$cityNameAr، $governorateAr" else "$cityNameEn, $governorateEn",
            country = if (isArabic) "$countryAr ($countryEn)" else "$countryEn ($countryAr)",
            latitude = latitude,
            longitude = longitude,
            locationSource = source
        )
        saveSettings(newSettings)
    }

    fun resetToDefaultMansoura() {
        val defaultSettings = PrayerLocationAndCalcSettings(
            isAutoLocation = true,
            country = "مصر (Egypt)",
            countryName = "مصر",
            governorate = "الدقهلية",
            cityName = "المنصورة",
            city = "المنصورة، الدقهلية",
            latitude = 31.0409,
            longitude = 31.3785,
            calcMethod = "EGYPTIAN_GENERAL_SURVEY",
            calculationMethod = "EGYPTIAN_GENERAL_SURVEY",
            asrMethod = "STANDARD",
            locationSource = "DEFAULT"
        )
        saveSettings(defaultSettings)
        logCurrentLocation("Reset to default Mansoura")
    }

    private fun loadSettings(): PrayerLocationAndCalcSettings {
        val isAuto = prefs.getBoolean(KEY_AUTO_LOCATION, true)
        val country = prefs.getString(KEY_COUNTRY_FULL, "مصر (Egypt)") ?: "مصر (Egypt)"
        val countryName = prefs.getString(KEY_COUNTRY_NAME, "مصر") ?: "مصر"
        val governorate = prefs.getString(KEY_GOVERNORATE, "الدقهلية") ?: "الدقهلية"
        val cityName = prefs.getString(KEY_CITY_NAME, "المنصورة") ?: "المنصورة"
        val city = prefs.getString(KEY_CITY_FULL, "المنصورة، الدقهلية") ?: "المنصورة، الدقهلية"
        val latitude = prefs.getFloat(KEY_LATITUDE, 31.0409f).toDouble()
        val longitude = prefs.getFloat(KEY_LONGITUDE, 31.3785f).toDouble()
        val calcMethod = prefs.getString(KEY_CALC_METHOD, "EGYPTIAN_GENERAL_SURVEY") ?: "EGYPTIAN_GENERAL_SURVEY"
        val asrMethod = prefs.getString(KEY_ASR_METHOD, "STANDARD") ?: "STANDARD"
        val fajrOffset = prefs.getInt(KEY_FAJR_OFFSET, 0)
        val dhuhrOffset = prefs.getInt(KEY_DHUHR_OFFSET, 0)
        val asrOffset = prefs.getInt(KEY_ASR_OFFSET, 0)
        val maghribOffset = prefs.getInt(KEY_MAGHRIB_OFFSET, 0)
        val ishaOffset = prefs.getInt(KEY_ISHA_OFFSET, 0)
        val fajrEnabled = prefs.getBoolean(KEY_FAJR_ENABLED, true)
        val dhuhrEnabled = prefs.getBoolean(KEY_DHUHR_ENABLED, true)
        val asrEnabled = prefs.getBoolean(KEY_ASR_ENABLED, true)
        val maghribEnabled = prefs.getBoolean(KEY_MAGHRIB_ENABLED, true)
        val ishaEnabled = prefs.getBoolean(KEY_ISHA_ENABLED, true)
        val is24h = prefs.getBoolean(KEY_24_HOUR_FORMAT, false)
        val source = prefs.getString(KEY_LOCATION_SOURCE, "DEFAULT") ?: "DEFAULT"

        return PrayerLocationAndCalcSettings(
            isAutoLocation = isAuto,
            country = country,
            countryName = countryName,
            governorate = governorate,
            cityName = cityName,
            city = city,
            latitude = latitude,
            longitude = longitude,
            calcMethod = calcMethod,
            calculationMethod = calcMethod,
            asrMethod = asrMethod,
            fajrOffset = fajrOffset,
            dhuhrOffset = dhuhrOffset,
            asrOffset = asrOffset,
            maghribOffset = maghribOffset,
            ishaOffset = ishaOffset,
            fajrEnabled = fajrEnabled,
            dhuhrEnabled = dhuhrEnabled,
            asrEnabled = asrEnabled,
            maghribEnabled = maghribEnabled,
            ishaEnabled = ishaEnabled,
            is24HourFormat = is24h,
            locationSource = source
        )
    }

    private fun logCurrentLocation(tag: String) {
        val s = _settingsFlow.value
        Log.d(
            "WAQTI_PRAYER_DEBUG",
            "[$tag] Location: ${s.cityName}, ${s.governorate}, ${s.countryName} | Lat: ${s.latitude}, Lon: ${s.longitude} | Source: ${s.locationSource} | Method: ${s.calculationMethod}"
        )
    }

    companion object {
        private const val PREFS_NAME = "waqti_location_preferences"
        private const val KEY_AUTO_LOCATION = "key_is_auto_location"
        private const val KEY_COUNTRY_FULL = "key_country_full"
        private const val KEY_COUNTRY_NAME = "key_country_name"
        private const val KEY_GOVERNORATE = "key_governorate"
        private const val KEY_CITY_NAME = "key_city_name"
        private const val KEY_CITY_FULL = "key_city_full"
        private const val KEY_LATITUDE = "key_latitude"
        private const val KEY_LONGITUDE = "key_longitude"
        private const val KEY_CALC_METHOD = "key_calc_method"
        private const val KEY_ASR_METHOD = "key_asr_method"
        private const val KEY_FAJR_OFFSET = "key_fajr_offset"
        private const val KEY_DHUHR_OFFSET = "key_dhuhr_offset"
        private const val KEY_ASR_OFFSET = "key_asr_offset"
        private const val KEY_MAGHRIB_OFFSET = "key_maghrib_offset"
        private const val KEY_ISHA_OFFSET = "key_isha_offset"
        private const val KEY_FAJR_ENABLED = "key_fajr_enabled"
        private const val KEY_DHUHR_ENABLED = "key_dhuhr_enabled"
        private const val KEY_ASR_ENABLED = "key_asr_enabled"
        private const val KEY_MAGHRIB_ENABLED = "key_maghrib_enabled"
        private const val KEY_ISHA_ENABLED = "key_isha_enabled"
        private const val KEY_24_HOUR_FORMAT = "key_24_hour_format"
        private const val KEY_LOCATION_SOURCE = "key_location_source"

        @Volatile
        private var INSTANCE: WaqtiLocationPreferences? = null

        fun getInstance(context: Context): WaqtiLocationPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaqtiLocationPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
