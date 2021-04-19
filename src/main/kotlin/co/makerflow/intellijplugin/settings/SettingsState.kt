package co.makerflow.intellijplugin.settings

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "co.makerflow.intellijplugin.settings.SettingsState", storages = [Storage("MakerflowSettings.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var apiToken: String
        get() {
            val credentialAttributes =
                ServiceManager.getService(CredentialAttributesProvider::class.java).getCredentialAttributes()
            return PasswordSafe.instance.getPassword(credentialAttributes).orEmpty()
        }
        set(value) {
            val credentialAttributes =
                ServiceManager.getService(CredentialAttributesProvider::class.java).getCredentialAttributes()
            val credentials = Credentials("user", value)
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }
    var dontShowApiTokenPrompt = false
    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: SettingsState
            get() = ServiceManager.getService(SettingsState::class.java)
    }
}
