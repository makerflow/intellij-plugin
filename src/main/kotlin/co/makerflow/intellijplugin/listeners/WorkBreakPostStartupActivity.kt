package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.WorkBreakService
import co.makerflow.intellijplugin.state.WorkBreakState
import co.makerflow.intellijplugin.state.toBreak
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DELAY_BETWEEN_FETCHES = 10L

class WorkBreakPostStartupActivity : ProjectActivity {

    // A new Job to fetch the ongoing work break
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun execute(project: Project) {

        // Fetch ongoing flow mode at regular intervals
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            thisLogger().info("Fetching ongoing flow mode")
            ApplicationManager.getApplication().invokeLater {
                coroutineScope.launch {
                    service<WorkBreakService>().getOngoingWorkBreak()?.let { workBreak ->
                        WorkBreakState.instance.currentBreak = workBreak.toBreak()
                    }
                }
            }
        }, 0, DELAY_BETWEEN_FETCHES, java.util.concurrent.TimeUnit.SECONDS)
    }

}
