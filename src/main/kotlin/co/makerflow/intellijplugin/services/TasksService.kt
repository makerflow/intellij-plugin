package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.TasksApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.MarkDoneRequest
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Service
class TasksService {

    private val baseUrl = System.getenv("MAKERFLOW_API_URL") ?: ApiClient.BASE_URL

    private fun tasksApi(): TasksApi {
        val apiToken = getApiToken()
        val api = TasksApi(baseUrl, null, null, ApiClient.JSON_DEFAULT)
        api.setApiKey(apiToken)
        return api
    }

    private fun getApiToken() = System.getenv("MAKERFLOW_API_TOKEN") ?: SettingsState.instance.apiToken

    suspend fun fetchTasks(): List<TypedTodo> {
        return coroutineScope {
            var tasks: List<TypedTodo> = listOf()
            launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    val res = tasksApi().getTodos("jetbrains")
                    if (res.success) {
                        tasks = res.body()
                    }
                } catch (e: Exception) {
                    when (e) {
                        is JsonConvertException,
                        is NoTransformationFoundException,
                        is JsonEncodingException -> {
                            /*
                           Intentionally left blank
                           */
                            thisLogger().error(e)
                            thisLogger().error("Error serializing tasks : ${e.message}")
                        }

                        else -> {
                            thisLogger().error(e)
                            thisLogger().error("Error serializing tasks : ${e.message}")
                            throw e
                        }
                    }
                }
            }.join()
            return@coroutineScope tasks
        }
    }

    suspend fun markTaskDone(task: TypedTodo): Boolean {
        return updateTaskStatus(task, true)
    }

    suspend fun markTaskUndone(task: TypedTodo): Boolean {
        return updateTaskStatus(task, false)
    }

    private suspend fun updateTaskStatus(task: TypedTodo, done: Boolean): Boolean {
        return coroutineScope {
            var success = false
            launch {
                val response = tasksApi().markDone("jetbrains", MarkDoneRequest(todo = task, done))
                success = response.success
            }.join()
            return@coroutineScope success
        }
    }

}
