package co.makerflow.intellijplugin.actions

import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StopFlowModeNotificationAction : NotificationAction("Stop") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        val apiToken = SettingsState.instance.apiToken
        ApiClient(ApiClient.BASE_URL, null, null, ApiClient.JSON_DEFAULT).setApiKey(apiToken)
        CoroutineScope(Dispatchers.IO).launch {
            service<FlowModeService>().stopFlowMode()
        }
        notification.expire()
    }
}
