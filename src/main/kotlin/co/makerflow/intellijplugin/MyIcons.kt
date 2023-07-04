package co.makerflow.intellijplugin

import com.intellij.openapi.util.IconLoader

class MyIcons {

    @JvmField
    val pluginIcon = IconLoader.getIcon("/icons/pluginIcon.svg", javaClass)

    @JvmField
    val startTimer = IconLoader.getIcon("/icons/startTimer.svg", javaClass)
}
