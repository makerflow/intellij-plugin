package co.makerflow.intellijplugin.actions.flowmode

import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.WorkBreakState
import com.intellij.openapi.actionSystem.AnActionEvent

private const val START_FLOW_MODE_DIRECTIVE = "Start Flow Mode (Without Timer)"
private const val START_FLOW_MODE_DESCRIPTION = "Begin an untimed Flow Mode session"

class ToggleFlowModeAction :
    FlowModeAction(
        START_FLOW_MODE_DIRECTIVE,
        START_FLOW_MODE_DESCRIPTION
    ) {
    override fun update(e: AnActionEvent) {
        if (WorkBreakState.isOngoing()) {
            e.presentation.isEnabled = false
            e.presentation.text = "Stop Break to Start Flow Mode"
            e.presentation.description = "You are currently on break. Stop the break to start a Flow Mode session."
        } else if (FlowState.instance.currentFlow == null) {
            e.presentation.isEnabled = true
            e.presentation.text = START_FLOW_MODE_DIRECTIVE
            e.presentation.description = START_FLOW_MODE_DESCRIPTION
        } else {
            e.presentation.isEnabled = true
            e.presentation.text = STOP_FLOW_MODE_DIRECTIVE
            e.presentation.description = "Stop the ongoing Flow Mode session"
        }
    }

}
