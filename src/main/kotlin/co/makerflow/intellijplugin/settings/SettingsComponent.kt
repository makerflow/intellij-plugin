package co.makerflow.intellijplugin.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class SettingsComponent {
    val panel: JPanel
    private val apiToken = JBPasswordField()
    val preferredFocusedComponent: JComponent
        get() = apiToken
    var apiTokenText: String
        get() = apiToken.text
        set(newText) {
            apiToken.text = newText
        }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter API Token: "), apiToken, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
