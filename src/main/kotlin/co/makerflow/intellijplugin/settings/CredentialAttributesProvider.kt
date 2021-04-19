package co.makerflow.intellijplugin.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName

class CredentialAttributesProvider {

    fun getCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(generateServiceName("co.makerflow", "apiToken"))
    }
}
