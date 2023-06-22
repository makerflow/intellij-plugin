package co.makerflow.intellijplugin.actions

import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DontAskForApiKeyAgainNotificationAction : NotificationAction("Don't ask again") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        SettingsState.instance.dontShowApiTokenPrompt = true
        notification.expire()
    }
}
