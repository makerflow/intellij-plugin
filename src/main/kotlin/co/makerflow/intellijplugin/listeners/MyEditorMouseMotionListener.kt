package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.event.*


internal class MyEditorMouseMotionListener: EditorMouseMotionListener {

    override fun mouseMoved(e: EditorMouseEvent) {
        super.mouseMoved(e)
        val heartbeatService = ServiceManager.getService(HeartbeatService::class.java)
        heartbeatService.heartbeat()
    }
}
