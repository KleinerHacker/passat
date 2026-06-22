package org.pcsoft.passat.plugin.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.PassatBundle"

/**
 * Message bundle for all IntelliJ-UI-facing strings of the Passat plugin. Backed by
 * `resources/messages/PassatBundle.properties` (English, default) and localized only for the
 * IntelliJ standard languages (Chinese, Japanese, Korean).
 */
object PassatBundle : DynamicBundle(BUNDLE) {
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)

    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> =
        getLazyMessage(key, *params)
}
