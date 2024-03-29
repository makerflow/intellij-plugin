package co.makerflow.intellijplugin.status

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory

@Suppress("UnstableApiUsage")
class MakerflowStatusBarWidgetFactory : StatusBarEditorBasedWidgetFactory(), LightEditCompatible {

    private val id = "co.makerflow.intellijplugin.MakerflowStatusBarWidgetFactory"
    override fun getId(): String {
        return id
    }

    override fun getDisplayName(): String {
        @Suppress("DialogTitleCapitalization")
        return "Flow Mode status"
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return MakerflowStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }
}
