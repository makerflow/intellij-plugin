package co.makerflow.intellijplugin.actions

import co.makerflow.client.apis.FlowModeApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StopFlowModeNotificationAction : NotificationAction("Stop") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        val apiToken = SettingsState.instance.apiToken
        ApiClient(ApiClient.BASE_URL, null, null, ApiClient.JSON_DEFAULT).setApiKey(apiToken)
        runBlocking {
            launch {
                FlowModeApi().stopOngoingFlowMode("jetbrains")
            }
        }
//        Fuel.post("https://app.makerflow.co/api/flow-mode/stop?api_token==$apiToken").response()
        notification.expire()
    }
}
