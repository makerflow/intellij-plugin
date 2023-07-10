package co.makerflow.intellijplugin.status

import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.FlowStateChangeNotifier
import co.makerflow.intellijplugin.state.WorkBreakState
import co.makerflow.intellijplugin.state.WorkBreakStateChangeNotifier
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import com.intellij.util.ui.UIUtil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.annotations.NotNull
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Component


class MakerflowStatusBarWidget(project: @NotNull Project) : EditorBasedStatusBarPopup(project, false),
    StatusBarWidget.TextPresentation {

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return this
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState = WidgetState(getTooltipText(), getText(), true)

    override fun ID(): String {
        return "MakerflowFlowStatusBarWidget"
    }

    override fun createInstance(project: Project): StatusBarWidget = MakerflowStatusBarWidget(project)

    override fun createPopup(context: DataContext): ListPopup? {
        val actions = mutableListOf<AnAction>()
        val actionManager = ActionManager.getInstance()
        actions.add(
            actionManager.getAction(
                "co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction"
            )
        )
        actions.add(
            actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.TwentyFiveMinutesFlowModeAction")
        )
        actions.add(
            actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.FiftyMinutesFlowModeAction")
        )
        actions.add(
            actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.SeventyFiveMinutesFlowModeAction")
        )
        actions.add(
            actionManager.getAction("co.makerflow.intellijplugin.actions.ToggleWorkBreakAction")
        )
        val actionGroup: ActionGroup = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions.toTypedArray()
        }
        val title = if (FlowState.instance.currentFlow == null) {
            if (WorkBreakState.instance.currentBreak == null) {
                "Start Flow Mode or Break"
            } else {
                "Stop Break"
            }
        } else {
            "Stop Flow Mode"
        }
        return JBPopupFactory.getInstance().createActionGroupPopup(
            title,
            actionGroup,
            context,
            JBPopupFactory.ActionSelectionAid.NUMBERING,
            false
        )
    }

    override fun install(statusBar: StatusBar) {
        super.install(statusBar)
        myConnection.subscribe(
            FlowStateChangeNotifier.FLOW_STATE_CHANGE_TOPIC,
            FlowStateChangeNotifier { _, _ ->
                UIUtil.invokeLaterIfNeeded {
                    updateComponent(getWidgetState(null))
                }
            }
        )
        myConnection.subscribe(
            WorkBreakStateChangeNotifier.WORK_BREAK_STATE_CHANGE_TOPIC,
            WorkBreakStateChangeNotifier { _, _ ->
                UIUtil.invokeLaterIfNeeded {
                    updateComponent(getWidgetState(null))
                }
            }
        )
    }

    override fun getTooltipText(): String {
        return when {
            FlowState.isInFlow().not() && WorkBreakState.isOngoing().not() -> {
                "You don't have Flow Mode or Break currently active. " +
                        "Click here to start a new Flow Mode or Break session."
            }

            FlowState.isInFlow() -> {
                val pair = getTimingInfo()
                val startedFromNow = pair.first
                val endingIn = pair.second
                "Flow Mode started $startedFromNow$endingIn. Click here to stop the current Flow Mode session."
            }


            FlowState.isStopping() -> {
                "Stopping Flow Mode..."
            }

            FlowState.isStarting() -> {
                "Starting Flow Mode..."
            }

            WorkBreakState.isOngoing() -> {
                val startedFromNow = getBreakTimingInfo()
                "Break started $startedFromNow. Click here to stop the current Break session."
            }

            WorkBreakState.isStarting() -> {
                "Starting Break..."
            }

            WorkBreakState.isStopping() -> {
                "Stopping Break..."
            }

            else -> {
                "Makerflow"
            }
        }
    }


    override fun getText(): String {
        return when {
            FlowState.isInFlow().not() && WorkBreakState.isOngoing().not() -> {
                "Not in Flow Mode or on Break"
            }

            FlowState.isInFlow() -> {
                val pair = getTimingInfo()
                val startedFromNow = pair.first
                val endingIn = pair.second
                "Flow Mode Started: $startedFromNow$endingIn"
            }

            FlowState.isStopping() -> {
                "Flow Mode: Stopping"
            }

            FlowState.isStarting() -> {
                "Flow Mode: Starting"
            }

            WorkBreakState.isOngoing() -> {
                val startedFromNow = getBreakTimingInfo()
                "On Break Since $startedFromNow."
            }

            WorkBreakState.isStarting() -> {
                "Break: Starting"
            }

            WorkBreakState.isStopping() -> {
                "Break: Stopping"
            }

            else -> {
                "Makerflow"
            }
        }
    }

    private fun getTimingInfo(): Pair<String, String> {
        val p = PrettyTime()
        val startedFromNow = p.format(
            FlowState.instance.currentFlow!!.start.toLocalDateTime(TimeZone.currentSystemDefault())
                .toJavaLocalDateTime()
        )
        var endingIn = ""
        if (FlowState.instance.currentFlow!!.scheduledEnd != null) {
            endingIn = " | Ending: ${
                p.format(
                    FlowState.instance.currentFlow!!.scheduledEnd!!.toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    ).toJavaLocalDateTime()
                )
            }"
        }
        return Pair(startedFromNow, endingIn)
    }

    private fun getBreakTimingInfo(): String {
        val p = PrettyTime()
        return p.format(
            WorkBreakState.instance.currentBreak!!.start.toLocalDateTime(TimeZone.currentSystemDefault())
                .toJavaLocalDateTime()
        )
    }

    override fun getAlignment(): Float {
        return Component.CENTER_ALIGNMENT
    }


}
