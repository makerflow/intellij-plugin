package co.makerflow.intellijplugin.actions.flowmode

import co.makerflow.intellijplugin.state.FlowState
import com.intellij.openapi.actionSystem.AnActionEvent

private const val START_FLOW_MODE_DIRECTIVE = "Start Flow Mode (Without Timer)"
private const val START_FLOW_MODE_DESCRIPTION = "Begin an untimed Flow Mode session"

class ToggleFlowModeAction :
    FlowModeAction(
        START_FLOW_MODE_DIRECTIVE,
        START_FLOW_MODE_DESCRIPTION
    ) {
    override fun update(e: AnActionEvent) {
        if (FlowState.instance.currentFlow == null) {
            e.presentation.text = START_FLOW_MODE_DIRECTIVE
            e.presentation.description = START_FLOW_MODE_DESCRIPTION
        } else {
            e.presentation.text = STOP_FLOW_MODE_DIRECTIVE
            e.presentation.description = "Stop the ongoing Flow Mode session"
        }
    }

}
