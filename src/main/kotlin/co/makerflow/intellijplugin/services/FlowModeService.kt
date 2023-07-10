package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.FlowModeApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.EndedFlowMode
import co.makerflow.client.models.FlowMode
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.settings.SettingsState
import co.makerflow.intellijplugin.state.Flow
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Service
class FlowModeService {
    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL
    private val todoUtil = service<TodoUtil>()

    private fun flowModeApi(): FlowModeApi {
        val apiToken = getApiToken()
        val api = FlowModeApi(baseUrl, null, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return api
    }

    private fun getApiToken() = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken

    suspend fun startFlowMode(): FlowMode? {
        return startFlowMode(null, null)
    }
    suspend fun startFlowMode(todo: TypedTodo?, duration: Int?): FlowMode? {

        return coroutineScope {
            var flowMode: FlowMode? = null
            launch {
                val flowModeApi = flowModeApi()
                val startFlowModeResponse = flowModeApi.startFlowMode("jetbrains",
                    false,
                    duration,
                    todo?.sourceType?.name,
                    todo?.type,
                    todo?.let { todoUtil.determineTodoId(todo) })
                flowMode = if (startFlowModeResponse.status == HttpStatusCode.Conflict.value) {
                    fetchOngoingFlowMode().first
                } else {
                    startFlowModeResponse.body().data
                }
            }.join()
            return@coroutineScope flowMode
        }
    }

    suspend fun stopFlowMode(): EndedFlowMode? = coroutineScope {
        var flowMode: EndedFlowMode? = null
        launch {
            flowMode = flowModeApi().stopOngoingFlowMode("jetbrains").body().data
        }.join()
        return@coroutineScope flowMode
    }

    suspend fun fetchOngoingFlowMode(): Pair<FlowMode?, TypedTodo?> = coroutineScope {
        var pair = Pair<FlowMode?, TypedTodo?>(null, null)
        launch {
            val flowModeApi = flowModeApi()
            val response = flowModeApi.fetchOngoingFlowMode("jetbrains")
            if (response.success) {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val body = response.body()
                    pair = Pair(body.data, body.todo)
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
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
