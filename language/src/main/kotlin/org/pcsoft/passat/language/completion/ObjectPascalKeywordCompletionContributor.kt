package org.pcsoft.passat.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.pcsoft.passat.language.ObjectPascalLanguage
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes

/**
 * Suggests structural keywords only where they are grammatically meaningful, deduced from the tokens
 * preceding the caret:
 *
 * - `program` / `unit` — at the very start of the file (root, before anything else).
 * - `interface` — after a unit header (`unit X;`), opening the unit's interface section.
 * - `implementation` — after the `interface` keyword (and its optional `uses` clause).
 * - `uses` — after the program header (`program X;`) while still above the first `begin`
 *   (outside the block); also right after a unit's `interface`/`implementation` section keyword.
 * - `initialization` — after a unit's `implementation` section (and its optional `uses` clause).
 * - `finalization` — after a unit's `initialization` keyword, or directly after `implementation`
 *   (the section is independently optional, so a unit may have only `finalization`).
 * - `begin` — at a program's top level after the header and after the last `uses` clause
 *   (i.e. where the main block starts).
 * - `end` — inside an open `begin` block (more `begin` than `end` seen so far), or as a unit's
 *   terminating `end` after its `implementation`/`initialization`/`finalization` section.
 */
class ObjectPascalKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement().withLanguage(ObjectPascalLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    val ctx = TokenContext.before(parameters)

                    if (ctx.isEmpty) {
                        result.addElement(keyword("program"))
                        result.addElement(keyword("unit"))
                        return
                    }
                    if (ctx.allowsInterface) {
                        result.addElement(keyword("interface"))
                    }
                    if (ctx.allowsImplementation) {
                        result.addElement(keyword("implementation"))
                    }
                    if (ctx.allowsUses) {
                        result.addElement(keyword("uses"))
                    }
                    if (ctx.allowsInitialization) {
                        result.addElement(keyword("initialization"))
                    }
                    if (ctx.allowsFinalization) {
                        result.addElement(keyword("finalization"))
                    }
                    if (ctx.allowsBegin) {
                        result.addElement(keyword("begin"))
                    }
                    if (ctx.openBlocks > 0 || ctx.allowsUnitEnd) {
                        result.addElement(keyword("end"))
                    }
                }
            },
        )
    }

    /** Token facts about everything to the left of the caret, used to gate keyword suggestions. */
    private class TokenContext(
        val isEmpty: Boolean,
        private val hasProgram: Boolean,
        private val hasUnit: Boolean,
        private val hasInterface: Boolean,
        private val hasImplementation: Boolean,
        private val hasInitialization: Boolean,
        private val hasFinalization: Boolean,
        private val endCount: Int,
        private val openBlocksValue: Int,
        private val lastMeaningful: IElementType?,
    ) {
        val openBlocks: Int get() = openBlocksValue

        /**
         * `uses` is valid wherever a `uses` clause may (repeatedly) appear: after the program header,
         * inside a unit's interface section, or inside its implementation section. Like Java imports,
         * several consecutive `uses` clauses are allowed, so a `;` that closed a preceding clause also
         * permits another `uses` — the trailing keyword need not be the section keyword itself.
         */
        val allowsUses: Boolean
            get() {
                val inProgramHeader = hasProgram && openBlocksValue == 0 &&
                    lastMeaningful == ObjectPascalTypes.SEMICOLON
                val inInterface = hasInterface && !hasImplementation &&
                    (lastMeaningful == ObjectPascalTypes.INTERFACE || lastMeaningful == ObjectPascalTypes.SEMICOLON)
                val inImplementation = hasImplementation && endCount == 0 && openBlocksValue == 0 &&
                    !hasInitialization && !hasFinalization &&
                    (lastMeaningful == ObjectPascalTypes.IMPLEMENTATION || lastMeaningful == ObjectPascalTypes.SEMICOLON)
                return inProgramHeader || inInterface || inImplementation
            }

        /** `interface` opens a unit's interface section, right after the `unit X;` header. */
        val allowsInterface: Boolean
            get() = hasUnit && !hasInterface && openBlocksValue == 0 &&
                lastMeaningful == ObjectPascalTypes.SEMICOLON

        /**
         * `implementation` follows the interface section — directly after the `interface` keyword
         * or after the optional `uses` clause that closes it (a `;`).
         */
        val allowsImplementation: Boolean
            get() = hasInterface && !hasImplementation &&
                (lastMeaningful == ObjectPascalTypes.INTERFACE || lastMeaningful == ObjectPascalTypes.SEMICOLON)

        /**
         * `initialization` opens the unit's optional initialization section — directly after the
         * `implementation` keyword or after the optional `uses` clause that closes it (a `;`).
         */
        val allowsInitialization: Boolean
            get() = hasImplementation && !hasInitialization && endCount == 0 &&
                (lastMeaningful == ObjectPascalTypes.IMPLEMENTATION || lastMeaningful == ObjectPascalTypes.SEMICOLON)

        /**
         * `finalization` is an independent optional section: it may follow the `initialization`
         * keyword or, when there is no initialization section, the `implementation` section directly
         * (after its keyword or optional `uses` clause).
         */
        val allowsFinalization: Boolean
            get() = hasImplementation && !hasFinalization && endCount == 0 &&
                (lastMeaningful == ObjectPascalTypes.INITIALIZATION ||
                    lastMeaningful == ObjectPascalTypes.IMPLEMENTATION ||
                    lastMeaningful == ObjectPascalTypes.SEMICOLON)

        /**
         * A unit's terminating `end` is valid once the `implementation` section is open and the unit
         * has not been terminated yet — directly after `implementation` (and its optional `uses`
         * clause) or after the `initialization`/`finalization` keyword.
         */
        val allowsUnitEnd: Boolean
            get() = hasImplementation && endCount == 0 && openBlocksValue == 0 &&
                (lastMeaningful == ObjectPascalTypes.IMPLEMENTATION ||
                    lastMeaningful == ObjectPascalTypes.SEMICOLON ||
                    lastMeaningful == ObjectPascalTypes.INITIALIZATION ||
                    lastMeaningful == ObjectPascalTypes.FINALIZATION)

        /**
         * `begin` (the main block) is valid at a program's top level after the header and after the
         * last `uses` clause — i.e. once a `;` has closed the preceding statement and no block is open.
         */
        val allowsBegin: Boolean
            get() = hasProgram && openBlocksValue == 0 && lastMeaningful == ObjectPascalTypes.SEMICOLON

        companion object {
            fun before(parameters: CompletionParameters): TokenContext {
                val caret = parameters.offset
                val tokens = SyntaxTraverser.psiTraverser(parameters.position.containingFile)
                    .traverse()
                    .filter { it.firstChild == null && it.textRange.endOffset <= caret }
                    .filter { it.elementType != TokenType.WHITE_SPACE }
                    .toList()

                var begins = 0
                var ends = 0
                var hasProgram = false
                var hasUnit = false
                var hasInterface = false
                var hasImplementation = false
                var hasInitialization = false
                var hasFinalization = false
                for (token in tokens) {
                    when (token.elementType) {
                        ObjectPascalTypes.BEGIN -> begins++
                        ObjectPascalTypes.END -> ends++
                        ObjectPascalTypes.PROGRAM -> hasProgram = true
                        ObjectPascalTypes.UNIT -> hasUnit = true
                        ObjectPascalTypes.INTERFACE -> hasInterface = true
                        ObjectPascalTypes.IMPLEMENTATION -> hasImplementation = true
                        ObjectPascalTypes.INITIALIZATION -> hasInitialization = true
                        ObjectPascalTypes.FINALIZATION -> hasFinalization = true
                    }
                }
                return TokenContext(
                    isEmpty = tokens.isEmpty(),
                    hasProgram = hasProgram,
                    hasUnit = hasUnit,
                    hasInterface = hasInterface,
                    hasImplementation = hasImplementation,
                    hasInitialization = hasInitialization,
                    hasFinalization = hasFinalization,
                    endCount = ends,
                    openBlocksValue = (begins - ends).coerceAtLeast(0),
                    lastMeaningful = tokens.lastOrNull()?.elementType,
                )
            }
        }
    }

    private companion object {
        /** Keywords (except `end`) read better followed by a space; place the caret after it. */
        private val APPEND_SPACE = InsertHandler<LookupElement> { ctx: InsertionContext, _ ->
            ctx.document.insertString(ctx.tailOffset, " ")
            ctx.editor.caretModel.moveToOffset(ctx.tailOffset + 1)
        }

        fun keyword(text: String): LookupElement {
            val builder = LookupElementBuilder.create(text).bold()
            return if (text == "end") builder else builder.withInsertHandler(APPEND_SPACE)
        }
    }
}
