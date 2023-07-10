package co.makerflow.intellijplugin.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "co.makerflow.intellijplugin.state.WorkBreakState", storages = [Storage("MakerflowWorkBreakState.xml")])
class WorkBreakState : PersistentStateComponent<WorkBreakState> {

    var processing: Boolean = false
        set(value) {
            field = value
            ApplicationManager.getApplication().messageBus
                .syncPublisher(WorkBreakStateChangeNotifier.WORK_BREAK_STATE_CHANGE_TOPIC).updated(currentBreak, value)
        }
    var currentBreak: Break? = null
        set(value) {
            field = value
            ApplicationManager.getApplication().messageBus
                .syncPublisher(WorkBreakStateChangeNotifier.WORK_BREAK_STATE_CHANGE_TOPIC).updated(value, processing)
        }

    fun isStarting(): Boolean {
        return currentBreak == null && processing
    }

    fun isStopping(): Boolean {
        return currentBreak != null && processing
    }

    fun isOngoing(): Boolean {
        return currentBreak != null
    }

    override fun getState(): WorkBreakState {
        return instance
    }

    override fun loadState(state: WorkBreakState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {

        fun isStarting(): Boolean = instance.isStarting()
        fun isStopping(): Boolean = instance.isStopping()
        fun isOngoing(): Boolean = instance.isOngoing()

        private val state = WorkBreakState()

        @JvmStatic
        val instance: WorkBreakState
            get() = state
    }
}
