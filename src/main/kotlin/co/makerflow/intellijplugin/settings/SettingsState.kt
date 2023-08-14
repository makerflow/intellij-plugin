package co.makerflow.intellijplugin.settings

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "co.makerflow.intellijplugin.settings.SettingsState", storages = [Storage("MakerflowSettings.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var apiToken: String
        get() {
            val credentialAttributes = service<CredentialAttributesProvider>().getCredentialAttributes()
            return PasswordSafe.instance.getPassword(credentialAttributes).orEmpty()
        }
        set(value) {
            val credentialAttributes = service<CredentialAttributesProvider>().getCredentialAttributes()
            val credentials = Credentials("user", value)
            PasswordSafe.instance[credentialAttributes] = credentials
        }
    var dontShowApiTokenPrompt = false
    var dontShowFlowModeStartedNotification = false
    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val state = SettingsState()
        val instance: SettingsState
            get() = state
    }
}
