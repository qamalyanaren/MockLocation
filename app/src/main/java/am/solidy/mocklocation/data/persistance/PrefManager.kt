package am.solidy.mocklocation.data.persistance

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds only an instance of [SharedPreferences]
 * Do not put values that should be stored in shared preferences
 * Use [PrefManager] for that instead
 */
interface SharedPrefHolder {
    val preferences: SharedPreferences
}

//NOTE: real abstraction

interface PrefManager {
    var param1: String
    var param2: String

    fun clear()
}

@Singleton
class PrefManagerImpl @Inject constructor(
    preferences: SharedPreferences
) : PrefManager, SharedPrefHolder {
    override val preferences: SharedPreferences by lazy { preferences }

    override var param1: String by PrefDelegate("")
    override var param2: String by PrefDelegate("")

    override fun clear() {
        param1 = ""
        param2 = ""
    }

}
