package co.makerflow.intellijplugin.status

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory

@Suppress("UnstableApiUsage")
class FlowStatusBarWidgetFactory : StatusBarEditorBasedWidgetFactory(), LightEditCompatible {
    override fun getId(): String {
        return "MakerflowFlowStatus"
    }

    override fun getDisplayName(): String {
        return "Flow Mode status"
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return co.makerflow.intellijplugin.status.FlowStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }
}
