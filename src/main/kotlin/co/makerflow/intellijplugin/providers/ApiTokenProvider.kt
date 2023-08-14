package co.makerflow.intellijplugin.providers

import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.DumbAware

@Service
class ApiTokenProvider {
    fun getApiToken(): String {
        val forceEmptyApiToken = System.getenv("MAKERFLOW_FORCE_EMPTY_API_TOKEN")
        val devApiToken = System.getenv("MAKERFLOW_API_TOKEN")
        return if (forceEmptyApiToken.isNullOrEmpty().not() && forceEmptyApiToken.equals("true", true)) {
            ""
        } else if (devApiToken.isNullOrEmpty().not()) {
            devApiToken
        } else {
            SettingsState.instance.apiToken
        }
    }
}
