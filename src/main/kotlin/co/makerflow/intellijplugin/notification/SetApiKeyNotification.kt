package co.makerflow.intellijplugin.notification

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class SetApiKeyNotification : NotificationAction("Set token") {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, "Makerflow")
        notification.expire()
    }
}
