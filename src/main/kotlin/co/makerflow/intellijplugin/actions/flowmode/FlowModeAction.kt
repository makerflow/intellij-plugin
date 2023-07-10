package co.makerflow.intellijplugin.actions.flowmode

import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.services.toFlow
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.WorkBreakState
import com.intellij.ide.actions.searcheverywhere.PossibleSlowContributor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class FlowModeAction(
    text: String,
    description: String,
    private val duration: Int? = null
) :
    AnAction(text, description, null), PossibleSlowContributor {
    private val startFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val stopFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)
    fun stopFlowMode() {
        FlowState.instance.processing = true
        val flowModeService = service<FlowModeService>()
        ApplicationManager.getApplication().invokeLater {
            stopFlowModeCoroutineScope.launch {
                flowModeService.stopFlowMode()
                FlowState.instance.currentFlow = null
            }.invokeOnCompletion {
                FlowState.instance.processing = false
            }
        }
    }

    private fun startFlowMode() {
        FlowState.instance.processing = true
        val flowModeService = service<FlowModeService>()
        ApplicationManager.getApplication().invokeLater {
            startFlowModeCoroutineScope.launch {
                flowModeService.startFlowMode(null, duration)?.let { flowMode ->
                    FlowState.instance.currentFlow = flowMode.toFlow()
                }
            }.invokeOnCompletion {
                FlowState.instance.processing = false
            }
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (FlowState.instance.currentFlow == null) {
            startFlowMode()
        } else {
            stopFlowMode()
        }
    }

    override fun update(e: AnActionEvent) {
        val flowState = FlowState.instance
        if (duration != null) {
            e.presentation.isEnabled = if (WorkBreakState.isOngoing()) {
                false
            } else {
                !flowState.processing && flowState.currentFlow == null
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
