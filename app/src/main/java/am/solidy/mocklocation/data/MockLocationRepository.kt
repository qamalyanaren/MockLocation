package am.solidy.mocklocation.data

import am.solidy.mocklocation.data.model.MockLocation
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

private const val MOCK_LOCATION_URL = "https://jsonplaceholder.typicode.com/todos/"

interface MockLocationRepository {
    fun fetchMockLocation(): Flow<MockLocation>
}

class MockLocationRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient
) : MockLocationRepository {
    override fun fetchMockLocation() = flow {
        val response = httpClient.get<MockLocation> {
            url(MOCK_LOCATION_URL + "${Random.nextInt(1, 100)}")
        }
        emit(response)
    }

}