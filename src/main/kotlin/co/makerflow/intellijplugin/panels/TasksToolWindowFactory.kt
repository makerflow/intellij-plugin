package co.makerflow.intellijplugin.panels

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TasksToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val tasksPanel = TasksPanel()
        val content = contentManager.factory.createContent(tasksPanel, null, true)
        contentManager.addContent(content)
        toolWindow.setTitleActions(
            listOf(
                tasksPanel.reloadAction,
                ActionManager.getInstance().getAction("co.makerflow.intellijplugin.actions.tasks.AddCustomTaskAction")
            )
        )
    }
}
