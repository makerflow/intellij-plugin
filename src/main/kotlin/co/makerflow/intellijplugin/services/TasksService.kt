package co.makerflow.intellijplugin.services

import co.makerflow.client.apis.TasksApi
import co.makerflow.client.infrastructure.ApiClient
import co.makerflow.client.models.CustomTask
import co.makerflow.client.models.CustomTaskTodo
import co.makerflow.client.models.MarkDoneRequest
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.settings.SettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.messages.Topic
import com.squareup.moshi.JsonEncodingException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val SOURCE_TYPE = "jetbrains"

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
                    val res = tasksApi().getTodos(SOURCE_TYPE)
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
                val response = tasksApi().markDone(SOURCE_TYPE, MarkDoneRequest(todo = task, done))
                success = response.success
            }.join()
            return@coroutineScope success
        }
    }

    suspend fun addTask(task: String): CustomTaskTodo? {
        return coroutineScope {
            var ret: CustomTaskTodo? = null
            launch {
                val data = CustomTask(title = task, done = false)
                val response = tasksApi().addCustomTask(SOURCE_TYPE, data)
                if (response.success) {
                    val customTask = response.body().data
                    ret = customTask?.let {
                        CustomTaskTodo(task = it)
                    }?.apply {
                        done = false
                        sourceType = TypedTodo.SourceType.makerflow
                        createdAt = customTask.createdAt
                        type = "makerflow"
                    }
                }
            }.join()
            return@coroutineScope ret
        }
    }

    companion object {
        val TASKS_ADDED_TOPIC: Topic<TaskAdded> = Topic.create("Tasks", TaskAdded::class.java)
    }

}

fun interface TaskAdded {
    fun taskAdded(task: CustomTaskTodo)
}
