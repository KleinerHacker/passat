package org.pcsoft.passat.language.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes;

/*
 * Object Pascal lexer (JFlex). Object Pascal is case-insensitive, so %caseless makes all keyword
 * patterns match regardless of case (PROGRAM, Program, program ...). Phase 1, first step: only the
 * tokens needed for an empty program are recognized; the token set grows with PASCAL_STATUS.md.
 */
%%

%public
%class ObjectPascalLexer
%implements FlexLexer
%unicode
%caseless
%function advance
%type IElementType
%eof{ return;
%eof}

WHITE_SPACE=[\ \t\f\r\n]+
IDENTIFIER=[A-Za-z_][A-Za-z0-9_]*
// Single-quoted Object Pascal string; a doubled quote ('') is an escaped quote.
STRING='([^'\r\n]|'')*'

%%

<YYINITIAL> {
  {WHITE_SPACE}      { return TokenType.WHITE_SPACE; }

  "program"          { return ObjectPascalTypes.PROGRAM; }
  "unit"             { return ObjectPascalTypes.UNIT; }
  "uses"             { return ObjectPascalTypes.USES; }
  "interface"        { return ObjectPascalTypes.INTERFACE; }
  "implementation"   { return ObjectPascalTypes.IMPLEMENTATION; }
  "in"               { return ObjectPascalTypes.IN; }
  "begin"            { return ObjectPascalTypes.BEGIN; }
  "end"              { return ObjectPascalTypes.END; }

  ";"                { return ObjectPascalTypes.SEMICOLON; }
  ","                { return ObjectPascalTypes.COMMA; }
  "."                { return ObjectPascalTypes.DOT; }

  {STRING}           { return ObjectPascalTypes.STRING; }
  {IDENTIFIER}       { return ObjectPascalTypes.IDENTIFIER; }
}

[^]                  { return TokenType.BAD_CHARACTER; }
