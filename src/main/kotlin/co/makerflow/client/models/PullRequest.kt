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
 * @param id 
 * @param userId 
 * @param done 
 * @param eventType 
 * @param link 
 * @param repositoryUuid 
 * @param repositoryName 
 * @param pullrequestId 
 * @param pullrequestTitle 
 * @param installationId 
 */


data class PullRequest (

    @field:JsonProperty("id")
    val id: kotlin.Int? = null,

    @field:JsonProperty("user_id")
    val userId: kotlin.Int? = null,

    @field:JsonProperty("done")
    val done: kotlin.Boolean? = null,

    @field:JsonProperty("event_type")
    val eventType: kotlin.String? = null,

    @field:JsonProperty("link")
    val link: kotlin.String? = null,

    @field:JsonProperty("repository_uuid")
    val repositoryUuid: kotlin.String? = null,

    @field:JsonProperty("repository_name")
    val repositoryName: kotlin.String? = null,

    @field:JsonProperty("pullrequest_id")
    val pullrequestId: kotlin.String? = null,

    @field:JsonProperty("pullrequest_title")
    val pullrequestTitle: kotlin.String? = null,

    @field:JsonProperty("installation_id")
    val installationId: kotlin.String? = null

)

