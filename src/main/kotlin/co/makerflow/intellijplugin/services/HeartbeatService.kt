package co.makerflow.intellijplugin.services

import co.makerflow.intellijplugin.notification.DontAskForApiKeyAgainNotification
import co.makerflow.intellijplugin.notification.SetApiKeyNotification
import co.makerflow.intellijplugin.settings.SettingsState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager
import kotlin.concurrent.fixedRateTimer

class HeartbeatService {

    private val activityTimestamps = arrayListOf<Long>()
    private val notificationGroup =
        NotificationGroup("Makerflow notifications", NotificationDisplayType.BALLOON, true)

    fun heartbeat() {
        activityTimestamps.add(System.currentTimeMillis())
    }

    init {
        fixedRateTimer("heartbeatProcessor", false, INTERVAL, INTERVAL) {
            if (activityTimestamps.size > 0) {
                val list = activityTimestamps.toMutableList()
                activityTimestamps.clear()
                list.sort()
                val mapper = ObjectMapper()
                val nodes: ArrayNode = mapper.createArrayNode()
                nodes.add(list.first())
                nodes.add(list.last())
                val apiToken = SettingsState.instance.apiToken
                if (apiToken.isEmpty() && !SettingsState.instance.dontShowApiTokenPrompt) {
                    notificationGroup
                        .createNotification("Please set the Makerflow API key", NotificationType.WARNING)
                        .setTitle("Makerflow API key missing")
                        .addAction(SetApiKeyNotification())
                        .addAction(DontAskForApiKeyAgainNotification())
                        .notify(ProjectManager.getInstance().defaultProject)
                } else if (apiToken.isNotEmpty()) {
                    val httpAsync =
                        "https://app.makerflow.co/api/productive-activity?api_token=$apiToken"
                            .httpPost()
                            .jsonBody(nodes.toString())
                            .responseString { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        activityTimestamps.addAll(list)
                                    }
                                    is Result.Success -> TODO()
                                }
                            }

                    httpAsync.join()
                }
            }
        }
    }

    companion object {
        private const val INTERVAL = 30000L
    }
}
