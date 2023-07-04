package co.makerflow.intellijplugin.panels

import co.makerflow.client.models.CustomTaskTodo
import co.makerflow.client.models.OnboardingTask
import co.makerflow.client.models.OnboardingTask.Step
import co.makerflow.client.models.PullRequestTodo
import co.makerflow.client.models.TypedTodo
import co.makerflow.intellijplugin.services.FlowModeService
import co.makerflow.intellijplugin.services.TasksService
import co.makerflow.intellijplugin.services.TodoUtil
import co.makerflow.intellijplugin.services.toFlow
import co.makerflow.intellijplugin.state.FlowState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.Gaps
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.ocpsoft.prettytime.PrettyTime
import java.awt.event.ItemEvent
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.border.CompoundBorder

private const val DELAY_TO_RELOAD_TASKS = 30L
private const val INITIAL_DELAY_TO_RELOAD_TASKS = 60L
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
                .align(Align.CENTER)
        }
        row {
            label("Please wait, loading tasks...")
                .align(Align.CENTER)
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
            reload()
        }

        fun reload() {
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
        }, INITIAL_DELAY_TO_RELOAD_TASKS, DELAY_TO_RELOAD_TASKS, java.util.concurrent.TimeUnit.SECONDS)

    }

    private fun loadTasks() {
        val service = service<TasksService>()
        fetchTasksCoroutineScope.launch {
            checkboxes.clear()
            val tasks = service.fetchTasks().sortedBy { it.createdAt }
            if (tasks.isEmpty()) {
                ApplicationManager.getApplication().invokeLater {
                    val noTasksMessage = panel {
                        row {
                            label("No new tasks, you are a free bird!")
                                .align(Align.CENTER)
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
                val taskPresentationComponent = TaskPresentationComponent(reloadAction, checkboxes, service)
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

private const val FLOW_MODE_DROPDOWN_WITHOUT_TIMER = "Flow Mode without timer"
private const val FLOW_MODE_DROPDOWN_25_MINUTES = "Flow Mode for 25 minutes"
private const val FLOW_MODE_DROPDOWN_50_MINUTES = "Flow Mode for 50 minutes"
private const val FLOW_MODE_DROPDOWN_75_MINUTES = "Flow Mode for 75 minutes"

class TaskPresentationComponent(
    private val reloadAction: TasksPanel.ReloadAction,
    private var checkboxes: ArrayList<JCheckBox>,
    private val service: TasksService,
    private val todoUtil: TodoUtil = service<TodoUtil>()
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

    private val beginFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val endFlowModeCoroutineScope = CoroutineScope(Dispatchers.IO)

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
        todoUtil.isTodoInFlow(value)
        val panel = panel {
            row {
                cell(checkbox)

                panel {
                    row {
                        if (value !is OnboardingTask) {
                                val isTodoInFlow = todoUtil.isTodoInFlow(value)
                                val showDropdown = AtomicBooleanProperty(!isTodoInFlow)
                                val showStopButton = AtomicBooleanProperty(isTodoInFlow)
                                val stoppingFlowMode = AtomicBooleanProperty(false)
                                link("Stop") {
                                    stoppingFlowMode.set(true)
                                    showStopButton.set(false)
                                    endFlowModeCoroutineScope.launch {
                                        try {
                                            service<FlowModeService>().stopFlowMode()
                                            FlowState.instance.currentFlow = null
                                            stoppingFlowMode.set(false)
                                            showStopButton.set(false)
                                            showDropdown.set(true)
                                            reloadAction.reload()
                                        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                                            thisLogger().error("Error stopping flow mode", e)
                                            stoppingFlowMode.set(false)
                                            showStopButton.set(true)
                                            throw e
                                        }
                                    }
                                }
                                    .applyToComponent {
                                        this.toolTipText = "Stop flow mode"
                                }.apply {
                                    visible(showStopButton.get())
                                    showStopButton.afterChange {
                                        visible(it)
                                    }
                                    }
                                label("Stopping...")
                                .align(AlignX.LEFT)
                                    .customize(Gaps())
                                .apply {
                                    visible(stoppingFlowMode.get())
                                    stoppingFlowMode.afterChange {
                                        visible(it)
                                    }
                                }
                                startFlowModeDropdown(value, showDropdown)
                        }
                        icon(getIconForType(value.type!!)).align(AlignX.CENTER)
                        if (link != null) {
                            browserLink(taskTitle!!, link)
                        } else {
                            label(taskTitle!!)
                        }
                    }
                }
            }.bottomGap(BottomGap.NONE).layout(RowLayout.PARENT_GRID)
            separator()
            row {
                cell()
                comment(taskSubtitle)
            }.bottomGap(BottomGap.NONE)
        }

        panel.border = border
        panel.apply()
        return panel
    }

    private fun Row.startFlowModeDropdown(value: TypedTodo?, showDropdown: AtomicBooleanProperty) {
        val startingFlowMode = AtomicBooleanProperty(false)
        dropDownLink(
            "Start",
            listOf(
                FLOW_MODE_DROPDOWN_WITHOUT_TIMER,
                FLOW_MODE_DROPDOWN_25_MINUTES,
                FLOW_MODE_DROPDOWN_50_MINUTES,
                FLOW_MODE_DROPDOWN_75_MINUTES,
            )
        ).onChanged {
                val flowModeService = service<FlowModeService>()
                beginFlowModeCoroutineScope.launch {
                    showDropdown.set(false)
                    startingFlowMode.set(true)
                    try {
                        val flowMode = when (it.selectedItem) {
                            FLOW_MODE_DROPDOWN_WITHOUT_TIMER -> {
                                flowModeService.startFlowMode(value, duration = null)
                            }

                            FLOW_MODE_DROPDOWN_25_MINUTES -> {
                                flowModeService.startFlowMode(value, duration = 25)
                            }

                            FLOW_MODE_DROPDOWN_50_MINUTES -> {
                                flowModeService.startFlowMode(value, duration = 50)
                            }

                            FLOW_MODE_DROPDOWN_75_MINUTES -> {
                                flowModeService.startFlowMode(value, duration = 75)
                            }

                            else -> null
                        }
                        if (flowMode != null) {
                            FlowState.instance.currentFlow = flowMode.toFlow(value)
                        }
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        thisLogger().error("Error starting flow mode", e)
                        showDropdown.set(true)
                        throw e
                    }
                    reloadAction.reload()
                }
            }
            .apply {
                visible(showDropdown.get())
                showDropdown.afterChange {
                    visible(it)
                }
            }
            .applyToComponent {
                this.toolTipText = "Start a flow mode session for this task"
            }
        label("Starting...")
            .align(AlignX.LEFT)
            .customize(Gaps())
            .apply {
                visible(startingFlowMode.get())
                startingFlowMode.afterChange {
                    visible(it)
                }
            }
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
        if (createdAt.isEmpty()) {
            return sourceDescription
        }
        return "$sourceDescription | $createdAt"
    }

}
