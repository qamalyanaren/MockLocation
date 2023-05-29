package am.solidy.mocklocation.data.persistance

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefDelegate<T>(
    private val defaultValue: T,
    //_key parameter is optional. The property name will be used if not specified
    private val _key: String? = null
) {

    private var storedValue: T? = null

    operator fun provideDelegate(
        thisRef: SharedPrefHolder,
        prop: KProperty<*>
    ): ReadWriteProperty<SharedPrefHolder, T> {
        val key = _key ?: prop.name

        return object : ReadWriteProperty<SharedPrefHolder, T> {
            override fun getValue(thisRef: SharedPrefHolder, property: KProperty<*>): T {
                if (!thisRef.preferences.contains(key)) {
                    setValue(thisRef, property, defaultValue)
                    return defaultValue
                }
                if (storedValue == null) {
                    @Suppress("UNCHECKED_CAST")
                    storedValue = when (defaultValue) {
                        is Int -> thisRef.preferences.getInt(key, defaultValue as Int) as T
                        is Long -> thisRef.preferences.getLong(key, defaultValue as Long) as T
                        is Float -> thisRef.preferences.getFloat(key, defaultValue as Float) as T
                        is String -> thisRef.preferences.getString(key, defaultValue as String) as T
                        is Boolean -> thisRef.preferences.getBoolean(
                            key,
                            defaultValue as Boolean
                        ) as T

                        else -> error("This type can not be stored into Preferences")
                    }
                }
                return storedValue!!
            }

            override fun setValue(thisRef: SharedPrefHolder, property: KProperty<*>, value: T) {
                with(thisRef.preferences.edit()) {
                    when (value) {
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                        is String -> putString(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> error("Only primitive types can be stored into Preferences")
                    }
                    apply()
                }
                storedValue = value
            }

        }
    }
}