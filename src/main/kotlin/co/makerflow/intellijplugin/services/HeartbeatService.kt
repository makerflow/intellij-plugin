package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.ProductiveActivityApi
import co.makerflow.intellijplugin.MyBundle
import co.makerflow.intellijplugin.actions.DontAskForApiKeyAgainNotificationAction
import co.makerflow.intellijplugin.actions.SetApiKeyNotificationAction
import co.makerflow.intellijplugin.providers.ApiClientProvider
import co.makerflow.intellijplugin.providers.ApiTokenProvider
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.UIUtil
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Service
class HeartbeatService {

    private val activityTimestamps = mutableSetOf<Long>()
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("Makerflow")

    private var apiKeyNotificationShown = 0
    private fun promptForApiTokenNotification() = notificationGroup
        .createNotification(
            MyBundle.getMessage("makerflow-apikey.notification.body"),
            NotificationType.WARNING
        )
        .setTitle(MyBundle.getMessage("makerflow-apikey.notification.title"))
        .addAction(SetApiKeyNotificationAction())
        .addAction(DontAskForApiKeyAgainNotificationAction())
        .whenExpired {
            apiKeyNotificationShown = 0
        }

    fun heartbeat() {
        activityTimestamps.add(System.currentTimeMillis())
    }

    init {
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            if (activityTimestamps.size >= 2) {

                val apiToken = getApiToken()
                if (apiToken.isEmpty() && !SettingsState.instance.dontShowApiTokenPrompt) {
                    if (apiKeyNotificationShown == 0) {
                        ProjectManager.getInstance().openProjects.forEach { promptForApiTokenNotification().notify(it) }
                        apiKeyNotificationShown++
                    }
                } else if (apiToken.isNotEmpty()) {
                    UIUtil.invokeLaterIfNeeded {
                        CoroutineScope(Dispatchers.IO).launch {
                            send(apiToken)
                        }
                    }
                }
            }
        }, INTERVAL, INTERVAL, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private fun getApiToken(): String {
        return service<ApiTokenProvider>().getApiToken()
    }

    private suspend fun send(
        apiToken: String
    ) {
        val list = activityTimestamps.toMutableList()
        activityTimestamps.clear()
        list.sort()
        val api = service<ApiClientProvider>().provide(ProductiveActivityApi::class.java) ?: return
        api.setApiKey(apiToken)
        return coroutineScope {
            @Suppress("TooGenericExceptionCaught")
            launch {
                try {
                    val logProductiveActivity =
                        api.logProductiveActivity(list, "jetbrains")
                    if (!logProductiveActivity.success) {
                        activityTimestamps.addAll(list)
                    }
                } catch (e: Exception) {
                    activityTimestamps.addAll(list)
                    @Suppress("kotlin:S125")
                    when (e) {
                        is HttpRequestTimeoutException,
                        is ConnectTimeoutException,
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank
                           thisLogger().error(e)
                           thisLogger().error("Error converting ongoing flow mode: ${e.message}")
                           */
                        }

                        else -> throw e
                    }
                }
            }
        }.join()
    }

    companion object {
        private const val INTERVAL = 30000L
    }
}
