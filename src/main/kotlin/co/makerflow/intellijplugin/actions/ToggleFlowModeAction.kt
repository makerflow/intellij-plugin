package co.makerflow.intellijplugin.actions

import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.services.toFlow
import co.makerflow.intellijplugin.state.Flow
import co.makerflow.intellijplugin.state.FlowState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import kotlinx.coroutines.*
import kotlinx.datetime.Instant

class ToggleFlowModeAction : AnAction("Toggle Flow Mode", "Begin or end flow mode based on current status", null) {

    private val startFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val stopFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun actionPerformed(e: AnActionEvent) {
        FlowState.instance.processing = true
        val flowModeService = service<FlowModeService>()
        if (FlowState.instance.currentFlow == null) {
            ApplicationManager.getApplication().invokeLater {
                startFlowModeCoroutineScope.launch {
                    flowModeService.startFlowMode()?.let { flowMode ->
                        FlowState.instance.currentFlow = flowMode.toFlow()
                    }
                }.invokeOnCompletion {
                    FlowState.instance.processing = false
                }
            }
        } else {
            ApplicationManager.getApplication().invokeLater {
                stopFlowModeCoroutineScope.launch {
                    flowModeService.stopFlowMode()
                    FlowState.instance.currentFlow = null
                }.invokeOnCompletion {
                    FlowState.instance.processing = false
                }
            }
        }
    }

}
