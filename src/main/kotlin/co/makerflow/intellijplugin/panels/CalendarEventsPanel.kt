package co.makerflow.intellijplugin.panels

import co.makerflow.client.models.CalendarEvent
import co.makerflow.intellijplugin.providers.ApiTokenProvider
import co.makerflow.intellijplugin.services.EventsService
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Panel
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
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.CompoundBorder


private const val CONTENT_TOP_OFFSET = 10
private const val DELAY_TO_RELOAD_EVENTS = 300L
private const val INITIAL_DELAY_TO_RELOAD_EVENTS = 60L

class UpcomingEventsPanel : SimpleToolWindowPanel(true) {

    private val fetchEventsCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val loadingIconPanel = panel {
        row {
            cell(
                AsyncProcessIcon.BigCentered("Loading events from calendar...")
            )
                .align(Align.CENTER)
        }
        row {
            label("Please wait, loading events from calendar...")
                .align(Align.CENTER)
        }
    }.apply {
        border = JBUI.Borders.empty()
        alignmentX = CENTER_ALIGNMENT
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

    class ReloadAction(private val panel: UpcomingEventsPanel) :
        AnAction("Reload", "Reload upcoming events", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            panel.loadEvents()
        }

    }

    val reloadAction = ReloadAction(this)

    init {

        super.setContent(loadingMessageContent)

        ApplicationManager.getApplication().invokeLater {
            loadEvents()
        }

        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            ApplicationManager.getApplication().invokeLater {
                loadEvents()
            }
        }, INITIAL_DELAY_TO_RELOAD_EVENTS, DELAY_TO_RELOAD_EVENTS, TimeUnit.SECONDS)


    }

    private fun loadEvents() {
        val apiTokenPresent = service<ApiTokenProvider>().getApiToken().isNotEmpty()
        if (apiTokenPresent.not()) {
            showMissingApiTokenMessage()
            return
        }
        val service = service<EventsService>()
        fetchEventsCoroutineScope.launch {
            val events = service.fetchEvents()
            if (events.isEmpty()) {
                ApplicationManager.getApplication().invokeLater {
                    val noEventsMessage = panel {
                        row {
                            label("No upcoming events, go get some sun!")
                                .align(Align.CENTER)
                        }
                    }.apply {
                        border = JBUI.Borders.empty()
                        alignmentX = CENTER_ALIGNMENT
                    }
                    super.setContent(JBScrollPane(noEventsMessage).apply {
                        border = JBUI.Borders.emptyTop(CONTENT_TOP_OFFSET)
                    })
                }
                return@launch
            }
            ApplicationManager.getApplication().invokeLater {
                val eventPresentationComponent = EventPresentationComponent()
                val updatedContainer = JBPanel<JBPanel<*>>()
                // Set layout to BoxLayout to stack components vertically
                updatedContainer.layout = BoxLayout(updatedContainer, BoxLayout.Y_AXIS)
                events.forEach {
                    updatedContainer.add(
                        eventPresentationComponent.getComponent(it)
                    )
                }
                super.setContent(JBScrollPane(updatedContainer).apply {
                    border = JBUI.Borders.empty()
                })
            }
        }
    }

    private fun showMissingApiTokenMessage() {
        ApplicationManager.getApplication().invokeLater {
            val noTokenMessage = panel {
                row {
                    label("An API key for Makerflow is required to fetch events from your calendar.")
                        .align(Align.CENTER)
                }
                row {
                    button(
                        "Set API Key",
                        ActionManager.getInstance().getAction("co.makerflow.intellijplugin.actions.SetApiKeyAction")
                    )
                        .align(Align.CENTER)
                }
                row {
                    comment("Click the button above to set your API key and for more information on how to get one.")
                        .align(Align.CENTER)
                }
            }.apply {
                border = JBUI.Borders.empty()
                alignmentX = CENTER_ALIGNMENT
            }
            super.setContent(JBScrollPane(noTokenMessage).apply {
                border = JBUI.Borders.emptyTop(CONTENT_TOP_OFFSET)
            })
        }
    }

}


private const val LIST_ITEM_PADDING_MARGIN = 5
private const val ROUNDED_CORNER_RADIUS = 5

class EventPresentationComponent {

    fun getComponent(event: CalendarEvent): JComponent {
        val eventPanel = EventPanel()
        eventPanel.layout = BoxLayout(eventPanel, BoxLayout.Y_AXIS)
        eventPanel.add(buildPanel(event))
        return eventPanel
    }

    inner class EventPanel : JPanel() {
        override fun getMaximumSize(): Dimension {
            // Makes it so the height matches the content and width is the maximum available in parent
            return Dimension(Int.MAX_VALUE, this.preferredSize.height)
        }
    }

