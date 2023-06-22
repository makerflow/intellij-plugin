package co.makerflow.intellijplugin.listeners

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.ui.ClickListener
import java.awt.Component
import java.awt.event.MouseEvent

class FlowModeStatusBarWidgetClickListener(private val component: Component?) : ClickListener() {
    override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
        val action: AnAction = ActionManager.getInstance().getAction("ToggleFlowMode")
        ActionManager.getInstance().tryToExecute(action, event, component, null, true)
        return true
    }
}
