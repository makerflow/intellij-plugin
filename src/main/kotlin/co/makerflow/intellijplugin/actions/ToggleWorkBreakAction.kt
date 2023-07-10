package co.makerflow.intellijplugin.actions

import co.makerflow.intellijplugin.dialogs.StartWorkBreakDialog
import co.makerflow.intellijplugin.services.WorkBreakService
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.WorkBreakState
import co.makerflow.intellijplugin.state.WorkBreakStateChangeNotifier
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Action to toggle work break
 */
class ToggleWorkBreakAction : AnAction("Begin Break", "Take a break from work", AllIcons.Actions.Pause) {

    init {
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(
                WorkBreakStateChangeNotifier.WORK_BREAK_STATE_CHANGE_TOPIC,
                WorkBreakStateChangeNotifier { _, _ ->
                    UIUtil.invokeLaterIfNeeded {
                        update(AnActionEvent.createFromAnAction(
                            this,
                            null,
                            "",
                            DataContext.EMPTY_CONTEXT
                        ))
                    }
                })
    }
    override fun update(e: AnActionEvent) {
        if (WorkBreakState.isOngoing().not()) {
            e.presentation.text = "Begin Break"
            e.presentation.description = "Take a break from work."
        } else {
            e.presentation.text = "Stop Break"
            e.presentation.description = "Stop the ongoing break session."
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    private val stopWorkBreakCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun actionPerformed(e: AnActionEvent) {
        if (WorkBreakState.isOngoing().not()) {
            StartWorkBreakDialog().show()
        } else {
            stopWorkBreakCoroutineScope.launch {
                WorkBreakState.instance.processing = true
                service<WorkBreakService>().stopWorkBreak().let {
                    WorkBreakState.instance.currentBreak = null
                }
                WorkBreakState.instance.processing = false
            }
        }
    }
}
