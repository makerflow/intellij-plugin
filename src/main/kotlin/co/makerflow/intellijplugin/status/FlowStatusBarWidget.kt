package co.makerflow.intellijplugin.status

import co.makerflow.intellijplugin.state.Flow
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.FlowStateChangeNotifier
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import com.intellij.util.ui.UIUtil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.annotations.NotNull
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Component
import java.awt.event.MouseEvent


class FlowStatusBarWidget(project: @NotNull Project) : EditorBasedWidget(project),
    StatusBarWidget.TextPresentation {

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return this
    }

    override fun ID(): String {
        return "MakerflowFlowStatusBarWidget"
    }

    override fun install(statusBar: StatusBar) {
        super.install(statusBar)
        myConnection.subscribe(
            FlowStateChangeNotifier.FLOW_STATE_CHANGE_TOPIC,
            object : FlowStateChangeNotifier {
                override fun updated(flow: Flow?, processing: Boolean) {
                    UIUtil.invokeLaterIfNeeded {
                        val statusBarHere = WindowManager.getInstance().getStatusBar(project)
                        statusBarHere?.updateWidget(ID())
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
                val p = PrettyTime()
                val startedFromNow = p.format(
                    FlowState.instance.currentFlow!!.start.toLocalDateTime(TimeZone.currentSystemDefault())
                        .toJavaLocalDateTime()
                )
                "Flow Mode started $startedFromNow. Click here to stop the current Flow Mode session."
            }

            FlowState.instance.currentFlow != null && FlowState.instance.processing -> {
                @Suppress("DialogTitleCapitalization")
                "Stopping Flow Mode..."
            }

            else -> {
                @Suppress("DialogTitleCapitalization")
                "Starting Flow Mode..."
            }
        }
    }

    override fun getClickConsumer(): Consumer<MouseEvent> {
        return Consumer<MouseEvent> { mouseEvent: MouseEvent ->
            val dataContext =
                DataManager.getInstance().getDataContext(mouseEvent.source as Component)
            val toggleFlowModeAction: AnAction = ActionManager.getInstance().getAction("ToggleFlowMode")
            val actionEvent = AnActionEvent.createFromDataContext("ACTION_PLACE", null, dataContext)
            toggleFlowModeAction.actionPerformed(actionEvent)
        }
    }


    @Suppress("DialogTitleCapitalization")
    override fun getText(): String {
        return when {
            FlowState.instance.currentFlow == null && !FlowState.instance.processing -> {
                "Flow Mode: Stopped"
            }

            FlowState.instance.currentFlow != null && !FlowState.instance.processing -> {
                val p = PrettyTime()
                val startedFromNow = p.format(
                    FlowState.instance.currentFlow!!.start.toLocalDateTime(TimeZone.currentSystemDefault())
                        .toJavaLocalDateTime()
                )
                "Flow Mode: Started $startedFromNow"
            }

            FlowState.instance.currentFlow != null && FlowState.instance.processing -> {
                "Flow Mode: Stopping"
            }

            else -> {
                "Flow Mode: Starting"
            }
        }
    }

    override fun getAlignment(): Float {
        return Component.CENTER_ALIGNMENT
    }


}
