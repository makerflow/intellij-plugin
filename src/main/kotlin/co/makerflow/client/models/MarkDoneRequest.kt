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

import co.makerflow.client.models.TypedTodo

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 *
 * @param todo 
 * @param done 
 */


data class MarkDoneRequest (

    @field:JsonProperty("todo")
    val todo: TypedTodo? = null,

    @field:JsonProperty("done")
    val done: kotlin.Boolean? = null

)
