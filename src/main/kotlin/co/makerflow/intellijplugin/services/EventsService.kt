package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.EventsApi
import co.makerflow.client.models.CalendarEvent
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

@Service
class EventsService : DumbAware {

    private fun eventsApi(): EventsApi? {
        return service<ApiClientProvider>().provide(EventsApi::class.java)
    }


    suspend fun fetchEvents(): List<CalendarEvent> {
        return coroutineScope {
            var events: List<CalendarEvent>? = listOf()
            val eventsApi = eventsApi() ?: return@coroutineScope events!!
            launch {
                val response = eventsApi.upcomingCalendarEvents("jetbrains")
                @Suppress("TooGenericExceptionCaught")
                try {
                    if (response.success) {
                        events = response.body().events
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
                           Intentionally commented out */
//                           thisLogger().error(e)
//                           thisLogger().error("Error converting ongoing flow mode: ${e.message}")
                        }

                        else -> {
                            thisLogger().error("Error fetching events: ${e.message}")
                            throw e
                        }
                    }
                }
            }.join()
            return@coroutineScope events!!
        }
    }
}
