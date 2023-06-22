package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener

internal class MyEditorMouseListener : EditorMouseListener {

    private val heartbeatService = service<HeartbeatService>()

    override fun mouseClicked(event: EditorMouseEvent) {
        heartbeatService.heartbeat()
    }

}
