package org.pcsoft.passat.language

import com.intellij.testFramework.ParsingTestCase
import org.pcsoft.passat.language.parser.ObjectPascalParserDefinition

/**
 * Parsing tests for the minimal Object Pascal grammar (an empty program). Each `.pas` test data
 * file under `src/test/resources/testData` has a matching `.txt` file holding the expected PSI tree.
 */
class ObjectPascalParsingTest : ParsingTestCase("", "pas", ObjectPascalParserDefinition()) {

    fun testEmptyProgram() = doTest(true)

    /** Object Pascal is case-insensitive: upper-case keywords must parse identically. */
    fun testEmptyProgramUpperCase() = doTest(true)

    /** A program with a `uses` clause importing several units. */
    fun testProgramWithUses() = doTest(true)

    /** A unit with `uses` clauses in both the interface and implementation sections. */
    fun testUnitWithUses() = doTest(true)

    /** Several consecutive `uses` clauses are allowed (like Java imports) in a program. */
    fun testProgramWithMultipleUses() = doTest(true)

    /** Several consecutive `uses` clauses are allowed in both unit sections. */
    fun testUnitWithMultipleUses() = doTest(true)

    /** A unit with optional `initialization` and `finalization` sections (in that order). */
    fun testUnitWithInitialization() = doTest(true)

    /** Both sections are independent: a unit may have only `finalization`. */
    fun testUnitWithFinalizationOnly() = doTest(true)

    override fun getTestDataPath(): String = "src/test/resources/testData"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = true
}
