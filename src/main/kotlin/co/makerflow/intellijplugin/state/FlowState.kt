package co.makerflow.intellijplugin.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "co.makerflow.intellijplugin.state.FlowState", storages = [Storage("MakerflowFlowState.xml")])
class FlowState : PersistentStateComponent<FlowState> {
    var processing: Boolean = false
        set(value) {
            field = value
            ApplicationManager.getApplication().messageBus
                .syncPublisher(FlowStateChangeNotifier.FLOW_STATE_CHANGE_TOPIC).updated(currentFlow, value)
        }
    var currentFlow: Flow? = null
        set(value) {
            field = value
            ApplicationManager.getApplication().messageBus
                .syncPublisher(FlowStateChangeNotifier.FLOW_STATE_CHANGE_TOPIC).updated(value, processing)
        }

    fun isStarting(): Boolean {
        return currentFlow != null && !processing
    }

    fun isStopping(): Boolean {
        return currentFlow == null && processing
    }

    fun isInFlow(): Boolean {
        return currentFlow != null && !processing
    }

    override fun getState(): FlowState {
        return instance
    }

    override fun loadState(state: FlowState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun isInFlow(): Boolean  = instance.isInFlow()
        fun isStarting(): Boolean  = instance.isStarting()
        fun isStopping(): Boolean  = instance.isStopping()

        private val state = FlowState()
        @JvmStatic
        val instance: FlowState
            get() = state
    }
}
