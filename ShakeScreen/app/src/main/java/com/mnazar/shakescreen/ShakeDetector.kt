package com.mnazar.shakescreen

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val sensorManager: SensorManager,
    private val onShake: () -> Unit
) : SensorEventListener {

    // Reduced threshold for more sensitive shake detection
    private val shakeThreshold = 8f
    private var lastShakeTime = 0L
    private var isListening = false

    // Optional: Track previous acceleration for better shake detection
    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var currentAcceleration = SensorManager.GRAVITY_EARTH

    fun start() {
        if (!isListening) {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sensor ->
                val registered = sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI
                )
                isListening = registered

                if (registered) {
                    android.util.Log.d("ShakeDetector", "Shake detection started")
                } else {
                    android.util.Log.e("ShakeDetector", "Failed to register accelerometer listener")
                }
            } ?: run {
                android.util.Log.e("ShakeDetector", "No accelerometer sensor available")
            }
        }
    }

    fun stop() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            android.util.Log.d("ShakeDetector", "Shake detection stopped")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate acceleration magnitude
            val acceleration = sqrt(x * x + y * y + z * z)

            // Apply low-pass filter to smooth out noise
            lastAcceleration = currentAcceleration
            currentAcceleration = acceleration * 0.1f + lastAcceleration * 0.9f

            // Calculate delta (change in acceleration)
            val delta = acceleration - currentAcceleration

            // Check if shake threshold is exceeded
            if (delta > shakeThreshold) {
                val now = System.currentTimeMillis()

                // Debounce: ignore shakes that happen too quickly
                if (now - lastShakeTime > 800) { // Increased debounce to 800ms
                    lastShakeTime = now
                    android.util.Log.d("ShakeDetector", "Shake detected! Delta: $delta")
                    onShake()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        android.util.Log.d("ShakeDetector", "Sensor accuracy changed: $accuracy")
    }
}