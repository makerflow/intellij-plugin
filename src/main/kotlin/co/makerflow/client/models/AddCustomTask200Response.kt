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

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 *
 * @param `data`
 * @param message
 * @param success
 */


data class AddCustomTask200Response(

    @field:JsonProperty("data")
    val `data`: CustomTask? = null,

    @field:JsonProperty("message")
    val message: kotlin.String? = null,

    @field:JsonProperty("success")
    val success: kotlin.Boolean? = null

)

