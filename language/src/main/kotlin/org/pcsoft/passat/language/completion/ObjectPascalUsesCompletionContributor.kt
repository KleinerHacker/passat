package org.pcsoft.passat.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.pcsoft.passat.language.ObjectPascalLanguage
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes
import org.pcsoft.passat.language.parser.psi.ObjectPascalUsesClause

/**
 * Offers the available unit names inside a `uses` clause — the suggestion box the user sees while
 * typing `uses <caret>`. Names come from [org.pcsoft.passat.language.psi.PascalUnits], so the box lists both project-local units
 * and the FPC SDK's standard units, each tagged with its originating file (see [UnitLookupElements]).
 *
 * The "in a `uses` clause" decision is made from the preceding tokens, not the parsed PSI: while the
 * clause is still being typed it has no terminating `;` yet, so the parser hasn't built a complete
 * [ObjectPascalUsesClause] node — gating on the PSI would withhold suggestions until the `;` exists,
 * which is useless for writing code.
 */
class ObjectPascalUsesCompletionContributor : CompletionContributor() {
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
                    if (!isInsideUsesClause(parameters)) return
                    val file = parameters.originalFile
                    UnitLookupElements.forScope(file.project, file.resolveScope)
                        .forEach { result.addElement(it) }
                }
            },
        )
    }

    /**
     * True when the caret sits inside a (possibly still-unterminated) `uses` clause: scanning the
     * tokens left of the caret, the nearest clause boundary is a `uses` keyword reached over only the
     * tokens that may appear inside a clause (unit names, `in 'file'`, commas). A `;` or any other
     * keyword closes the clause.
     */
    private fun isInsideUsesClause(parameters: CompletionParameters): Boolean {
        val caret = parameters.offset
        val tokens = SyntaxTraverser.psiTraverser(parameters.position.containingFile)
            .traverse()
            .filter { it.firstChild == null && it.textRange.endOffset <= caret }
            .filter { it.elementType != TokenType.WHITE_SPACE }
            .toList()
        for (token in tokens.asReversed()) {
            when (token.elementType) {
                ObjectPascalTypes.USES -> return true
                ObjectPascalTypes.IDENTIFIER,
                ObjectPascalTypes.COMMA,
                ObjectPascalTypes.IN,
                ObjectPascalTypes.STRING,
                -> Unit // still potentially inside the clause; keep scanning left
                else -> return false // `;` or any other keyword ends the clause
            }
        }
        return false
    }

    /**
     * Inside a `uses` clause, complete with the *trimmed* dummy identifier (no trailing space). The
     * platform's default dummy ends in a space, so invoking completion in the middle of an existing
     * unit name (`uses Sys<caret>Utils`) would split it into two adjacent identifiers — a parse error
     * that drops the caret out of the `uses` clause and yields no suggestions. The trimmed dummy keeps
     * the name a single identifier so the clause still parses and suggestions appear.
     */
    override fun beforeCompletion(context: CompletionInitializationContext) {
        val element = context.file.findElementAt(context.startOffset)
        if (element != null && PsiTreeUtil.getParentOfType(element, ObjectPascalUsesClause::class.java) != null) {
            context.dummyIdentifier = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        }
    }
}
