package am.solidy.mocklocation


import am.solidy.mocklocation.databinding.ActivityMainBinding
import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


//TODO: just for testing
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var mockLocationProvider: MockLocationImpl? = null
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tieLogin.setText(viewModel.param1.value)
        binding.tiePassword.setText(viewModel.param2.value)

        viewModel.isInputsValid.flowWithLifecycle(lifecycle)
            .onEach {
                binding.btnFetch.isEnabled = it
            }.launchIn(lifecycleScope)

        binding.tieLogin.doAfterTextChanged { viewModel.setParam1(it.toString()) }
        binding.tiePassword.doAfterTextChanged { viewModel.setParam2(it.toString()) }


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
            Toast.makeText(
                this,
                "We need permission for mock location",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMockLocation() {

        if (!isDeveloperOptionsEnabled()) {
            Toast.makeText(
                this,
                "Please enable developer mode",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!isMockLocationEnabled()) {
            Toast.makeText(
                this,
                "Please turn on Mock Location permission on Developer Settings",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            return
        }

        viewModel.mockLocation.flowWithLifecycle(lifecycle)
            .onEach { location ->

                mockLocationProvider = MockLocationImpl(this)
                mockLocationProvider?.startMockLocationUpdates(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                Toast.makeText(
                    this,
                    "The location is mocked successfully",
                    Toast.LENGTH_LONG
                ).show()

            }.launchIn(lifecycleScope)

        binding.btnFetch.setOnClickListener {
            viewModel.getMockLocation()
        }
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
    }
}