package co.makerflow.intellijplugin.settings

import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UI
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
        get() = apiToken.password.joinToString("")
        set(newText) {
            apiToken.text = newText
        }
    private val comment =
        "<a target=\"_blank\" href=\"https://app.makerflow.co/settings#/api\">Click here</a> to get your API token."

    init {
        val component = UI.PanelFactory.panel(apiToken)
            .withLabel("Enter API token:")
            .withComment(comment)
            .createPanel()
        panel = FormBuilder.createFormBuilder()
            .addComponent(component, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
