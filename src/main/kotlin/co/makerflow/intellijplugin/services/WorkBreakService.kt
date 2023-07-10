package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.BreaksApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.BreakReason
import co.makerflow.client.models.EndedWorkBreak
import co.makerflow.client.models.WorkBreak
import co.makerflow.intellijplugin.settings.SettingsState
import co.makerflow.intellijplugin.state.Break
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.util.messages.Topic
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * A service to manage work breaks.
 */
@Service
class WorkBreakService : DumbAware {
    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL

    private fun api(): BreaksApi {
        val apiToken = getApiToken()
        val api = BreaksApi(baseUrl, null, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return api
    }

    private fun getApiToken() = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken

    /**
     * Starts a work break.
     */
    suspend fun startWorkBreak(reason: BreakReason): WorkBreak? {
        return coroutineScope {
            var workBreak: WorkBreak? = null
            launch {
                val breaksApi = api()
                @Suppress("TooGenericExceptionCaught")
                try {
                    val startWorkBreakResponse = breaksApi.startWorkBreak(reason, "jetbrains", null)
                    if (startWorkBreakResponse.success) {
                        workBreak = startWorkBreakResponse.body()
                    }
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank */
//                            thisLogger().error(e)
//                            thisLogger().error("Error starting work break: ${e.message}")
                        }

                        else -> {
                            thisLogger().error(e)
                            thisLogger().error("Error starting work break: ${e.message}")
                            throw e
                        }
                    }
                }
            }.join()
            return@coroutineScope workBreak
        }
    }

    /**
     * Stops ongoing work break.
     */
    suspend fun stopWorkBreak(): EndedWorkBreak? {
        return coroutineScope {
            var workBreak: EndedWorkBreak? = null
            launch {
                val breaksApi = api()
                @Suppress("TooGenericExceptionCaught")
                try {
                    val stopWorkBreakResponse = breaksApi.stopOngoingBeak("jetbrains")
                    if (stopWorkBreakResponse.success) {
                        workBreak = stopWorkBreakResponse.body()
                    }
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank */
//                            thisLogger().error(e)
//                            thisLogger().error("Error stopping work break: ${e.message}")
                        }

                        else -> {
                            thisLogger().error(e)
                            thisLogger().error("Error stopping work break: ${e.message}")
                            throw e
                        }
                    }
                }
            }.join()
            return@coroutineScope workBreak
        }
    }

    /**
     * Fetches ongoing work break.
     */
    suspend fun getOngoingWorkBreak(): WorkBreak? {
        return coroutineScope {
            var workBreak: WorkBreak? = null
            launch {
                val breaksApi = api()
                @Suppress("TooGenericExceptionCaught")
                try {
                    val ongoingWorkBreakResponse = breaksApi.getOngoingBreak("jetbrains")
                    if (ongoingWorkBreakResponse.success) {
                        workBreak = ongoingWorkBreakResponse.body()
                    }
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank */
//                            thisLogger().error(e)
//                            thisLogger().error("Error fetching ongoing work break: ${e.message}")
                        }

                        else -> {
                            thisLogger().error(e)
                            thisLogger().error("Error fetching ongoing work break: ${e.message}")
                            throw e
                        }
                    }
                }
            }.join()
            return@coroutineScope workBreak
        }
    }

}
