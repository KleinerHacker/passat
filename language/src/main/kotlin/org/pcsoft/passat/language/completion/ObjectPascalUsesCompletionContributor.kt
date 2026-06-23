package org.pcsoft.passat.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.pcsoft.passat.language.ObjectPascalLanguage
import org.pcsoft.passat.language.parser.psi.ObjectPascalUsesClause

/**
 * Offers the available unit names inside a `uses` clause — the suggestion box the user sees while
 * typing `uses <caret>`. Names come from [org.pcsoft.passat.language.psi.PascalUnits], so the box lists both project-local units
 * and the FPC SDK's standard units, each tagged with its originating file (see [UnitLookupElements]).
 */
class ObjectPascalUsesCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement()
                .withLanguage(ObjectPascalLanguage)
                .inside(psiElement(ObjectPascalUsesClause::class.java)),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    val file = parameters.originalFile
                    UnitLookupElements.forScope(file.project, file.resolveScope)
                        .forEach { result.addElement(it) }
                }
            },
        )
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
