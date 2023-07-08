package co.makerflow.intellijplugin.panels

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

@Suppress("UnstableApiUsage")
class CalendarEventsToolWindowFactory: ToolWindowFactory, DumbAware, LightEditCompatible {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = UpcomingEventsPanel()
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(panel, null, false)
        contentManager.addContent(content)
        toolWindow.setTitleActions(listOf(panel.reloadAction))
    }
}
