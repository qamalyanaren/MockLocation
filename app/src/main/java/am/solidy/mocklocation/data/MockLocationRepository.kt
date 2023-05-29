package am.solidy.mocklocation.data

import am.solidy.mocklocation.data.model.MockLocation
import am.solidy.mocklocation.data.payload.MockLocationPayload
import am.solidy.mocklocation.data.persistance.PrefManager
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val MOCK_LOCATION_URL =
    "https://www.keepup.care/api/app/employee/find-employee-upcoming-task-location"

interface MockLocationRepository {
    fun fetchMockLocation(payload: MockLocationPayload): Flow<MockLocation>
}

class MockLocationRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : MockLocationRepository {
    override fun fetchMockLocation(payload: MockLocationPayload) = flow {
        val response = httpClient.post<MockLocation>(MOCK_LOCATION_URL) {
            body = payload
        }
        emit(response)
    }

}