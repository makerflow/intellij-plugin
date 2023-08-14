package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.FlowModeApi
import co.makerflow.client.models.EndedFlowMode
import co.makerflow.client.models.FlowMode
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.providers.ApiClientProvider
import co.makerflow.intellijplugin.state.Flow
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Service
class FlowModeService {
    private val todoUtil = service<TodoUtil>()

    private fun flowModeApi(): FlowModeApi? {
        return service<ApiClientProvider>().provide(FlowModeApi::class.java)
    }


    suspend fun startFlowMode(todo: TypedTodo?, duration: Int?): FlowMode? {

        return coroutineScope {
            var flowMode: FlowMode? = null
            val flowModeApi = flowModeApi() ?: return@coroutineScope flowMode
            launch {
                val startFlowModeResponse = flowModeApi.startFlowMode("jetbrains",
                    false,
                    duration,
                    todo?.sourceType?.name,
                    todo?.type,
                    todo?.let { todoUtil.determineTodoId(todo) })
                @Suppress("TooGenericExceptionCaught")
                try {
                    flowMode = if (startFlowModeResponse.status == HttpStatusCode.Conflict.value) {
                        fetchOngoingFlowMode().first
                    } else {
                        startFlowModeResponse.body().data
                    }
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
                        is HttpRequestTimeoutException,
                        is ConnectTimeoutException,
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank */
//                           thisLogger().error(e)
//                           thisLogger().error("Error converting ongoing flow mode: ${e.message}")
                        }

                        else -> throw e
                    }
                }
            }.join()
            return@coroutineScope flowMode
        }
    }

    suspend fun stopFlowMode(): EndedFlowMode? = coroutineScope {
        var flowMode: EndedFlowMode? = null
        val flowModeApi = flowModeApi() ?: return@coroutineScope flowMode
        launch {
            flowMode = flowModeApi.stopOngoingFlowMode("jetbrains").body().data
        }.join()
        return@coroutineScope flowMode
    }

    suspend fun fetchOngoingFlowMode(): Pair<FlowMode?, TypedTodo?> = coroutineScope {
        var pair = Pair<FlowMode?, TypedTodo?>(null, null)
        val flowModeApi = flowModeApi() ?: return@coroutineScope pair
        launch {
            val response = flowModeApi.fetchOngoingFlowMode("jetbrains")
            if (response.success) {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val body = response.body()
                    pair = Pair(body.data, body.todo)
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
                        is HttpRequestTimeoutException,
                        is ConnectTimeoutException,
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank */
//                           thisLogger().error(e)
//                           thisLogger().error("Error converting ongoing flow mode: ${e.message}")
                        }

                        else -> throw e
                    }
                }
            }
        }.join()
        return@coroutineScope pair
    }


}

// extension function to convert from FlowMode to Flow
fun FlowMode.toFlow(todo: TypedTodo? = null): Flow {
    val scheduledEnd = this.scheduledEnd?.let { Instant.parse(it) }
    return Flow(this.id, Instant.parse(this.start), this.pairing ?: false, scheduledEnd, todo)
}
