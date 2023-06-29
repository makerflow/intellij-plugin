package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.services.HeartbeatService
import co.makerflow.intellijplugin.services.toFlow
import co.makerflow.intellijplugin.state.FlowState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


private const val DELAY_BETWEEN_FETCHES = 10L

class FlowModeProjectManagerListener : ProjectManagerListener {

    // A new Job to fetch the ongoing flow mode
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    override fun projectOpened(project: Project) {

        service<HeartbeatService>().heartbeat()

        // Fetch ongoing flow mode at regular intervals
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            thisLogger().info("Fetching ongoing flow mode")
            ApplicationManager.getApplication().invokeLater {
                coroutineScope.launch {
                    val flowModeService = service<FlowModeService>()
                    val ongoingFlowMode = flowModeService.fetchOngoingFlowMode()
                    ongoingFlowMode.let { pair ->
                        FlowState.instance.currentFlow = pair.first?.toFlow(pair.second)
                    }
                }
            }
        }, 0, DELAY_BETWEEN_FETCHES, java.util.concurrent.TimeUnit.SECONDS)
    }

    override fun projectClosing(project: Project) {
        super.projectClosing(project)
        coroutineScope.cancel()
    }
}

