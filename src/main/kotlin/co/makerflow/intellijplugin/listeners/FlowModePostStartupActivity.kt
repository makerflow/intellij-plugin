package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.MyBundle
import co.makerflow.intellijplugin.actions.DontShowFlowModeStartedNotificationAgain
import co.makerflow.intellijplugin.actions.StopFlowModeNotificationAction
import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.services.HeartbeatService
import co.makerflow.intellijplugin.services.toFlow
import co.makerflow.intellijplugin.settings.SettingsState
import co.makerflow.intellijplugin.state.FlowState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


private const val DELAY_BETWEEN_FETCHES = 10L

class FlowModePostStartupActivity : ProjectActivity {

    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("Makerflow")

    private var flowModeStartedNotificationShown = false
    private fun showFlowModeStartedNotification() = notificationGroup
        .createNotification(
            MyBundle.getMessage("makerflow.notification.flowMode.started.body"),
            NotificationType.INFORMATION
        )
        .setTitle(MyBundle.getMessage("makerflow.notification.flowMode.started.title"))
        .addAction(StopFlowModeNotificationAction())
        .addAction(DontShowFlowModeStartedNotificationAgain())
        .whenExpired {
            flowModeStartedNotificationShown = false
        }

    override suspend fun execute(project: Project) {

        service<HeartbeatService>().heartbeat()

        // Fetch ongoing flow mode at regular intervals
        val checking = AtomicBoolean()
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            ApplicationManager.getApplication().invokeLater {
                if (checking.get() || FlowState.isStarting() || FlowState.isStopping()) {
                    return@invokeLater
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        checking.set(true)
                        val flowModeService = service<FlowModeService>()
                        val ongoingFlowMode = flowModeService.fetchOngoingFlowMode()
                        ongoingFlowMode.let { pair ->
                            val flowModeStartedOutside = (!FlowState.isStarting()
                                    && pair.first != null
                                    && FlowState.instance.currentFlow == null)
                            val showingNotificationWontBeAnnoying = !flowModeStartedNotificationShown
                                    && !SettingsState.instance.dontShowFlowModeStartedNotification
                            if (flowModeStartedOutside && showingNotificationWontBeAnnoying) {
                                showFlowModeStartedNotification().notify(project)
                                flowModeStartedNotificationShown = true
                            }
                            FlowState.instance.currentFlow = pair.first?.toFlow(pair.second)
                        }
                    } finally {
                        checking.set(false)
                    }
                }
            }
        }, 0, DELAY_BETWEEN_FETCHES, java.util.concurrent.TimeUnit.SECONDS)
    }

}

