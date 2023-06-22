package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class MyDocumentListener : DocumentListener {

    private val heartbeatService = service<HeartbeatService>()

    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        heartbeatService.heartbeat()
    }
}
