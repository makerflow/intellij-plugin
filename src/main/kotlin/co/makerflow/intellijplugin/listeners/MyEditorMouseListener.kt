package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener

internal class MyEditorMouseListener : EditorMouseListener {

    override fun mouseClicked(event: EditorMouseEvent) {
        heartbeat()
    }

    override fun mousePressed(event: EditorMouseEvent) {
        heartbeat()
    }

    override fun mouseReleased(event: EditorMouseEvent) {
        heartbeat()
    }

    private fun heartbeat() {
        val heartbeatService = ServiceManager.getService(HeartbeatService::class.java)
        heartbeatService.heartbeat()
    }
}