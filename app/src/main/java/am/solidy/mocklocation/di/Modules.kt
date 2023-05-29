package am.solidy.mocklocation.di

import am.solidy.mocklocation.data.MockLocationRepository
import am.solidy.mocklocation.data.MockLocationRepositoryImpl
import am.solidy.mocklocation.data.persistance.PrefManager
import am.solidy.mocklocation.data.persistance.PrefManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun getHttpClient(httpClient: KtorClient): HttpClient = httpClient.getHttpClient()
}

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindMockLocationRepository(impl: MockLocationRepositoryImpl): MockLocationRepository

    @Binds
    fun bindPrefManager(impl: PrefManagerImpl): PrefManager
}