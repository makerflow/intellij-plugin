package co.makerflow.intellijplugin.notification

import co.makerflow.intellijplugin.settings.SettingsState
import com.github.kittinunf.fuel.Fuel
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent

class StopFlowModeNotificationAction : NotificationAction("Stop") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        val apiToken = SettingsState.instance.apiToken
        Fuel.post("https://app.makerflow.co/api/flow-mode/stop?api_token==$apiToken").response()
        notification.expire()
    }
}
