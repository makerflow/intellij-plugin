package co.makerflow.intellijplugin.state

import co.makerflow.client.models.WorkBreak
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Break(
    val id: Int,
    val start: Instant,
    val end: Instant?,
)

fun WorkBreak.toBreak(): Break {
    return Break(
        id = this.id,
        start = Instant.parse(this.start),
        end = this.end?.let { Instant.parse(it) }
    )
}
