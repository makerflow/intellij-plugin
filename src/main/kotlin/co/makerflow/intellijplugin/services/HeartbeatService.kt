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
import java.util.concurrent.atomic.AtomicBoolean

@Service
class HeartbeatService {

    private val activityTimestamps = sortedSetOf<Long>()
    private var lastSendTimestamp = 0L
    private val activityTimestampsQueue = mutableSetOf<Long>() // see heartbeat() for more

    // Flags to prevent multiple concurrent requests or modifications to activityTimestamps
    private val sending = AtomicBoolean()
    private val pruningTimestamps = AtomicBoolean()

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
        val now = System.currentTimeMillis()
        if (sending.get() || pruningTimestamps.get()) {
            // If we're currently sending timestamps to the backend, then let's avoid modifying
            // activityTimestamps to prevent concurrent modification exceptions
            // Instead we maintain a shadow set of timestamps that we'll merge into activityTimestamps
            // once the request is complete
            // I couldn't a find a way to use a thread-safe set that would allow us to sort and clear it in different
            // threads, so we're using a regular mutable set with these workarounds to emulate a thread-safe set
            activityTimestampsQueue.add(now)
            return
        }
        if (activityTimestampsQueue.isNotEmpty()) {
            activityTimestamps.addAll(activityTimestampsQueue)
            activityTimestampsQueue.clear()
        }
        activityTimestamps.add(now)
    }

    init {
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            if (sending.get() || activityTimestamps.size < 2) {
                return@scheduleWithFixedDelay
            }

            pruneTimestamps()

            // Send timestamps to backend
            val apiToken = getApiToken()
            if (apiToken.isEmpty() && !SettingsState.instance.dontShowApiTokenPrompt) {
                if (apiKeyNotificationShown == 0) {
                    ProjectManager.getInstance().openProjects.forEach { promptForApiTokenNotification().notify(it) }
                    apiKeyNotificationShown++
                }
            } else if (apiToken.isNotEmpty()) {
                UIUtil.invokeLaterIfNeeded {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!sending.get()) {
                            send()
                        }
                    }
                }
            }
        }, INTERVAL, INTERVAL, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private fun pruneTimestamps() {
        // Clear timestamps older than lastSendTimestamp
        pruningTimestamps.set(true)
        activityTimestamps.removeIf { it < lastSendTimestamp }
        pruningTimestamps.set(false)
    }

    private fun getApiToken(): String {
        return service<ApiTokenProvider>().getApiToken()
    }

    private suspend fun send() {
        sending.set(true)
        val api = service<ApiClientProvider>().provide(ProductiveActivityApi::class.java) ?: return
        val sortedTimestamps = activityTimestamps.toList()
        return coroutineScope {
            @Suppress("TooGenericExceptionCaught")
            launch {
                try {
                    val req = api.logProductiveActivity(sortedTimestamps, "jetbrains")
                    if (req.success) {
                        lastSendTimestamp = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
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

                        else -> {
                            sending.set(false)
                            throw e
                        }
                    }
                } finally {
                    sending.set(false)
                }
            }
        }.join()
    }

    companion object {
        private const val INTERVAL = 30000L
    }
}
