package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class MyDocumentListener : DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        val heartbeatService = ServiceManager.getService(HeartbeatService::class.java)
        heartbeatService.heartbeat()
    }
}
