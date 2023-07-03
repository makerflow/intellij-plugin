package co.makerflow.intellijplugin.services

import co.makerflow.client.models.CustomTaskTodo
import co.makerflow.client.models.PullRequestTodo
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.state.FlowState
import com.intellij.openapi.components.Service

@Service
class TodoUtil {
    fun determineTodoId(todo: TypedTodo): String {
        return when (todo.sourceType) {
            TypedTodo.SourceType.makerflow -> {
                if (todo is CustomTaskTodo) todo.task.id.toString()
                else throw UnsupportedOperationException("Unknown makerflow todo type")
            }

            TypedTodo.SourceType.github -> (todo as PullRequestTodo).pr?.id.toString()
            TypedTodo.SourceType.bitbucket -> (todo as PullRequestTodo).pr?.id.toString()
            TypedTodo.SourceType.slack -> (todo as PullRequestTodo).pr?.id.toString()
            else -> {
                throw UnsupportedOperationException("Unknown todo type")
            }
        }
    }

    fun isTodoInFlow(todo: TypedTodo): Boolean {
        return FlowState.instance.currentFlow?.todo?.let {
            todo.sourceType == it.sourceType && todo.type == it.type && determineTodoId(todo) == determineTodoId(it)
        } ?: false
    }
}
