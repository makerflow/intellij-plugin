/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package co.makerflow.client.apis

import co.makerflow.client.models.CalendarEvent

import co.makerflow.client.infrastructure.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.forms.formData
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.ParametersBuilder
import com.fasterxml.jackson.databind.ObjectMapper

    open class CalendarApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonBlock: ObjectMapper.() -> Unit = ApiClient.JSON_DEFAULT,
    ) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonBlock) {

        /**
        * 
        * 
         * @param source To specify source of request (optional)
         * @return kotlin.collections.List<CalendarEvent>
        */
            @Suppress("UNCHECKED_CAST")
        open suspend fun upcomingCalendarEvents(source: kotlin.String?): HttpResponse<kotlin.collections.List<CalendarEvent>> {

            val localVariableAuthNames = listOf<String>("api_token")

            val localVariableBody = 
                    io.ktor.client.utils.EmptyContent

            val localVariableQuery = mutableMapOf<String, List<String>>()
            source?.apply { localVariableQuery["source"] = listOf("$source") }

            val localVariableHeaders = mutableMapOf<String, String>()

            val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/tasks/calendar/events",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
            )

            return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
            ).wrap()
            }

        }