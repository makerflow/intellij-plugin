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

import co.makerflow.client.models.FetchOngoingFlowMode200ResponseAnyOf
import co.makerflow.client.models.FlowMode
import co.makerflow.client.models.TypedTodo

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 *
 * @param `data` 
 * @param todo 
 */


data class FetchOngoingFlowMode200Response (

    @field:JsonProperty("data")
    val `data`: FlowMode? = null,

    @field:JsonProperty("todo")
    val todo: TypedTodo? = null

)

