# Object Pascal language status

This file tracks the current implementation status of the **entire Object Pascal language** in
Passat: which language constructs, dialect features and version levels are already supported by the
language core (lexer, parser, PSI and the features layered on top) and which are still missing.

Keep this document in sync with the actual capabilities of the `:language` module. All content is
written in **English**.

## Legend

- ✅ Implemented
- 🚧 In progress / partial
- ❌ Not yet implemented

## Status overview

| Area                         | Status | Notes |
|------------------------------|:------:|-------|
| Lexer (tokens, comments)     |   🚧   | JFlex lexer; case-insensitive keywords `program`/`begin`/`end`, identifier, `;`, `.`. No comments/literals yet |
| Parser / grammar (PSI)       |   🚧   | Grammar-Kit; only an empty program `program X; begin end.` |
| Syntax highlighting          |   🚧   | Keyword highlighting (Java keyword color, theme-aware) |
| Units & program structure    |   🚧   | Empty `program` shell only; `unit`, `library`, `uses` missing |
| Declarations                 |   ❌   | const, type, var, procedure/function |
| Types                        |   ❌   | records, classes, enums, sets, arrays, generics |
| Statements & expressions     |   ❌   | control flow, operators |
| Classes & OOP                 |   ❌   | classes, interfaces, properties, visibility |
| Generics                     |   ❌   | generic types and methods |
| Compiler directives          |   ❌   | `{$...}` directives, conditional compilation |
| Dialects / language versions |   ❌   | FPC dialect levels (e.g. `objfpc`, `delphi`) |

## Notes

Update the table above (and add detail subsections as needed) whenever language support changes.

### Phase 1 — first step (current)

The language core stands up the full JFlex → Grammar-Kit → PSI → highlighting pipeline for the
smallest valid program:

```pascal
program Demo;
begin
end.
```

Lexing is case-insensitive (`PROGRAM`/`Program`/`program` are all the `program` keyword). Registered
features: `ObjectPascalFileType` (`.pas`/`.pp`/`.lpr`), `ObjectPascalParserDefinition` and
keyword `SyntaxHighlighter`. Everything beyond this empty-program shell is still missing.
