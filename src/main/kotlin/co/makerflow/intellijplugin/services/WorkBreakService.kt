package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.BreaksApi
import co.makerflow.client.models.BreakReason
import co.makerflow.client.models.EndedWorkBreak
import co.makerflow.client.models.WorkBreak
import co.makerflow.intellijplugin.providers.ApiClientProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * A service to manage work breaks.
 */
@Service
class WorkBreakService : DumbAware {

    private fun api(): BreaksApi? {
        return service<ApiClientProvider>().provide(BreaksApi::class.java)
    }


    /**
     * Starts a work break.
     */
    suspend fun startWorkBreak(reason: BreakReason): WorkBreak? {
        return coroutineScope {
            var workBreak: WorkBreak? = null
            val breaksApi = api() ?: return@coroutineScope workBreak
            launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val startWorkBreakResponse = breaksApi.startWorkBreak(reason, "jetbrains", null)
                    if (startWorkBreakResponse.success) {
                        workBreak = startWorkBreakResponse.body()
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
            val breaksApi = api() ?: return@coroutineScope workBreak
            launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val stopWorkBreakResponse = breaksApi.stopOngoingBeak("jetbrains")
                    if (stopWorkBreakResponse.success) {
                        workBreak = stopWorkBreakResponse.body()
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
            val breaksApi = api() ?: return@coroutineScope workBreak
            launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val ongoingWorkBreakResponse = breaksApi.getOngoingBreak("jetbrains")
                    if (ongoingWorkBreakResponse.success) {
                        workBreak = ongoingWorkBreakResponse.body()
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
