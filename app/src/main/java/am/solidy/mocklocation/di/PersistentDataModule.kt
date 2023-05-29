package am.solidy.mocklocation.di

import am.solidy.mocklocation.BuildConfig
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistentDataModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .setUserAuthenticationRequired(false)
            .build()

        // TODO: cause of this issue https://issuetracker.google.com/issues/164901843?pli=1
        val sharedPreferences = try {
            initialiseSecurePrefs(context, masterKey)
        } catch (ex: Exception) {
            clearSharedPreferences(context)
            initialiseSecurePrefs(context, masterKey)
        }

        return sharedPreferences
    }

    private fun initialiseSecurePrefs(context: Context, masterKey: MasterKey) =
        EncryptedSharedPreferences.create(
            context,
            BuildConfig.APPLICATION_ID + "prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private fun clearSharedPreferences(context: Context) {
        val dir = File("${context.filesDir.parent}/shared_prefs/")
        val children = dir.list() ?: arrayOf()
        for (i in children.indices) {
            context.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE)
                .edit()
                .clear().commit()
            File(dir, children[i]).delete()
        }
    }
}