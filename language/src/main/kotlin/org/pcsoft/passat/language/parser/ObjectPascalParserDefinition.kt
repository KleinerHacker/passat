package org.pcsoft.passat.language.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.pcsoft.passat.language.ObjectPascalLanguage
import org.pcsoft.passat.language.lexer.ObjectPascalLexerAdapter
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes
import org.pcsoft.passat.language.psi.ObjectPascalFile

/**
 * Wires the Object Pascal lexer, parser and PSI factory into the platform.
 */
class ObjectPascalParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = ObjectPascalLexerAdapter()

    override fun createParser(project: Project?): PsiParser = ObjectPascalParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createElement(node: ASTNode): PsiElement = ObjectPascalTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = ObjectPascalFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(ObjectPascalLanguage)
    }
}
