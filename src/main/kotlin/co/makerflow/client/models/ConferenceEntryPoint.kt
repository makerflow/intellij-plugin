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
 * Information about calling into the meeting
 *
 * @param entryPointType
 * @param label
 * @param uri
 */


@JsonIgnoreProperties(ignoreUnknown = true)
data class ConferenceEntryPoint (

    @field:JsonProperty("entryPointType")
    val entryPointType: kotlin.String? = null,

    @field:JsonProperty("label")
    val label: kotlin.String? = null,

    @field:JsonProperty("uri")
    val uri: kotlin.String? = null

)

