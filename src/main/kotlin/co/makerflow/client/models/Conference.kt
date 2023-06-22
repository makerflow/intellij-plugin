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

import co.makerflow.client.models.ConferenceEntryPoint

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 *
 * @param conferenceId 
 * @param location 
 * @param organizerEmail 
 * @param organizerName 
 * @param status 
 * @param entryPoints 
 */


data class Conference (

    @field:JsonProperty("conferenceId")
    val conferenceId: kotlin.String? = null,

    @field:JsonProperty("location")
    val location: kotlin.String? = null,

    @field:JsonProperty("organizer_email")
    val organizerEmail: kotlin.String? = null,

    @field:JsonProperty("organizer_name")
    val organizerName: kotlin.String? = null,

    @field:JsonProperty("status")
    val status: kotlin.String? = null,

    @field:JsonProperty("entryPoints")
    val entryPoints: kotlin.collections.List<ConferenceEntryPoint>? = null

)

