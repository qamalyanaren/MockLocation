package am.solidy.mocklocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock


//TODO: just for testing
class MockLocationImpl internal constructor(context: Context) {
    private val mLocationManager: LocationManager
    private val mHandler: Handler
    private lateinit var mRunnable: Runnable

    init {
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mHandler = Handler(Looper.getMainLooper())
    }

    fun startMockLocationUpdates(latitude: Double, longitude: Double) {
        mRunnable = Runnable {
            setMock(LocationManager.GPS_PROVIDER, latitude, longitude)
            setMock(LocationManager.NETWORK_PROVIDER, latitude, longitude)
            mHandler.postDelayed(mRunnable, 1000)
        }
        mHandler.post(mRunnable)
    }

    fun stopMockLocationUpdates() {
        mHandler.removeCallbacks(mRunnable)
        mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("WrongConstant")
    private fun setMock(provider: String, latitude: Double, longitude: Double) {

        val powerUsage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ProviderProperties.POWER_USAGE_LOW
        } else 0

        val accuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ProviderProperties.ACCURACY_FINE
        } else 5

        try {
            mLocationManager.addTestProvider(
                provider,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                powerUsage,
                accuracy
            )
        } catch (ignored: java.lang.IllegalArgumentException) {
            //TODO: error handling
        }

        val newLocation = Location(provider).also {
            it.latitude = latitude
            it.longitude = longitude
            it.altitude = 3.0
            it.time = System.currentTimeMillis()
            it.speed = 0.01f
            it.bearing = 1f
            it.accuracy = 3f
            it.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.bearingAccuracyDegrees = 0.1f
                it.verticalAccuracyMeters = 0.1f
                it.speedAccuracyMetersPerSecond = 0.01f
            }
            it.provider = provider
        }
        try {
            mLocationManager.setTestProviderEnabled(provider, true)
        } catch (ignored: java.lang.IllegalArgumentException) {
            //TODO: error handling
        }
        try {
            mLocationManager.setTestProviderLocation(provider, newLocation)
        } catch (ignored: java.lang.IllegalArgumentException) {
            //TODO: error handling
        }
    }
}