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
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.DefaultListModel
import javax.swing.Icon
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.CompoundBorder

private const val DELAY_TO_RELOAD_TASKS = 30L

private const val CONTENT_TOP_OFFSET = 10

class TasksPanel : SimpleToolWindowPanel(true) {

    private val fetchTasksCoroutineScope = CoroutineScope(Dispatchers.IO)

    private val listModel: DefaultListModel<TypedTodo> = DefaultListModel()
    private val list: JBList<TypedTodo> = JBList(listModel).apply {
        cellRenderer = TaskListCellRenderer()
        border = JBUI.Borders.empty()
    }

    private class ReloadAction(val tasksPanel: TasksPanel) :
        AnAction("Reload", "Reload tasks", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            tasksPanel.loadTasks()
        }
    }

    private val reloadAction = ReloadAction(this)
    private val reloadButton = ActionButton(
        reloadAction,
        reloadAction.templatePresentation,
        ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
        ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
    )

    init {

        val content = JBScrollPane(list).apply {
            border = JBUI.Borders.emptyTop(CONTENT_TOP_OFFSET)
        }
        super.setContent(content)

        // Add a reload button to a toolbar for the tool window
        val actionToolBar = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT))
        actionToolBar.add(reloadButton)
        super.setToolbar(actionToolBar)

        ApplicationManager.getApplication().invokeLater {
            loadTasks()
        }

        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            // Perform reload action
            loadTasks()
        }, DELAY_TO_RELOAD_TASKS, DELAY_TO_RELOAD_TASKS, java.util.concurrent.TimeUnit.SECONDS)
    }

    private fun loadTasks() {
        fetchTasksCoroutineScope.launch {
            list.setPaintBusy(true)
            listModel.clear()
            val tasks = service<TasksService>().fetchTasks()
            tasks.forEach {
                listModel.addElement(it)
            }
        }.invokeOnCompletion {
            list.setPaintBusy(false)
            sortTasks()
        }
    }

    private fun sortTasks() {
        val tasks = listModel.elements().toList().sortedBy { it.createdAt }
        listModel.clear()
        tasks.forEach { listModel.addElement(it) }
    }
}

private const val LIST_ITEM_PADDING_MARGIN = 5
private const val ROUNDED_CORNER_RADIUS = 5

class TaskListCellRenderer : ListCellRenderer<TypedTodo> {
    override fun getListCellRendererComponent(
        list: JList<out TypedTodo>?,
        value: TypedTodo?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val taskTitle = getTaskTitle(value)
        val taskSubtitle = getSubtitle(value!!)
        val link = if ((value is PullRequestTodo && value.pr?.link != null)) {
            value.pr.link
        } else {
            null
        }
        return panel {
            row {
                checkBox("").apply {
                    component.isSelected = value.done ?: false
                    component.addActionListener {
                        TODO()
                    }
                }
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
                    }
                }
            }

        }.apply {
            background = if (isSelected) list?.selectionBackground else list?.background
            border = CompoundBorder(
                JBUI.Borders.empty(LIST_ITEM_PADDING_MARGIN), // Outer border for padding around each item
                CompoundBorder(
                    // Inner border for the gray line around each item
                    RoundedLineBorder(JBColor.GRAY, ROUNDED_CORNER_RADIUS),
                    // Innermost border for left and right margins
                    JBUI.Borders.empty(0, LIST_ITEM_PADDING_MARGIN)
                )
            )
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
            " | " + PrettyTime().format(
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
        return "$sourceDescription$createdAt"
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
