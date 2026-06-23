package org.pcsoft.passat.plugin.icon

import com.intellij.openapi.util.IconLoader

/** Icons used by the Passat plugin UI (module type, FPC SDK). */
object PassatIcons {
    @JvmField
    val PascalModule = IconLoader.getIcon("/icons/pascalModule.svg", PassatIcons::class.java)

    @JvmField
    val FpcSdk = IconLoader.getIcon("/icons/fpcSdk.svg", PassatIcons::class.java)

    /**
     * File-type icon for the "New Object Pascal File" action. The plugin keeps its own copy because
     * the language core is bundled as a separate content module whose classes/resources are not
     * visible to the main plugin classloader.
     */
    @JvmField
    val PascalFile = IconLoader.getIcon("/icons/pascalFile.svg", PassatIcons::class.java)
}
