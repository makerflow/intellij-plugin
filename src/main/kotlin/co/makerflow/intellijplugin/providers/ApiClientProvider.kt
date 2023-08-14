package co.makerflow.intellijplugin.providers

import co.makerflow.client.apis.BreaksApi
import co.makerflow.client.apis.CalendarApi
import co.makerflow.client.apis.EventsApi
import co.makerflow.client.apis.FlowModeApi
import co.makerflow.client.apis.ProductiveActivityApi
import co.makerflow.client.apis.TasksApi
import co.makerflow.client.infrastructure.ApiClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

@Service
class ApiClientProvider {

    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL

    fun <T : ApiClient> provide(clazz: Class<T>): T? {
        val apiToken = service<ApiTokenProvider>().getApiToken()
        if (apiToken.isEmpty()) {
            return null
        }
        @Suppress("UNCHECKED_CAST") val api: T? = when (clazz) {
            EventsApi::class.java -> EventsApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            BreaksApi::class.java -> BreaksApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            FlowModeApi::class.java -> FlowModeApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            ProductiveActivityApi::class.java -> ProductiveActivityApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            CalendarApi::class.java -> CalendarApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            TasksApi::class.java -> TasksApi(baseUrl, null, null, ApiClient.JSON_DEFAULT) as T

            else -> null
        }
        api?.setApiKey(apiToken)
        return api
    }
}
