package co.makerflow.intellijplugin.panels

import co.makerflow.client.models.CustomTaskTodo
import co.makerflow.client.models.OnboardingTask
import co.makerflow.client.models.OnboardingTask.Step
import co.makerflow.client.models.PullRequestTodo
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.services.TasksService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.ocpsoft.prettytime.PrettyTime
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.border.CompoundBorder

private const val DELAY_TO_RELOAD_TASKS = 30L
private const val CONTENT_TOP_OFFSET = 10
private const val LIST_ITEM_PADDING_MARGIN = 5
private const val ROUNDED_CORNER_RADIUS = 5


class TasksPanel : SimpleToolWindowPanel(true) {

    private val fetchTasksCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val checkboxes = arrayListOf<JCheckBox>()
    private val loadingIconPanel = panel {
        row {
            cell(
                AsyncProcessIcon.BigCentered("Loading tasks...")
            )
                .horizontalAlign(HorizontalAlign.CENTER)
                .verticalAlign(VerticalAlign.CENTER)
        }
        row {
            label("Please wait, loading tasks...")
                .horizontalAlign(HorizontalAlign.CENTER)
                .verticalAlign(VerticalAlign.CENTER)
        }
    }.apply {
        border = JBUI.Borders.empty()
        alignmentX = JComponent.CENTER_ALIGNMENT
    }
    private var container = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(
            loadingIconPanel
        )
    }
    private val loadingMessageContent = JBScrollPane(container).apply {
        border = JBUI.Borders.emptyTop(CONTENT_TOP_OFFSET)
    }

    class ReloadAction(private val tasksPanel: TasksPanel) :
        AnAction("Reload", "Reload tasks", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            tasksPanel.loadTasks()
        }
    }

    val reloadAction = ReloadAction(this)

    init {


        super.setContent(loadingMessageContent)

        ApplicationManager.getApplication().invokeLater {
            loadTasks()
        }

        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            ApplicationManager.getApplication().invokeLater {
                loadTasks()
            }
        }, DELAY_TO_RELOAD_TASKS + 30, DELAY_TO_RELOAD_TASKS, java.util.concurrent.TimeUnit.SECONDS)

    }

    private fun loadTasks() {
        val service = service<TasksService>()
        fetchTasksCoroutineScope.launch {
            UIUtil.invokeLaterIfNeeded {
                super.setContent(loadingMessageContent)
            }
            checkboxes.clear()
            val tasks = service.fetchTasks().sortedBy { it.createdAt }
            if (tasks.isEmpty()) {
                ApplicationManager.getApplication().invokeLater {
                    val noTasksMessage = panel {
                        row {
                            label("No new tasks, you are a free bird!")
                                .horizontalAlign(HorizontalAlign.CENTER)
                                .verticalAlign(VerticalAlign.CENTER)
                        }
                    }.apply {
                        border = JBUI.Borders.empty()
                        alignmentX = JComponent.CENTER_ALIGNMENT
                    }
                    super.setContent(JBScrollPane(noTasksMessage).apply {
                        border = JBUI.Borders.emptyTop(CONTENT_TOP_OFFSET)
                    })
                }
                return@launch
            }
            // Init list with temp checkboxes to avoid IndexOutOfBoundException
            tasks.forEach { _ ->
                checkboxes.add(JCheckBox())
            }
            ApplicationManager.getApplication().invokeLater {
                val taskPresentationComponent = TaskPresentationComponent(checkboxes, service)
                val updatedContainer = JBPanel<JBPanel<*>>()
                // Set layout to BoxLayout to stack components vertically
                updatedContainer.layout = BoxLayout(updatedContainer, BoxLayout.Y_AXIS)
                tasks.forEachIndexed { index, task ->
                    updatedContainer.add(
                        taskPresentationComponent.getComponent(task, index)
                    )
                }
                super.setContent(JBScrollPane(updatedContainer).apply {
                    border = JBUI.Borders.empty()
                })
            }
        }
    }

}

fun String.pluralize(count: Int): String {
    val updated = if (count > 1) {
        this + 's'
    } else {
        this
    }
    return "$count $updated"
}

private class TaskDone(val task: TypedTodo, val service: TasksService) {
    private val markDoneCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val unmarkDoneCoroutineScope = CoroutineScope(Dispatchers.IO)
    var done: Boolean = task.done ?: false
        set(value) {
            ApplicationManager.getApplication().invokeLater {
                if (value) {
                    markDoneCoroutineScope.launch {
                            service.markTaskDone(task)
                    }
                } else {
                    unmarkDoneCoroutineScope.launch {
                            service.markTaskUndone(task)
                    }
                }
            }
            task.done = value
            field = value
        }
}

