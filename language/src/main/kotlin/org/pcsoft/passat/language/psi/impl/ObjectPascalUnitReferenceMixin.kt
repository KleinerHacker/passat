package org.pcsoft.passat.language.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes
import org.pcsoft.passat.language.psi.ObjectPascalUsedUnit
import org.pcsoft.passat.language.psi.ObjectPascalUsedUnitReference

/**
 * Base class injected (via the Grammar-Kit `mixin`) into the generated `unitReference` PSI
 * implementation, turning each imported unit into a resolvable [ObjectPascalUsedUnit].
 */
abstract class ObjectPascalUnitReferenceMixin(node: ASTNode) :
    ASTWrapperPsiElement(node), ObjectPascalUsedUnit {

    override fun getNameIdentifier(): PsiElement? = findChildByType(ObjectPascalTypes.IDENTIFIER)

    override fun getUnitName(): String = getNameIdentifier()?.text ?: ""

    override fun getReference(): PsiReference? {
        val nameElement = getNameIdentifier() ?: return null
        return ObjectPascalUsedUnitReference(this, nameElement)
    }

    override fun getReferences(): Array<PsiReference> =
        getReference()?.let { arrayOf(it) } ?: PsiReference.EMPTY_ARRAY
}
