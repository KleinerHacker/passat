package org.pcsoft.passat.language.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import org.pcsoft.passat.language.parser.psi.ObjectPascalBlock
import org.pcsoft.passat.language.parser.psi.ObjectPascalProgramDefinition
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes
import org.pcsoft.passat.language.parser.psi.ObjectPascalUnitDefinition
import org.pcsoft.passat.language.parser.psi.ObjectPascalUsesClause

/**
 * Provides code folding for Object Pascal:
 *
 *  - every `begin ... end` block (the fold reaches through the trailing `;` or `.`),
 *  - each run of consecutive `uses` clauses within a section (Java-imports style, collapsed by
 *    default); the `implementation` keyword separates the interface-uses run from the
 *    implementation-uses run, so a unit yields up to two independent uses folds,
 *  - the `interface`, `implementation`, `initialization` and `finalization` sections of a unit
 *    (keyword included in the fold, body hidden behind the placeholder).
 *
 * The grammar currently carries no member statements inside the sections, so folds are built from
 * AST token ranges. As the grammar grows the section folds will automatically span the new members.
 */
class ObjectPascalFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // begin ... end<;|.>
        for (block in PsiTreeUtil.findChildrenOfType(root, ObjectPascalBlock::class.java)) {
            addBlockFold(block, document, descriptors)
        }

        // uses runs (program and unit headers both carry uses clauses)
        for (program in PsiTreeUtil.findChildrenOfType(root, ObjectPascalProgramDefinition::class.java)) {
            addUsesFolds(program, document, descriptors)
        }
        for (unit in PsiTreeUtil.findChildrenOfType(root, ObjectPascalUnitDefinition::class.java)) {
            addUsesFolds(unit, document, descriptors)
            addSectionFolds(unit, document, descriptors)
        }

        return descriptors.toTypedArray()
    }

    /** Fold a `begin ... end` block, extending the range through a trailing `;` or `.`. */
    private fun addBlockFold(block: ObjectPascalBlock, document: Document, out: MutableList<FoldingDescriptor>) {
        var end = block.textRange.endOffset
        val terminator = block.siblings(forward = true, withSelf = false)
            .firstOrNull { it !is PsiWhiteSpace && it !is PsiComment }
        if (terminator != null) {
            val type = terminator.node.elementType
            if (type == ObjectPascalTypes.DOT || type == ObjectPascalTypes.SEMICOLON) {
                end = terminator.textRange.endOffset
            }
        }
        addFold(block.node, TextRange(block.textRange.startOffset, end), document, "begin ... end", false, out)
    }

    /** Group consecutive `uses` clauses (whitespace/comments allowed between) into a single fold. */
    private fun addUsesFolds(parent: PsiElement, document: Document, out: MutableList<FoldingDescriptor>) {
        var runStart: ObjectPascalUsesClause? = null
        var runEnd: ObjectPascalUsesClause? = null

        fun flush() {
            val first = runStart ?: return
            val last = runEnd ?: return
            addFold(
                first.node,
                TextRange(first.textRange.startOffset, last.textRange.endOffset),
                document,
                "uses ...",
                collapsedByDefault = true,
                out,
            )
            runStart = null
            runEnd = null
        }

        var child = parent.firstChild
        while (child != null) {
            when {
                child is ObjectPascalUsesClause -> {
                    if (runStart == null) runStart = child
                    runEnd = child
                }
                child is PsiWhiteSpace || child is PsiComment -> { /* keep the current run open */ }
                else -> flush() // any other token (e.g. `implementation`) breaks the run
            }
            child = child.nextSibling
        }
        flush()
    }

    /** Fold each present unit section from its keyword to the start of the next section / closing `end`. */
    private fun addSectionFolds(unit: ObjectPascalUnitDefinition, document: Document, out: MutableList<FoldingDescriptor>) {
        val node = unit.node
        val interfaceKw = node.findChildByType(ObjectPascalTypes.INTERFACE)
        val implementationKw = node.findChildByType(ObjectPascalTypes.IMPLEMENTATION)
        val initializationKw = node.findChildByType(ObjectPascalTypes.INITIALIZATION)
        val finalizationKw = node.findChildByType(ObjectPascalTypes.FINALIZATION)
        val endKw = node.findChildByType(ObjectPascalTypes.END)

        fun startOf(vararg candidates: ASTNode?): Int? =
            candidates.firstOrNull { it != null }?.startOffset

        interfaceKw?.let {
            startOf(implementationKw, initializationKw, finalizationKw, endKw)
                ?.let { boundary -> addSectionFold(it, boundary, document, "interface ...", out) }
        }
        implementationKw?.let {
            startOf(initializationKw, finalizationKw, endKw)
                ?.let { boundary -> addSectionFold(it, boundary, document, "implementation ...", out) }
        }
        initializationKw?.let {
            startOf(finalizationKw, endKw)
                ?.let { boundary -> addSectionFold(it, boundary, document, "initialization ...", out) }
        }
        finalizationKw?.let {
            endKw?.startOffset?.let { boundary -> addSectionFold(it, boundary, document, "finalization ...", out) }
        }
    }

    private fun addSectionFold(
        keyword: ASTNode,
        boundary: Int,
        document: Document,
        placeholder: String,
        out: MutableList<FoldingDescriptor>,
    ) {
        // Trim trailing whitespace so the fold does not swallow the blank line before the next keyword.
        var end = boundary
        val text = document.charsSequence
        while (end > keyword.startOffset && text[end - 1].isWhitespace()) end--
        val range = TextRange(keyword.startOffset, end)
        if (document.getLineNumber(range.startOffset) < document.getLineNumber(range.endOffset)) {
            addFold(keyword, range, document, placeholder, false, out)
        }
    }

    private fun addFold(
        node: ASTNode,
        range: TextRange,
        document: Document,
        placeholder: String,
        collapsedByDefault: Boolean,
        out: MutableList<FoldingDescriptor>,
    ) {
        if (range.isEmpty) return
        out += FoldingDescriptor(node, range, null, emptySet<Any>(), false, placeholder, collapsedByDefault)
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
