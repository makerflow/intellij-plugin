package co.makerflow.intellijplugin.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.openapi.components.Service

@Service
class CredentialAttributesProvider {

    fun getCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(generateServiceName("co.makerflow", "apiToken"))
    }
}
