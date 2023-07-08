package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.EventsApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.CalendarEvent
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Service
class EventsService : DumbAware {
    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL

    private fun eventsApi(): EventsApi {
        val apiToken = getApiToken()
        val api = EventsApi(baseUrl, null, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return api
    }

    private fun getApiToken() = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken

    suspend fun fetchEvents(): List<CalendarEvent> {
        return coroutineScope {
            var events: List<CalendarEvent>? = listOf()
            launch {
                val response = eventsApi().upcomingCalendarEvents("jetbrains")
                @Suppress("TooGenericExceptionCaught")
                try {
                    if (response.success) {
                        events = response.body().events
                    }
                } catch (e: Exception) {
                    @Suppress("kotlin:S125")
                    when (e) {
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
