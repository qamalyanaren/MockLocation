package am.solidy.mocklocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


//TODO: just for testing
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var mockLocationProvider: MockLocationImpl? = null
    private lateinit var tvMockInfo: TextView

    private val viewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMockInfo = findViewById(R.id.tvFakeInfo)

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        if (requestCode == PERMISSION_REQUEST_CODE && permissionLocation) {
            initMockLocation()
        } else {
            tvMockInfo.text = "Չհաջողվեց ձեր կորդինատները փոխարինել"
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMockLocation() {
//        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//        val mockLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (!isDeveloperOptionsEnabled()) {
            Toast.makeText(
                this,
                "Please enable developer mode",
                Toast.LENGTH_LONG
            ).show()
            tvMockInfo.text = "Չհաջողվեց ձեր կորդինատները փոխարինել"
            return
        }
        if (!isMockLocationEnabled()) {
            Toast.makeText(
                this,
                "Please turn on Mock Location permission on Developer Settings",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            tvMockInfo.text = "Չհաջողվեց ձեր կորդինատները փոխարինել"
            return
        }

        viewModel.mockLocation.flowWithLifecycle(lifecycle)
            .onEach {
                mockLocationProvider = MockLocationImpl(this)
                mockLocationProvider?.startMockLocationUpdates(
                    latitude = MOCK_LOCATION_LATITUDE,
                    longitude = MOCK_LOCATION_LONGITUDE
                )

                tvMockInfo.text = "Ձեր կորդինատները հաջողությամբ փոխարինվեց (Վարդաբլուր, Լոռի)"

            }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        mockLocationProvider?.stopMockLocationUpdates()
        super.onDestroy()
    }

    private fun isDeveloperOptionsEnabled(): Boolean {
        return Settings.Secure.getInt(
            this.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    @Suppress("DEPRECATION")
    private fun isMockLocationEnabled(): Boolean {
        val isMockLocation: Boolean = try {
            val opsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                opsManager.unsafeCheckOp(
                    AppOpsManager.OPSTR_MOCK_LOCATION,
                    Process.myUid(),
                    BuildConfig.APPLICATION_ID
                ) == AppOpsManager.MODE_ALLOWED
            } else {
                opsManager.checkOp(
                    AppOpsManager.OPSTR_MOCK_LOCATION,
                    Process.myUid(),
                    BuildConfig.APPLICATION_ID
                ) == AppOpsManager.MODE_ALLOWED
            }
        } catch (e: Exception) {
            false
        }
        return isMockLocation
    }


    companion object {
        const val PERMISSION_REQUEST_CODE = 101

        const val MOCK_LOCATION_LATITUDE = 40.9696605
        const val MOCK_LOCATION_LONGITUDE = 44.5075671
    }
}