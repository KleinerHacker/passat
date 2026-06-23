package org.pcsoft.passat.plugin.sdk.ppu

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import java.io.File

/**
 * Application-level cache of the data Passat reads from compiled FPC units via `ppudump`, so a
 * single installation shared by several projects is indexed once and the (relatively expensive)
 * `ppudump` invocations are paid once per installation rather than on every completion. All real
 * work lives in [FpcUnitIndexStore]; this service just binds it to a store file under the IDE system
 * directory. See [FpcUnitIndexStore] for the indexing/persistence/interpretation logic.
 */
@Service(Service.Level.APP)
class FpcUnitIndex {
    private val store = FpcUnitIndexStore(
        PathManager.getSystemDir().resolve("passat").resolve("fpc-units.json").toFile()
    )

    /** The real internal unit name cached for [ppu], or `null` if it has not been indexed yet. */
    fun internalUnitName(ppu: File): String? = store.internalUnitName(ppu)

    /** The interface symbols cached for [ppu], or an empty list if it has not been indexed yet. */
    fun symbolsOf(ppu: File): List<FpcUnitIndexStore.IndexedSymbol> = store.symbolsOf(ppu)

    /** Reads and caches every `.ppu` under the given FPC home; must run off the EDT. */
    fun ensureIndexed(home: String): Int = store.ensureIndexed(home)

    companion object {
        @JvmStatic
        fun getInstance(): FpcUnitIndex =
            ApplicationManager.getApplication().getService(FpcUnitIndex::class.java)
    }
}
