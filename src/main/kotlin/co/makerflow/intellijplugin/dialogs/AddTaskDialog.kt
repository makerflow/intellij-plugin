package co.makerflow.intellijplugin.dialogs

import co.makerflow.intellijplugin.services.TasksService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.swing.JComponent

class AddTaskDialog : DialogWrapper(false) {

    private val propertyGraph = PropertyGraph()
    private val taskName = propertyGraph.property("")
    private lateinit var taskField: Cell<JBTextField>

    init {
        title = "Makerflow: Add Task"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                textField()
                    .bindText(taskName)
                    .apply { taskField = this }
                    .comment("What do you want to accomplish?")
            }
        }
    }

    override fun getPreferredFocusedComponent(): JComponent = taskField.component

    override fun doValidate(): ValidationInfo? {
        if (taskName.get().isBlank()) {
            return ValidationInfo("Task cannot be empty", taskField.component)
        }
        return null
    }

    private val addTaskCoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun doOKAction() {
        super.doOKAction()
        ApplicationManager.getApplication().invokeLater {
            addTaskCoroutineScope.launch {
                service<TasksService>().addTask(taskName.get())?.let {
                    // send a message on the message bus so the panel knows to reload
                    ApplicationManager.getApplication().messageBus.syncPublisher(TasksService.TASKS_ADDED_TOPIC)
                        .taskAdded(it)
                }
            }
        }
    }
}
