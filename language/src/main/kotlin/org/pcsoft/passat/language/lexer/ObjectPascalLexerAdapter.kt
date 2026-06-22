package org.pcsoft.passat.language.lexer

import com.intellij.lexer.FlexAdapter
import org.pcsoft.passat.language.parser.ObjectPascalLexer

/**
 * Adapts the JFlex-generated [ObjectPascalLexer] to the platform [com.intellij.lexer.Lexer] API.
 */
class ObjectPascalLexerAdapter : FlexAdapter(ObjectPascalLexer(null))
