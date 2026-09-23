package com.example.data.prayer

/**
 * Standardized enum representing the five prescribed daily prayers.
 * Ensures robust identification throughout scheduling, notifications, and Adhan playback.
 */
enum class WaqtiPrayer(val id: String, val nameEn: String, val nameAr: String) {
    FAJR("fajr", "Fajr", "الفجر"),
    DHUHR("dhuhr", "Dhuhr", "الظهر"),
    ASR("asr", "Asr", "العصر"),
    MAGHRIB("maghrib", "Maghrib", "المغرب"),
    ISHA("isha", "Isha", "العشاء");

    companion object {
        fun fromString(str: String?): WaqtiPrayer? {
            if (str == null) return null
            val clean = str.trim().lowercase()
            return entries.find {
                it.id == clean ||
                it.nameEn.lowercase() == clean ||
                it.nameAr == str.trim() ||
                str.contains(it.nameAr) ||
                str.contains(it.nameEn, ignoreCase = true)
            }
        }
    }
}
