package org.pcsoft.passat.plugin.sdk.ppu

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.logger
import org.pcsoft.passat.plugin.sdk.FpcSdkUtil
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * The persistence and `ppudump`-interpretation core behind [FpcUnitIndex], kept free of any
 * IntelliJ application/service state so it can be unit-tested directly against a backing file and a
 * real `.ppu`. Maps `.ppu` canonical path -> [IndexedUnit] (the unit's real name plus its interface
 * symbols), stamped with the file's `lastModified` so a changed unit is transparently re-read, and
 * mirrors the map to a single JSON document at [storeFile].
 */
class FpcUnitIndexStore(private val storeFile: File) {
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val entries: MutableMap<String, IndexedUnit> = ConcurrentHashMap()

    @Volatile
    private var loaded = false

    /** A cached unit: its real name, the symbols it exports, and the source `.ppu` mtime stamp. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class IndexedUnit @JsonCreator constructor(
        @param:JsonProperty("name") val name: String,
        @param:JsonProperty("lastModified") val lastModified: Long,
        @param:JsonProperty("symbols") val symbols: List<IndexedSymbol> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class IndexedSymbol @JsonCreator constructor(
        @param:JsonProperty("type") val type: String?,
        @param:JsonProperty("name") val name: String,
    )

    /** The real internal unit name cached for [ppu] at its current mtime, or `null` if not indexed. */
    fun internalUnitName(ppu: File): String? = lookup(ppu)?.name

    /** The interface symbols cached for [ppu] at its current mtime, or an empty list if not indexed. */
    fun symbolsOf(ppu: File): List<IndexedSymbol> = lookup(ppu)?.symbols ?: emptyList()

    private fun lookup(ppu: File): IndexedUnit? {
        ensureLoaded()
        val entry = entries[ppu.canonicalPath] ?: return null
        return if (entry.lastModified == ppu.lastModified()) entry else null
    }

    /**
     * Reads every `.ppu` under the given FPC home with `ppudump` and fills the cache, skipping units
     * already indexed at their current mtime. Spawns one external process per (new/changed) unit, so
     * it must run off the EDT. Persists when anything changed. Returns the number of units indexed.
     */
    fun ensureIndexed(home: String): Int {
        val exe = FpcSdkUtil.findPpuDumpExecutable(home) ?: run {
            LOG.info("Cannot index FPC home '$home': no ppudump executable")
            return 0
        }
        ensureLoaded()
        var indexed = 0
        FpcSdkUtil.findUnitDirectories(home).forEach { dir ->
            dir.listFiles { _, n -> n.endsWith(".ppu", ignoreCase = true) }?.forEach { ppu ->
                if (entries[ppu.canonicalPath]?.lastModified == ppu.lastModified()) return@forEach
                if (index(exe, ppu)) indexed++
            }
        }
        if (indexed > 0) save()
        return indexed
    }

    /**
     * Reads a single `.ppu` with `ppudump` at [exe] and stores it (without persisting). Returns true
     * when a unit was decoded and cached. Exposed for fine-grained testing of ppu interpretation.
     */
    fun index(exe: File, ppu: File): Boolean {
        val unit = PpuDumpReader.read(exe, ppu) ?: return false
        put(ppu, unit)
        return true
    }

    /** Maps a decoded [unit] into the cache under [ppu]'s key and mtime. */
    fun put(ppu: File, unit: PpuDumpUnit) {
        val name = unit.name ?: return
        val symbols = unit.interfaceSymbols
            .filter { !it.name.isNullOrBlank() }
            .map { IndexedSymbol(it.type, it.name!!) }
            .distinctBy { it.type to it.name }
        entries[ppu.canonicalPath] = IndexedUnit(name, ppu.lastModified(), symbols)
    }

    @Synchronized
    fun ensureLoaded() {
        if (loaded) return
        try {
            if (storeFile.isFile) {
                mapper.readValue(storeFile, StoredIndex::class.java).units.let(entries::putAll)
            }
        } catch (e: Exception) {
            LOG.info("Could not load FPC unit index from '${storeFile.absolutePath}', starting empty", e)
        } finally {
            loaded = true
        }
    }

    @Synchronized
    fun save() {
        try {
            storeFile.parentFile?.mkdirs()
            mapper.writeValue(storeFile, StoredIndex(entries.toMap()))
        } catch (e: Exception) {
            LOG.warn("Could not persist FPC unit index to '${storeFile.absolutePath}'", e)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class StoredIndex @JsonCreator constructor(
        @param:JsonProperty("units") val units: Map<String, IndexedUnit> = emptyMap(),
    )

    private companion object {
        val LOG = logger<FpcUnitIndexStore>()
    }
}
