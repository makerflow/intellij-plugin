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

import co.makerflow.client.models.GroupedSlackEvent
import co.makerflow.client.models.OnboardingTask
import co.makerflow.client.models.PullRequestTodo
import co.makerflow.client.models.SlackEvent

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 *
 * @param group
 * @param type
 * @param sourceType
 * @param events
 * @param createdAt
 * @param createdAt
 * @param done
 * @param step
 */


data class MarkDoneRequestTodo (

    @field:JsonProperty("group")
    val group: kotlin.String? = null,

    @field:JsonProperty("type")
    val type: MarkDoneRequestTodo.Type? = null,

    @field:JsonProperty("sourceType")
    val sourceType: MarkDoneRequestTodo.SourceType? = null,

    @field:JsonProperty("events")
    val events: kotlin.collections.List<SlackEvent>? = null,

    @field:JsonProperty("created_at")
    val createdAt: kotlin.String? = null,

    @field:JsonProperty("done")
    val done: kotlin.Boolean? = null,

    @field:JsonProperty("step")
    val step: MarkDoneRequestTodo.Step? = null

) {

    /**
     *
     *
     * Values: onboarding
     */
    enum class Type(val value: kotlin.String) {
        @JsonProperty(value = "onboarding") onboarding("onboarding");
    }
    /**
     *
     *
     * Values: makerflow
     */
    enum class SourceType(val value: kotlin.String) {
        @JsonProperty(value = "makerflow") makerflow("makerflow");
    }
    /**
     *
     *
     * Values: chatMinusIntegration,repoMinusIntegration,calendarMinusIntegration,cliMinusDownload,editorMinusIntegration,browserMinusExtension
     */
    enum class Step(val value: kotlin.String) {
        @JsonProperty(value = "chat-integration") chatMinusIntegration("chat-integration"),
        @JsonProperty(value = "repo-integration") repoMinusIntegration("repo-integration"),
        @JsonProperty(value = "calendar-integration") calendarMinusIntegration("calendar-integration"),
        @JsonProperty(value = "cli-download") cliMinusDownload("cli-download"),
        @JsonProperty(value = "editor-integration") editorMinusIntegration("editor-integration"),
        @JsonProperty(value = "browser-extension") browserMinusExtension("browser-extension");
    }
}

