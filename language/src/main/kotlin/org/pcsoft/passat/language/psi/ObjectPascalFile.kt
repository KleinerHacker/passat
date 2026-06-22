package org.pcsoft.passat.language.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import org.pcsoft.passat.language.ObjectPascalFileType
import org.pcsoft.passat.language.ObjectPascalLanguage

/**
 * PSI root node for an Object Pascal source file.
 */
class ObjectPascalFile(viewProvider: FileViewProvider) :
    PsiFileBase(viewProvider, ObjectPascalLanguage) {

    override fun getFileType() = ObjectPascalFileType.INSTANCE

    override fun toString(): String = "Object Pascal File"
}
