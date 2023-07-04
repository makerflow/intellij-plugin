package co.makerflow.intellijplugin.actions.flowmode

import co.makerflow.intellijplugin.state.Flow
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.FlowStateChangeNotifier
import com.intellij.ide.actions.searcheverywhere.PossibleSlowContributor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ui.UIUtil

const val STOP_FLOW_MODE_DIRECTIVE = "Stop Flow Mode"

private const val STOP_FLOW_MODE_DESCRIPTION = "Stop the ongoing Flow Mode session"

class FlowModeActionGroup : DefaultActionGroup(), PossibleSlowContributor {
    init {
        UIUtil.invokeLaterIfNeeded {
            if (FlowState.instance.currentFlow != null) {
                updateTemplateForStoppingFlowMode()
            } else {
                updateTemplateForStartingFlowMode()
            }
        }
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(FlowStateChangeNotifier.FLOW_STATE_CHANGE_TOPIC, object : FlowStateChangeNotifier {
                override fun updated(flow: Flow?, processing: Boolean) {
                    UIUtil.invokeLaterIfNeeded {
                        if (flow != null) {
                            updateTemplateForStoppingFlowMode()
                            updateTimedFlowModeActions()
                        } else {
                            updateTemplateForStartingFlowMode()
                            updateTimedFlowModeActions()
                        }
                    }
                }
            })
    }

    override fun update(e: AnActionEvent) {
        if (FlowState.instance.currentFlow != null) {
            e.presentation.text = STOP_FLOW_MODE_DIRECTIVE
            e.presentation.description = STOP_FLOW_MODE_DESCRIPTION
            e.presentation.isEnabled = false
        } else {
            e.presentation.text = "Choose Flow Mode Session Type"
            e.presentation.description = "Choose a Flow Mode type to start a new session"
            e.presentation.isEnabled = true
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun updateTemplateForStoppingFlowMode() {
        update(buildActionEvent(this))
        val toggleFlowModeAction = ActionManager.getInstance()
            .getAction("co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction")
        toggleFlowModeAction.update(
            buildActionEvent(toggleFlowModeAction)
        )
    }

    private fun updateTemplateForStartingFlowMode() {
        update(buildActionEvent(this))
        val toggleFlowModeAction = ActionManager.getInstance()
            .getAction("co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction")
        toggleFlowModeAction.update(
            buildActionEvent(toggleFlowModeAction)
        )
    }

    private fun updateTimedFlowModeActions() {
        val twentyFiveMinutesFlowModeAction = ActionManager.getInstance()
            .getAction("co.makerflow.intellijplugin.actions.flowmode.TwentyFiveMinutesFlowModeAction")
        twentyFiveMinutesFlowModeAction.update(
            buildActionEvent(twentyFiveMinutesFlowModeAction)
        )
        val fiftyMinutesFlowModeAction = ActionManager.getInstance()
            .getAction("co.makerflow.intellijplugin.actions.flowmode.FiftyMinutesFlowModeAction")
        fiftyMinutesFlowModeAction.update(
            buildActionEvent(fiftyMinutesFlowModeAction)
        )
        val seventyFiveMinutesFlowModeAction = ActionManager.getInstance()
            .getAction("co.makerflow.intellijplugin.actions.flowmode.SeventyFiveMinutesFlowModeAction")
        seventyFiveMinutesFlowModeAction.update(
            buildActionEvent(seventyFiveMinutesFlowModeAction)
        )
    }

    private fun buildActionEvent(action: AnAction) =
        AnActionEvent.createFromAnAction(
            action,
            null,
            "",
            DataContext.EMPTY_CONTEXT
        )

}
