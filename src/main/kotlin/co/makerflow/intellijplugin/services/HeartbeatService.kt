package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.ProductiveActivityApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.intellijplugin.MyBundle
import co.makerflow.intellijplugin.actions.DontAskForApiKeyAgainNotificationAction
import co.makerflow.intellijplugin.actions.DontShowFlowModeStartedNotificationAgain
import co.makerflow.intellijplugin.actions.SetApiKeyNotificationAction
import co.makerflow.intellijplugin.actions.StopFlowModeNotificationAction
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.UIUtil
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Service
class HeartbeatService {

    private val activityTimestamps = arrayListOf<Long>()
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("Makerflow")
    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL
    private val httpClient = HttpClient()

    private fun promptForApiTokenNotification() = notificationGroup
        .createNotification(
            MyBundle.getMessage("makerflow-apikey.notification.body"),
            NotificationType.WARNING
        )
        .setTitle(MyBundle.getMessage("makerflow-apikey.notification.title"))
        .addAction(SetApiKeyNotificationAction())
        .addAction(DontAskForApiKeyAgainNotificationAction())

    fun heartbeat() {
        activityTimestamps.add(System.currentTimeMillis())
    }

    // A new Job to fetch the ongoing flow mode
    private val sendHeartbeatsCoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            if (activityTimestamps.size >= 2) {

                val apiToken = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken
                if (apiToken.isEmpty() && !SettingsState.instance.dontShowApiTokenPrompt) {
                    ProjectManager.getInstance().openProjects.forEach { promptForApiTokenNotification().notify(it) }
                } else if (apiToken.isNotEmpty()) {
                    UIUtil.invokeLaterIfNeeded {
                        sendHeartbeatsCoroutineScope.launch {
                            send(apiToken)
                        }
                    }
                }
            }
        }, INTERVAL, INTERVAL, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private suspend fun send(
        apiToken: String
    ) {
        val list = activityTimestamps.toMutableList()
        activityTimestamps.clear()
        list.sort()
        val api = ProductiveActivityApi(baseUrl, httpClient.engine, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return coroutineScope {
            @Suppress("TooGenericExceptionCaught")
            launch {
                try {
                    val logProductiveActivity =
                        api.logProductiveActivity(list, "jetbrains")
                    if (logProductiveActivity.success) {
                        activityTimestamps.clear()
                    } else {
                        activityTimestamps.addAll(list)
                    }
                } catch (e: Exception) {
                    when(e) {
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