class TaskPresentationComponent(
    private var checkboxes: ArrayList<JCheckBox>,
    private val service: TasksService
) {

    private val border = CompoundBorder(
        JBUI.Borders.empty(LIST_ITEM_PADDING_MARGIN), // Outer border for padding around each item
        CompoundBorder(
            // Inner border for the gray line around each item
            RoundedLineBorder(JBColor.GRAY, ROUNDED_CORNER_RADIUS),
            // Innermost border for left and right margins
            JBUI.Borders.empty(0, LIST_ITEM_PADDING_MARGIN)
        )
    )

    fun getComponent(value: TypedTodo?, index: Int): JComponent {
        val taskTitle = getTaskTitle(value)
        val taskSubtitle = getSubtitle(value!!)
        val link = if ((value is PullRequestTodo && value.pr?.link != null)) {
            value.pr.link
        } else {
            null
        }
        val taskDone = TaskDone(value, service)
        val checkbox = JCheckBox("", taskDone.done).apply {
            this.isOpaque = true
            isEnabled = true
            isFocusable = true
            this.addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    taskDone.done = true
                } else if (it.stateChange == ItemEvent.DESELECTED) {
                    taskDone.done = false
                }
            }
        }
        checkboxes[index] = checkbox
        val panel = panel {
            row {
                cell(checkbox)

                panel {
                    row {
                        icon(getIconForType(value.type!!))
                        if (link != null) {
                            browserLink(taskTitle!!, link)
                        } else {
                            label(taskTitle!!)
                        }
                    }
                    separator()
                    row {
                        comment(taskSubtitle)
                    }.bottomGap(BottomGap.NONE)
                }
            }.bottomGap(BottomGap.NONE)

        }

        panel.border = border
        panel.apply()
        return panel
    }

    private fun getTaskTitle(value: TypedTodo?) = when (value) {
        is PullRequestTodo -> {
            "PR #${value.pr?.pullrequestId}: ${value.pr?.pullrequestTitle}"
        }

        is CustomTaskTodo -> {
            value.task.title
        }

        is OnboardingTask -> {
            when (value.step!!) {
                Step.chatMinusIntegration -> "Connect your Slack workspace"
                Step.repoMinusIntegration -> "Connect your code repository"
                Step.calendarMinusIntegration -> "Connect your Google account"
                Step.cliMinusDownload -> "Install Makerflow CLI"
                Step.editorMinusIntegration -> "Install VS Code plugin"
                Step.browserMinusExtension -> "Install browser extension"
            }
        }

        else -> ""
    }

    private fun getIconForType(type: String): Icon {
        return when (type) {
            "github" -> {
                AllIcons.Vcs.Vendors.Github
            }

            "bitbucket" -> {
                AllIcons.Vcs.Merge
            }

            "onboarding" -> {
                AllIcons.General.Information
            }

            "makerflow" -> {
                AllIcons.General.TodoDefault
            }

            else -> AllIcons.General.TodoDefault
        }
    }

    private fun getSubtitle(value: TypedTodo): String {
        val createdAt: String = if (value !is OnboardingTask) {
            PrettyTime().format(
                Instant.parse(value.createdAt!!).toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            )
        } else {
            ""
        }

        val sourceDescription = when (value) {
            is PullRequestTodo -> {
                "${value.pr?.repositoryName} | ${"comments".pluralize(value.meta?.comments!!)} | ${
                    "approvals".pluralize(
                        value.meta.approvals!!
                    )
                }"
            }

            is CustomTaskTodo -> {
                ""
            }

            is OnboardingTask -> {
                when (value.step!!) {
                    Step.chatMinusIntegration -> "Update your status automatically when you start or end flow mode " +
                            "or take a break while working."

                    Step.repoMinusIntegration -> "See pending pull requests and related information in your " +
                            "unified task list."

                    Step.calendarMinusIntegration -> "See upcoming events and join meetings quickly and easily."
                    Step.cliMinusDownload -> "Easily access useful Makerflow functionality from the command line."
                    Step.editorMinusIntegration -> "Easily access useful Makerflow functionality from " +
                            "your favorite editors."

                    Step.browserMinusExtension -> "Block distracting websites when you enter Flow Mode"
                }
            }

            else -> ""
        }
        if (sourceDescription.isEmpty()) {
            return createdAt
        }
        return "$sourceDescription$createdAt"
    }

}
