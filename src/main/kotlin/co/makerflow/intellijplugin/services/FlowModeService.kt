package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.FlowModeApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.EndedFlowMode
import co.makerflow.client.models.FlowMode
import co.makerflow.intellijplugin.settings.SettingsState
import co.makerflow.intellijplugin.state.Flow
import com.intellij.openapi.components.Service
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
    suspend fun startFlowMode(): FlowMode? {

        return coroutineScope {
            var flowMode: FlowMode? = null
            launch {
                val flowModeApi = flowModeApi()
                val startFlowModeResponse = flowModeApi.startFlowMode("jetbrains", false, null, null, null, null)
                flowMode = if (startFlowModeResponse.status == HttpStatusCode.Conflict.value) {
                    fetchOngoingFlowMode()
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

    private fun flowModeApi(): FlowModeApi {
        val apiToken = getApiToken()
        val api = FlowModeApi(baseUrl, null, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return api
    }

    private fun getApiToken() = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken

    suspend fun fetchOngoingFlowMode(): FlowMode? = coroutineScope {
        var flowMode: FlowMode? = null
        launch {
            val flowModeApi = flowModeApi()
            val fetchOngoingFlowMode = flowModeApi.fetchOngoingFlowMode("jetbrains")
            if (fetchOngoingFlowMode.success) {
                @Suppress("TooGenericExceptionCaught")
                try {
                    flowMode = fetchOngoingFlowMode.body().data
                } catch (e: Exception) {
                    when(e) {
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                             /*
                            Intentionally left blank
                            thisLogger().error(e)
                            thisLogger().error("Error converting ongoing flow mode: ${e.message}")
                            */
                        }
                        else -> throw e
                    }
                }
            }
        }.join()
        return@coroutineScope flowMode
    }
}

// extension function to convert from FlowMode to Flow
fun FlowMode.toFlow(): Flow {
    val scheduledEnd = this.scheduledEnd?.let { Instant.parse(it) }
    return Flow(this.id, Instant.parse(this.start), this.pairing ?: false, scheduledEnd)
}
