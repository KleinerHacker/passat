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
 * - `program` — at the very start of the file (root, before anything else).
 * - `uses` — after the program header (`program X;`) while still above the first `begin`
 *   (outside the block); also right after a unit's `interface`/`implementation` section keyword.
 * - `begin` — at a program's top level after the header and after the last `uses` clause
 *   (i.e. where the main block starts).
 * - `end` — inside an open `begin` block (more `begin` than `end` seen so far).
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
                        return
                    }
                    if (ctx.allowsUses) {
                        result.addElement(keyword("uses"))
                    }
                    if (ctx.allowsBegin) {
                        result.addElement(keyword("begin"))
                    }
                    if (ctx.openBlocks > 0) {
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
        private val hasUses: Boolean,
        private val openBlocksValue: Int,
        private val lastMeaningful: IElementType?,
    ) {
        val openBlocks: Int get() = openBlocksValue

        /** `uses` is valid after the program header (before any `begin`) or after a section keyword. */
        val allowsUses: Boolean
            get() = (hasProgram && openBlocksValue == 0 && !hasUses && lastMeaningful == ObjectPascalTypes.SEMICOLON) ||
                lastMeaningful == ObjectPascalTypes.INTERFACE ||
                lastMeaningful == ObjectPascalTypes.IMPLEMENTATION

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
                var hasUses = false
                for (token in tokens) {
                    when (token.elementType) {
                        ObjectPascalTypes.BEGIN -> begins++
                        ObjectPascalTypes.END -> ends++
                        ObjectPascalTypes.PROGRAM -> hasProgram = true
                        ObjectPascalTypes.USES -> hasUses = true
                    }
                }
                return TokenContext(
                    isEmpty = tokens.isEmpty(),
                    hasProgram = hasProgram,
                    hasUses = hasUses,
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
