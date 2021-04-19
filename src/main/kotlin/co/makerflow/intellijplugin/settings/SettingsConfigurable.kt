package co.makerflow.intellijplugin.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class SettingsConfigurable : Configurable {
    private var mySettingsComponent: SettingsComponent? = null

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Makerflow"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        mySettingsComponent = co.makerflow.intellijplugin.settings.SettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings: SettingsState = SettingsState.instance
        return mySettingsComponent!!.apiTokenText != settings.apiToken
    }

    override fun apply() {
        val settings: SettingsState = SettingsState.instance
        settings.apiToken = mySettingsComponent!!.apiTokenText
    }

    override fun reset() {
        val settings: SettingsState = SettingsState.instance
        mySettingsComponent?.apiTokenText = settings.apiToken
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