    private fun buildPanel(event: CalendarEvent): DialogPanel {
        val videoUri = event.conference?.entryPoints?.firstOrNull { it.entryPointType == "video" }?.uri
        val panel = panel {
            titleRow(event)
            separator()
            subTitleRow(event)
            separator().visible(videoUri.isNullOrBlank().not())
            videoRow(event, videoUri)
        }
        panel.border = CompoundBorder(
            JBUI.Borders.empty(LIST_ITEM_PADDING_MARGIN), // Outer border for padding around each item
            CompoundBorder(
                // Inner border for the gray line around each item
                RoundedLineBorder(JBColor.GRAY, ROUNDED_CORNER_RADIUS),
                // Innermost border for left and right margins
                JBUI.Borders.empty(0, LIST_ITEM_PADDING_MARGIN)
            )
        )
        panel.apply()
        return panel
    }

    @Suppress("unused", "kotlin:S125")
    private fun Panel.videoRow(event: CalendarEvent, videoUri: String?) {

        row {
            videoUri?.let {
                button("Join Now") {
                    BrowserUtil.browse(videoUri)
                }.applyToComponent {
                    icon = AllIcons.Ide.External_link_arrow
                }
                // joinOnStartButton(event, videoUri, p, start, now)
                // Join on start button to be implemented as part of MAK-60
            }
        }
            .bottomGap(BottomGap.NONE).layout(RowLayout.PARENT_GRID)
            .visible(videoUri.isNullOrBlank().not())
    }

    @Suppress("unused", "kotlin:S1144")
    private fun Row.joinOnStartButton(
        event: CalendarEvent,
        videoUri: String
    ) {
        val start = Instant.parse(event.start).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val p = PrettyTime()
        val now = LocalDateTime.now()
        button("Join On Start") {
            val sourceButton = it.source as JButton
            if (sourceButton.isEnabled) {
                // Difference between now and start in milliseconds
                val delay = Instant.now().until(Instant.parse(event.start), ChronoUnit.MILLIS)
                AppExecutorUtil.getAppScheduledExecutorService().schedule({
                    BrowserUtil.browse(videoUri)
                }, delay, TimeUnit.MILLISECONDS)
                AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
                    sourceButton.text = "Joining in ${p.format(start)}"
                }, 0, 1, TimeUnit.SECONDS);
                sourceButton.isEnabled = false
            }
        }.applyToComponent {
            icon = AllIcons.Actions.BuildAutoReloadChanges
        }.visible(start.isAfter(now))
    }

    private fun Panel.subTitleRow(event: CalendarEvent) {
        val now = LocalDateTime.now()
        val start = Instant.parse(event.start).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val end = Instant.parse(event.end).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val p = PrettyTime()
        val pattern = DateTimeFormatter.ofPattern("HH:mm a")
        val startInfo = "Start${if (start.isAfter(now)) "s" else "ed"} at ${start.format(pattern)} (${p.format(start)})"
        val endInfo = "End${if (end.isAfter(now)) "s" else "ed"} at ${end.format(pattern)} (${p.format(end)})"
        val timingInfo = "$startInfo | $endInfo"
        row {
            comment(timingInfo).customize(Gaps.EMPTY)
        }.bottomGap(BottomGap.NONE)
    }

    /**
     * Provides a label that tells the user whether the event is from the past, ongoing or upcoming
     */
    private fun determineRelativeLabel(event: CalendarEvent): String {
        val now = LocalDateTime.now()
        val start = Instant.parse(event.start).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val end = Instant.parse(event.end).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return when {
            start.isBefore(now) && end.isAfter(now) -> "Ongoing"
            start.isAfter(now) -> "Upcoming"
            else -> "Past"
        }
    }

    private fun Panel.titleRow(event: CalendarEvent) {
        val relativeLabel = determineRelativeLabel(event)
        row {
            label(relativeLabel).applyToComponent {
                border = CompoundBorder(
                    JBUI.Borders.empty(), // Outer border for padding around each item
                    CompoundBorder(
                        // Inner border for the gray line around each item
                        RoundedLineBorder(JBColor.GRAY, ROUNDED_CORNER_RADIUS),
                        // Innermost border for left and right margins
                        JBUI.Borders.empty(0, LIST_ITEM_PADDING_MARGIN)
                    )
                )
                background = when (relativeLabel) {
                    "Past" -> {
                        JBColor.namedColor("Panel.background")
                    }

                    "Ongoing" -> {
                        JBColor.namedColor("Banner.warningBackground")
                    }

                    else -> {
                        JBColor.namedColor("Banner.infoBackground")
                    }
                }
                isOpaque = true
            }
            event.summary?.let {
                label(it.trim()).applyToComponent {
                    event.htmlLink?.let {
                        // Make the label clickable
                        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseClicked(e: MouseEvent?) {
                                BrowserUtil.browse(event.htmlLink)
                            }
                        })
                        toolTipText = "Click to open in calendar"
                    }
                }
                    .customize(Gaps.EMPTY)
                    .applyToComponent {
                        border = JBUI.Borders.empty()
                    }
                    .align(AlignX.LEFT)
            }
        }.bottomGap(BottomGap.NONE)
    }

}
