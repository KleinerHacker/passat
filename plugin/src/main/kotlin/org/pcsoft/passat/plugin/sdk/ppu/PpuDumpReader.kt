package org.pcsoft.passat.plugin.sdk.ppu

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.logger
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Decodes compiled FPC units by invoking `ppudump -Fjson` and parsing its output into [PpuDumpUnit].
 * Kept free of IntelliJ SDK-model state so [parse] can be unit-tested against a captured sample dump.
 */
object PpuDumpReader {
    private val LOG = logger<PpuDumpReader>()

    // The Kotlin module makes Jackson honour data-class default values for absent properties (e.g.
    // a unit with no `Files`/`Interface`), so partial dumps bind without tripping null checks.
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // Some FPC units expose non-finite float constants; tolerate NaN/Infinity number literals.
        .configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true)

    // ppudump emits FPC's own spellings for non-finite floats (`Nan`, `Inf`, `-Inf`) as bare,
    // unquoted JSON values, which are not valid JSON and not the spellings Jackson accepts. Rewrite
    // them to the canonical NaN/Infinity tokens before parsing. The pattern only matches a value
    // position (after `:`), so quoted identifiers that happen to read "Nan" are untouched.
    private val nonFinite = Regex(""":(\s*)[+-]?(Nan|Inf)\b""")

    private fun normalize(json: String): String = nonFinite.replace(json) { m ->
        val sign = if (m.value.contains('-')) "-" else ""
        val token = if (m.groupValues[2] == "Nan") "NaN" else "Infinity"
        ":${m.groupValues[1]}$sign$token"
    }

    /**
     * Parses a complete `ppudump -Fjson` document (a JSON array of unit objects) into its units.
     * Pure and side-effect free — this is the seam exercised by tests with a real sample dump.
     */
    fun parse(json: String): List<PpuDumpUnit> =
        mapper.readValue(normalize(json), Array<PpuDumpUnit>::class.java).toList()

    /**
     * Runs `ppudump -Fjson -VIDS <ppuFile>` and returns the first decoded unit, or `null` if the
     * tool fails, times out or yields nothing parseable. `-VIDS` restricts output to the **interface**
     * (no implementation) while still emitting both symbol-table entries (types, consts, vars) and
     * definitions (procedures/functions, objects) — together the full set of identifiers the unit
     * exports. Uses a plain [ProcessBuilder] with a timeout, mirroring
     * [org.pcsoft.passat.plugin.sdk.FpcSdkUtil.detectVersion].
     */
    fun read(ppuDumpExe: File, ppuFile: File): PpuDumpUnit? {
        if (!ppuDumpExe.isFile || !ppuFile.isFile) return null
        return try {
            val process = ProcessBuilder(ppuDumpExe.absolutePath, "-Fjson", "-VIDS", ppuFile.absolutePath)
                .redirectErrorStream(false)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                LOG.warn("ppudump timed out for '${ppuFile.name}'")
                return null
            }
            if (process.exitValue() != 0 && output.isBlank()) return null
            parse(output).firstOrNull()
        } catch (e: Exception) {
            LOG.info("Failed to read ppu '${ppuFile.absolutePath}' with ppudump", e)
            null
        }
    }
}
