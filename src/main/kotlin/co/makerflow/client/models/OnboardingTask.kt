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
 * @param sourceType
 * @param type
 * @param createdAt
 * @param done
 * @param step
 */


data class OnboardingTask (


    @field:JsonProperty("step")
    val step: OnboardingTask.Step? = null

): TypedTodo() {

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

