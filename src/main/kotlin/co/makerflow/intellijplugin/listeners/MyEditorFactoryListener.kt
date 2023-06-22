package co.makerflow.intellijplugin.listeners;

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener


internal class MyEditorFactoryListener : EditorFactoryListener {

    private val heartbeatService = service<HeartbeatService>()

    override fun editorCreated(event: EditorFactoryEvent) {
        super.editorCreated(event)
        heartbeatService.heartbeat()
    }
}
