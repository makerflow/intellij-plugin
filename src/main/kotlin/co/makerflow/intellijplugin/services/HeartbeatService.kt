package co.makerflow.intellijplugin.services

import co.makerflow.intellijplugin.MyBundle
import co.makerflow.intellijplugin.notification.DontAskForApiKeyAgainNotification
import co.makerflow.intellijplugin.notification.DontShowFlowModeStartedNotificationAgain
import co.makerflow.intellijplugin.notification.SetApiKeyNotification
import co.makerflow.intellijplugin.settings.SettingsState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager
import kotlin.concurrent.fixedRateTimer

class HeartbeatService {

    private val activityTimestamps = arrayListOf<Long>()
    private val notificationGroup =
        NotificationGroup("Makerflow notifications", NotificationDisplayType.BALLOON, true)
    private fun flowModeStartedNotification() = notificationGroup
        .createNotification(
            "Flow Mode started",
            "",
            NotificationType.INFORMATION
        )
        .addAction(DontShowFlowModeStartedNotificationAgain())
    private fun promptForApiTokenNotification() = notificationGroup
        .createNotification(
            MyBundle.getMessage("makerflow-apikey.notification.body"),
            NotificationType.WARNING
        )
        .setTitle(MyBundle.getMessage("makerflow-apikey.notification.title"))
        .addAction(SetApiKeyNotification())
        .addAction(DontAskForApiKeyAgainNotification())

    fun heartbeat() {
        activityTimestamps.add(System.currentTimeMillis())
    }

    data class ProductiveActivityResponse(var success: Boolean, var flowModeAlreadyOngoing: Boolean)

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
                    ProjectManager.getInstance().openProjects.forEach { promptForApiTokenNotification().notify(it) }
                } else if (apiToken.isNotEmpty()) {
                    Fuel.post("https://app.makerflow.co/api/productive-activity?api_token=$apiToken")
                        .objectBody(nodes)
                        .responseObject(object : ResponseHandler<ProductiveActivityResponse> {
                            override fun success(
                                request: Request,
                                response: Response,
                                value: ProductiveActivityResponse
                            ) {
                                if (showFlowModeStartedNotification(value)) {
                                    println("showing notification")
                                    ProjectManager.getInstance().openProjects.forEach {
                                        flowModeStartedNotification().notify(it)
                                    }
                                }
                            }

                            override fun failure(request: Request, response: Response, error: FuelError) {
                                activityTimestamps.addAll(list)
                            }
                        })
                }
            }
        }
    }

    private fun showFlowModeStartedNotification(value: ProductiveActivityResponse) =
        !SettingsState.instance.dontShowFlowModeStartedNotification && value.success && value.flowModeAlreadyOngoing

    companion object {
        private const val INTERVAL = 30000L
    }
}
