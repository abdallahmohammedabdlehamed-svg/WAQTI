package com.example.data.prayer

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * State of the High-Precision Qibla Compass.
 */
data class QiblaCompassState(
    val isSensorAvailable: Boolean = true,
    val needsCalibration: Boolean = false,
    val isAligned: Boolean = false,
    val isPerfectAlignment: Boolean = false,
    val trueHeading: Float = 0f,       // Phone's current orientation relative to True North (0-360°)
    val magneticHeading: Float = 0f,   // Uncorrected raw magnetic heading
    val magneticDeclination: Float = 0f, // Magnetic declination in degrees
    val qiblaBearing: Float = 0f,      // Geographic bearing from user coords to Kaaba (0-360°)
    val relativeQiblaAngle: Float = 0f, // Angle where compass arrow should point (0-360°)
    val angleDifference: Float = 0f,   // Difference in range [-180, 180]
    val distanceToKaabaKm: Int = 0,
    val userLatitude: Double = 31.0409, // Default Mansoura
    val userLongitude: Double = 31.3785,
    val cityName: String = "المنصورة",
    val sensorTypeUsed: String = "ROTATION_VECTOR"
)

/**
 * Manages device orientation sensors and calculates real-time high-precision Qibla direction.
 *
 * Implements:
 * - Rotation Vector sensor fusion with fallback to Accelerometer + Magnetometer
 * - True North correction using GeomagneticField declination
 * - Full 3D tilt compensation
 * - Display rotation compensation (remapCoordinateSystem)
 * - Battery-conscious lifecycle management
 * - Circular shortest-path angular smoothing across 0°/360°
 * - Calibration status detection
 */
class WaqtiQiblaManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _compassState = MutableStateFlow(QiblaCompassState())
    val compassState: StateFlow<QiblaCompassState> = _compassState.asStateFlow()

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magneticSensor: Sensor? = null

    private var isListening = false
    private var currentDisplayRotation = Surface.ROTATION_0

    // Sensor buffers
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Fallback buffers
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Smoothed values
    private var smoothedTrueHeading: Float = 0f

    init {
        detectSensors()
    }

    private fun detectSensors() {
        if (sensorManager == null) {
            _compassState.value = _compassState.value.copy(isSensorAvailable = false)
            Log.w("WAQTI_QIBLA_DEBUG", "SensorManager is null. Device does not support hardware sensors.")
            return
        }

        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        val available = rotationVectorSensor != null || (accelerometerSensor != null && magneticSensor != null)
        val sensorType = if (rotationVectorSensor != null) "ROTATION_VECTOR" else if (available) "ACCEL_MAG" else "NONE"

        _compassState.value = _compassState.value.copy(
            isSensorAvailable = available,
            sensorTypeUsed = sensorType
        )

        Log.d("WAQTI_QIBLA_DEBUG", "Sensors detected: Available=$available, Type=$sensorType")
    }

    /**
     * Updates user location and recalculates static Qibla bearing, distance, and magnetic declination.
     */
    fun updateLocation(latitude: Double, longitude: Double, cityName: String = "المنصورة") {
        val qiblaBearing = PrayerCalculator.calculateQibla(latitude, longitude).toFloat()
        val distanceKm = PrayerCalculator.getDistanceToMakkah(latitude, longitude)

        val declination = try {
            val geomagneticField = GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            geomagneticField.declination
        } catch (e: Exception) {
            Log.e("WAQTI_QIBLA_DEBUG", "Error calculating GeomagneticField: ${e.message}")
            0f
        }

        val current = _compassState.value
        val diff = calculateAngleDifference(qiblaBearing, current.trueHeading)
        val relative = (qiblaBearing - current.trueHeading + 360f) % 360f
        val isAligned = abs(diff) <= 5.0f
        val isPerfect = abs(diff) <= 3.0f

        _compassState.value = current.copy(
            userLatitude = latitude,
            userLongitude = longitude,
            cityName = cityName,
            qiblaBearing = qiblaBearing,
            distanceToKaabaKm = distanceKm,
            magneticDeclination = declination,
            relativeQiblaAngle = relative,
            angleDifference = diff,
            isAligned = isAligned,
            isPerfectAlignment = isPerfect
        )

        Log.d(
            "WAQTI_QIBLA_DEBUG",
            "Location updated for Qibla: $cityName ($latitude, $longitude) -> Qibla Bearing: $qiblaBearing°, Declination: $declination°, Distance: ${distanceKm}km"
        )
    }

    /**
     * Registers sensor listeners when Qibla screen/dialog becomes visible.
     */
    fun startListening(displayRotation: Int = Surface.ROTATION_0) {
        if (isListening || sensorManager == null) return
        currentDisplayRotation = displayRotation

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
            Log.d("WAQTI_QIBLA_DEBUG", "Registered ROTATION_VECTOR listener")
        } else if (accelerometerSensor != null && magneticSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                this,
                magneticSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
            Log.d("WAQTI_QIBLA_DEBUG", "Registered ACCELEROMETER + MAGNETIC listener")
        }
    }

    /**
     * Unregisters sensor listeners when Qibla screen/dialog is disposed to protect battery life.
     */
    fun stopListening() {
        if (!isListening || sensorManager == null) return
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.w("WAQTI_QIBLA_DEBUG", "Error unregistering sensor listener: ${e.message}")
        }
        isListening = false
        hasGravity = false
        hasGeomagnetic = false
        Log.d("WAQTI_QIBLA_DEBUG", "Stopped sensor listener to conserve battery")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                processRotationMatrix(rotationMatrix)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
                if (hasGeomagnetic) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                        processRotationMatrix(rotationMatrix)
                    }
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                hasGeomagnetic = true
                if (hasGravity) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                        processRotationMatrix(rotationMatrix)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val needsCalib = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        if (_compassState.value.needsCalibration != needsCalib) {
            _compassState.value = _compassState.value.copy(needsCalibration = needsCalib)
            Log.d("WAQTI_QIBLA_DEBUG", "Sensor accuracy changed: $accuracy -> needsCalibration=$needsCalib")
        }
    }

    private fun processRotationMatrix(rotMatrix: FloatArray) {
        // Remap coordinate system according to device screen rotation
        when (currentDisplayRotation) {
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(
                    rotMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    remappedMatrix
                )
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(
                    rotMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    remappedMatrix
                )
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(
                    rotMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    remappedMatrix
                )
            }
            else -> {
                System.arraycopy(rotMatrix, 0, remappedMatrix, 0, 9)
            }
        }

        SensorManager.getOrientation(remappedMatrix, orientationAngles)
        val azimuthRad = orientationAngles[0]
        val rawMagneticDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f

        val declination = _compassState.value.magneticDeclination
        val targetTrueHeading = (rawMagneticDeg + declination + 360f) % 360f

        // Apply smooth circular interpolation
        smoothedTrueHeading = smoothCircular(smoothedTrueHeading, targetTrueHeading, alpha = 0.22f)

        val qiblaBearing = _compassState.value.qiblaBearing
        val relativeQibla = (qiblaBearing - smoothedTrueHeading + 360f) % 360f
        val diff = calculateAngleDifference(qiblaBearing, smoothedTrueHeading)
        val isAligned = abs(diff) <= 5.0f
        val isPerfect = abs(diff) <= 3.0f

        _compassState.value = _compassState.value.copy(
            trueHeading = smoothedTrueHeading,
            magneticHeading = rawMagneticDeg,
            relativeQiblaAngle = relativeQibla,
            angleDifference = diff,
            isAligned = isAligned,
            isPerfectAlignment = isPerfect
        )
    }

    /**
     * Circular smoothing along the shortest angular arc across 0°/360°.
     */
    private fun smoothCircular(current: Float, target: Float, alpha: Float = 0.20f): Float {
        var diff = (target - current + 180f) % 360f - 180f
        if (diff < -180f) diff += 360f
        return (current + diff * alpha + 360f) % 360f
    }

    private fun calculateAngleDifference(bearing: Float, heading: Float): Float {
        val rel = (bearing - heading + 360f) % 360f
        return if (rel > 180f) rel - 360f else rel
    }
}
