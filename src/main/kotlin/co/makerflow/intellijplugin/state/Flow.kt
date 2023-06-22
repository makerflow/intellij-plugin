package co.makerflow.intellijplugin.state

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Flow(
    val id: Int,
    val start: Instant,
    val pairing: Boolean,
    val scheduledEnd: Instant?,
)
