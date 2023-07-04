package co.makerflow.intellijplugin.status

import co.makerflow.intellijplugin.state.Flow
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.FlowStateChangeNotifier
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


class FlowStatusBarWidget(project: @NotNull Project) : EditorBasedStatusBarPopup(project, false),
    StatusBarWidget.TextPresentation {

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return this
    }

    override fun getWidgetState(file: VirtualFile?): WidgetState = WidgetState(getTooltipText(), getText(), true)

    override fun ID(): String {
        return "MakerflowFlowStatusBarWidget"
    }

    override fun createInstance(project: Project): StatusBarWidget = FlowStatusBarWidget(project)

    override fun createPopup(context: DataContext): ListPopup? {
        if (FlowState.instance.processing) {
            return null
        }
        val actions = mutableListOf<AnAction>()
        val actionManager = ActionManager.getInstance()
        actions.add(
            actionManager.getAction(
                "co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction"
            )
        )
        if (FlowState.instance.currentFlow == null) {
            actions.add(
                actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.TwentyFiveMinutesFlowModeAction")
            )
            actions.add(
                actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.FiftyMinutesFlowModeAction")
            )
            actions.add(
                actionManager.getAction("co.makerflow.intellijplugin.actions.flowmode.SeventyFiveMinutesFlowModeAction")
            )
        }
        val actionGroup: ActionGroup = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions.toTypedArray()
        }

        return JBPopupFactory.getInstance().createActionGroupPopup(
            if (FlowState.instance.currentFlow == null) "Start Flow Mode" else "Stop Flow Mode",
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
            object : FlowStateChangeNotifier {
                override fun updated(flow: Flow?, processing: Boolean) {
                    UIUtil.invokeLaterIfNeeded {
                        updateComponent(getWidgetState(null))
                    }
                }
            }
        )
    }

    override fun getTooltipText(): String {
        return when {
            FlowState.instance.currentFlow == null && !FlowState.instance.processing -> {
                "Flow Mode is not currently active. Click here to start a new Flow Mode session."
            }

            FlowState.instance.currentFlow != null && !FlowState.instance.processing -> {
                val pair = getTimingInfo()
                val startedFromNow = pair.first
                val endingIn = pair.second
                "Flow Mode started $startedFromNow$endingIn. Click here to stop the current Flow Mode session."
            }

            FlowState.instance.currentFlow != null && FlowState.instance.processing -> {
                "Stopping Flow Mode..."
            }

            else -> {
                "Starting Flow Mode..."
            }
        }
    }


    override fun getText(): String {
        return when {
            FlowState.instance.currentFlow == null && !FlowState.instance.processing -> {
                "Flow Mode: Stopped"
            }

            FlowState.instance.currentFlow != null && !FlowState.instance.processing -> {
                val pair = getTimingInfo()
                val startedFromNow = pair.first
                val endingIn = pair.second
                "Flow Mode Started: $startedFromNow$endingIn"
            }

            FlowState.instance.currentFlow != null && FlowState.instance.processing -> {
                "Flow Mode: Stopping"
            }

            else -> {
                "Flow Mode: Starting"
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

    override fun getAlignment(): Float {
        return Component.CENTER_ALIGNMENT
    }


}
