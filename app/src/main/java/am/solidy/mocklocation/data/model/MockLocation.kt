package am.solidy.mocklocation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MockLocation(
    val hasUpcomingTask: Boolean,
    val latitude: Double,
    val longitude: Double,
)
