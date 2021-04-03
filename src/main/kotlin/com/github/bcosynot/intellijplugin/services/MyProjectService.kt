package com.github.bcosynot.intellijplugin.services

import com.github.bcosynot.intellijplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
