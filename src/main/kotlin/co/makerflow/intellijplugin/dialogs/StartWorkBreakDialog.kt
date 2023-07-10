package co.makerflow.intellijplugin.dialogs

import co.makerflow.client.models.BreakReason
import co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction
import co.makerflow.intellijplugin.services.WorkBreakService
import co.makerflow.intellijplugin.state.FlowState
import co.makerflow.intellijplugin.state.WorkBreakState
import co.makerflow.intellijplugin.state.toBreak
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jdesktop.swingx.action.ActionManager
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.JComponent


/**
 * A dialog to start a work break by selecting a reason.
 */
class StartWorkBreakDialog : DialogWrapper(false) {

    private lateinit var reasonField: Cell<ComboBox<String>>
    private val propertyGraph = PropertyGraph()
    private val chosenReason = propertyGraph.property<BreakReason?>(null)

    init {
        title = "Makerflow: Start Work Break"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        // A panel where user can select a reason for the work break
        // from a dropdown menu whose options are values of BreakReason enum
        val reasons = BreakReason.values().toList().map { reason ->
            reason.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        return panel {
            row {
                comboBox(reasons)
                    .onChanged {
                        it.selectedItem?.let { reason ->
                            chosenReason.set(BreakReason.valueOf((reason as String).uppercase()))
                        }
                    }.apply {
                        chosenReason.set(BreakReason.valueOf(reasons[0].uppercase()))
                        reasonField = this
                    }
            }
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (chosenReason.get() == null) {
            return ValidationInfo("Please select a reason", reasonField.component)
        }
        return null
    }

    private val startWorkBreakCoroutineScope = CoroutineScope(Dispatchers.IO)
    override fun doOKAction() {
        super.doOKAction()
        // Start a work break with the chosen reason by calling service method
        // and send a message on the message bus so the panel knows to reload
        startWorkBreakCoroutineScope.launch {
            chosenReason.get()?.let { reason ->
                if (FlowState.isInFlow()) {
                    val toggleFlowModeAction = ActionManager.getInstance()
                        .getAction(" co.makerflow.intellijplugin.actions.flowmode.ToggleFlowModeAction")
                    toggleFlowModeAction
                        .actionPerformed(
                            ActionEvent(this, ActionEvent.ACTION_FIRST, "Toggle Flow Mode")
                        )
                }
                WorkBreakState.instance.processing = true
                service<WorkBreakService>().startWorkBreak(reason)?.let { workBreak ->
                    WorkBreakState.instance.currentBreak = workBreak.toBreak()
                }
                WorkBreakState.instance.processing = false
            }
        }
    }

}
