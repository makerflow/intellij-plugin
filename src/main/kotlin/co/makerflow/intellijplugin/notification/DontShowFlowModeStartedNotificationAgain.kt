package co.makerflow.intellijplugin.notification

import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DontShowFlowModeStartedNotificationAgain : NotificationAction("Don't show again") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        SettingsState.instance.dontShowFlowModeStartedNotification = true
        notification.expire()
    }
}
