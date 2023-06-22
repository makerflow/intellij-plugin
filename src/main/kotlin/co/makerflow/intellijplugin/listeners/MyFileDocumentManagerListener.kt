package co.makerflow.intellijplugin.listeners

import co.makerflow.intellijplugin.services.HeartbeatService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.vfs.VirtualFile

class MyFileDocumentManagerListener: com.intellij.openapi.fileEditor.FileDocumentManagerListener {

    private val heartbeatService = service<HeartbeatService>()

    override fun fileContentLoaded(file: VirtualFile, document: Document) {
        super.fileContentLoaded(file, document)
        heartbeatService.heartbeat()
    }

    override fun fileContentReloaded(file: VirtualFile, document: Document) {
        super.fileContentReloaded(file, document)
        heartbeatService.heartbeat()
    }

}
