package com.example.data.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.notification.PrayerLocationAndCalcSettings
import java.util.Locale

/**
 * Handles device location acquisition respecting the priority:
 * 1. Current GPS/device location if permission is granted.
 * 2. User-selected city/location saved in settings.
 * 3. Default location: Mansoura, Dakahlia, Egypt.
 *
 * Never silently falls back to Cairo.
 */
class WaqtiLocationManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val locationPrefs = WaqtiLocationPreferences.getInstance(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Resolves the effective location according to the 3-tier priority rule:
     * 1. GPS if available and permitted
     * 2. Saved user selection
     * 3. Mansoura, Dakahlia, Egypt (Default)
     */
    fun getEffectiveLocationSettings(): PrayerLocationAndCalcSettings {
        val saved = locationPrefs.getSettings()

        if (saved.isAutoLocation && hasLocationPermission()) {
            val lastLoc = getLastKnownLocation()
            if (lastLoc != null) {
                Log.d(
                    "WAQTI_PRAYER_DEBUG",
                    "Resolved location from device GPS: Lat ${lastLoc.latitude}, Lon ${lastLoc.longitude}"
                )
                // If saved is already up-to-date with this GPS, return it, else update
                val latDiff = kotlin.math.abs(saved.latitude - lastLoc.latitude)
                val lonDiff = kotlin.math.abs(saved.longitude - lastLoc.longitude)
                if (latDiff > 0.01 || lonDiff > 0.01) {
                    resolveAndSaveFromCoordinates(lastLoc.latitude, lastLoc.longitude, "GPS")
                    return locationPrefs.getSettings()
                }
                return saved
            }
        }

        // Priority 2: Saved user selection (or Priority 3: Default Mansoura already stored in prefs)
        Log.d(
            "WAQTI_PRAYER_DEBUG",
            "Resolved location from preferences/default: ${saved.cityName}, ${saved.governorate} (Lat: ${saved.latitude}, Lon: ${saved.longitude}) - Source: ${saved.locationSource}"
        )
        return saved
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission() || locationManager == null) return null

        var bestLocation: Location? = null
        try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
        } catch (e: Exception) {
            Log.e("WAQTI_PRAYER_DEBUG", "Error accessing getLastKnownLocation: ${e.message}")
        }
        return bestLocation
    }

    @SuppressLint("MissingPermission")
    fun refreshCurrentLocation(
        onResult: (success: Boolean, settings: PrayerLocationAndCalcSettings) -> Unit
    ) {
        if (!hasLocationPermission() || locationManager == null) {
            Log.w("WAQTI_PRAYER_DEBUG", "Cannot refresh GPS location: permission not granted or manager null. Using saved/default location.")
            onResult(false, locationPrefs.getSettings())
            return
        }

        val lastLoc = getLastKnownLocation()
        if (lastLoc != null) {
            resolveAndSaveFromCoordinates(lastLoc.latitude, lastLoc.longitude, "GPS")
            onResult(true, locationPrefs.getSettings())
            return
        }

        try {
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider == null) {
                Log.w("WAQTI_PRAYER_DEBUG", "No location provider enabled. Keeping saved location.")
                onResult(false, locationPrefs.getSettings())
                return
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    try {
                        locationManager.removeUpdates(this)
                    } catch (e: Exception) {
                        // ignore
                    }
                    resolveAndSaveFromCoordinates(location.latitude, location.longitude, "GPS")
                    onResult(true, locationPrefs.getSettings())
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    onResult(false, locationPrefs.getSettings())
                }
            }

            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("WAQTI_PRAYER_DEBUG", "Exception during requestSingleUpdate: ${e.message}")
            onResult(false, locationPrefs.getSettings())
        }
    }

    private fun resolveAndSaveFromCoordinates(latitude: Double, longitude: Double, source: String) {
        var cityNameAr = "موقعي الحالي"
        var cityNameEn = "Current Location"
        var governorateAr = "مصر"
        var governorateEn = "Egypt"
        var countryAr = "مصر"
        var countryEn = "Egypt"

        // Match against known Egyptian presets first
        val nearestPreset = PrayerCalculator.findNearestCity(latitude, longitude)
        if (nearestPreset != null) {
            cityNameAr = nearestPreset.cityNameAr
            cityNameEn = nearestPreset.cityNameEn
            governorateAr = nearestPreset.governorateAr
            governorateEn = nearestPreset.governorateEn
            countryAr = nearestPreset.countryAr
            countryEn = nearestPreset.countryEn
        } else {
            // Attempt geocoding
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        cityNameAr = addr.locality ?: addr.subAdminArea ?: cityNameAr
                        cityNameEn = addr.locality ?: addr.subAdminArea ?: cityNameEn
                        governorateAr = addr.adminArea ?: governorateAr
                        governorateEn = addr.adminArea ?: governorateEn
                        countryAr = addr.countryName ?: countryAr
                        countryEn = addr.countryName ?: countryEn
                    }
                }
            } catch (e: Exception) {
                Log.w("WAQTI_PRAYER_DEBUG", "Geocoder resolution error: ${e.message}")
            }
        }

        locationPrefs.updateLocation(
            cityNameAr = cityNameAr,
            cityNameEn = cityNameEn,
            governorateAr = governorateAr,
            governorateEn = governorateEn,
            countryAr = countryAr,
            countryEn = countryEn,
            latitude = latitude,
            longitude = longitude,
            source = source
        )

        Log.d(
            "WAQTI_PRAYER_DEBUG",
            "Location updated from $source: $cityNameAr, $governorateAr (Lat: $latitude, Lon: $longitude)"
        )
    }
}
