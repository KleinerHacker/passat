package org.pcsoft.passat.plugin.sdk.ppu

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Kotlin representation of the JSON produced by `ppudump -Fjson <file.ppu>`. The tool prints a JSON
 * array whose first element is the unit object modelled by [PpuDumpUnit]; nested elements describe
 * the source files the unit was built from and its interface symbols.
 *
 * Only the fields Passat actually consumes are mapped — every type carries
 * [JsonIgnoreProperties] so the long tail of compiler-internal, symbol-type-specific attributes is
 * ignored without breaking parsing. The constructors are annotated for Jackson databind directly
 * (no kotlin-module dependency), since the IntelliJ Platform bundles only jackson-databind/core.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PpuDumpUnit @JsonCreator constructor(
    @param:JsonProperty("Type") val type: String? = null,
    /** The unit's real, correctly-cased internal name (e.g. `System`, `SysUtils`). */
    @param:JsonProperty("Name") val name: String? = null,
    @param:JsonProperty("Version") val version: Int? = null,
    @param:JsonProperty("TargetCPU") val targetCpu: String? = null,
    @param:JsonProperty("TargetOS") val targetOs: String? = null,
    @param:JsonProperty("CRC") val crc: String? = null,
    @param:JsonProperty("InterfaceCRC") val interfaceCrc: String? = null,
    /** Source files (`.pp`/`.inc`) the unit was compiled from; [PpuPos.file] indexes into this 1-based list. */
    @param:JsonProperty("Files") val files: List<PpuFile> = emptyList(),
    /** Interface-section symbols: types, objects, procedures/functions, consts, vars, etc. */
    @param:JsonProperty("Interface") val interfaceSymbols: List<PpuSymbol> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PpuFile @JsonCreator constructor(
    @param:JsonProperty("Type") val type: String? = null,
    @param:JsonProperty("Name") val name: String? = null,
    @param:JsonProperty("Time") val time: String? = null,
)

/**
 * One interface symbol. [type] is the `ppudump` kind (`proc`, `type`, `const`, `var`, `field`,
 * `prop`, `rec`, `obj`, `enum`, `proctype`, …). [name] is the declared identifier (may be absent for
 * anonymous/structural entries). Type-specific details are intentionally not modelled.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PpuSymbol @JsonCreator constructor(
    @param:JsonProperty("Type") val type: String? = null,
    @param:JsonProperty("Name") val name: String? = null,
    @param:JsonProperty("Pos") val pos: PpuPos? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PpuPos @JsonCreator constructor(
    @param:JsonProperty("File") val file: Int? = null,
    @param:JsonProperty("Line") val line: Int? = null,
    @param:JsonProperty("Col") val col: Int? = null,
)
