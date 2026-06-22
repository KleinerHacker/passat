package org.pcsoft.passat.language

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type for Object Pascal source files. Object Pascal sources use several extensions; `pas` is
 * the common unit/program extension, `pp` is the FPC default and `lpr` is a Lazarus program file.
 */
class ObjectPascalFileType private constructor() : LanguageFileType(ObjectPascalLanguage) {
    override fun getName(): String = "Object Pascal"

    override fun getDescription(): String = "Object Pascal source file"

    override fun getDefaultExtension(): String = "pas"

    override fun getIcon(): Icon = ObjectPascalIcons.File

    companion object {
        @JvmField
        val INSTANCE = ObjectPascalFileType()
    }
}
