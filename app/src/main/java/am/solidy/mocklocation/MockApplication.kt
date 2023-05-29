package am.solidy.mocklocation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}