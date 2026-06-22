package org.pcsoft.passat.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext
import org.pcsoft.passat.language.ObjectPascalLanguage
import org.pcsoft.passat.language.parser.psi.ObjectPascalUsesClause
import org.pcsoft.passat.language.psi.PascalUnits

/**
 * Offers the available unit names inside a `uses` clause — the suggestion box the user sees while
 * typing `uses <caret>`. Names come from [PascalUnits], so the box lists both project-local units
 * and the FPC SDK's standard units.
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
                    PascalUnits.availableUnitNames(file.project, file.resolveScope).forEach { name ->
                        result.addElement(LookupElementBuilder.create(name))
                    }
                }
            },
        )
    }
}
