package co.makerflow.intellijplugin.state

import com.intellij.util.messages.Topic

interface FlowStateChangeNotifier {
    companion object {
        val FLOW_STATE_CHANGE_TOPIC: Topic<FlowStateChangeNotifier> =
            Topic.create("Flow state updated", FlowStateChangeNotifier::class.java)
    }

    fun updated(flow: Flow?, processing: Boolean)
}
