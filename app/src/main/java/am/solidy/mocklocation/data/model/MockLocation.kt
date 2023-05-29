package am.solidy.mocklocation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MockLocation(
    val userId: Long,
    val id: Long,
    val title: String,
    val completed: Boolean,
)
