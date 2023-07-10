package co.makerflow.intellijplugin.state

import com.intellij.util.messages.Topic

fun interface WorkBreakStateChangeNotifier {
    companion object {
        val WORK_BREAK_STATE_CHANGE_TOPIC: Topic<WorkBreakStateChangeNotifier> =
            Topic.create("Work Break state updated", WorkBreakStateChangeNotifier::class.java)
    }

    fun updated(workBreak: Break?, processing: Boolean)
}
