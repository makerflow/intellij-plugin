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

package co.makerflow.client.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A event in user's calendar
 *
 * @param id
 * @param userIntegrationId
 * @param iCalUID
 * @param start
 * @param startTimezone
 * @param end
 * @param endTimezone
 * @param summary
 * @param htmlLink
 * @param conference
 * @param providerId
 */


@JsonIgnoreProperties(ignoreUnknown = true)
data class CalendarEvent(

    @field:JsonProperty("id")
    val id: kotlin.Int? = null,

    @field:JsonProperty("user_integration_id")
    val userIntegrationId: kotlin.String? = null,

    @field:JsonProperty("iCalUID")
    val iCalUID: kotlin.String? = null,

    @field:JsonProperty("start")
    val start: kotlin.String? = null,

    @field:JsonProperty("start_timezone")
    val startTimezone: kotlin.String? = null,

    @field:JsonProperty("end")
    val end: kotlin.String? = null,

    @field:JsonProperty("end_timezone")
    val endTimezone: kotlin.String? = null,

    @field:JsonProperty("summary")
    val summary: kotlin.String? = null,

    @field:JsonProperty("htmlLink")
    val htmlLink: kotlin.String? = null,

    @field:JsonProperty("conference")
    val conference: Conference? = null,

    @field:JsonProperty("provider_id")
    val providerId: kotlin.String? = null

)

