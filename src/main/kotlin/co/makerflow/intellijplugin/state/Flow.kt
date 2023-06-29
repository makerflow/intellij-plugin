package co.makerflow.intellijplugin.state

import co.makerflow.client.models.TypedTodo
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Flow(
    val id: Int,
    val start: Instant,
    val pairing: Boolean,
    val scheduledEnd: Instant?,
    @Contextual val todo: TypedTodo?,
)
