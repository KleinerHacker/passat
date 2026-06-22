package org.pcsoft.passat.plugin.icon

import com.intellij.openapi.util.IconLoader

/** Icons used by the Passat plugin UI (module type, FPC SDK). */
object PassatIcons {
    @JvmField
    val PascalModule = IconLoader.getIcon("/icons/pascalModule.svg", PassatIcons::class.java)

    @JvmField
    val FpcSdk = IconLoader.getIcon("/icons/fpcSdk.svg", PassatIcons::class.java)
}
