package co.makerflow.intellijplugin.actions.tasks

import co.makerflow.intellijplugin.dialogs.AddTaskDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddCustomTaskAction : AnAction(
    "Makerflow: Add Task",
    "Add a new task to the Makerflow task list",
    AllIcons.Actions.AddList
) {
    override fun actionPerformed(e: AnActionEvent) {
        AddTaskDialog().showAndGet()
    }
}
