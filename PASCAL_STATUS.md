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
| Lexer (tokens, comments)     |   ❌   | Not started |
| Parser / grammar (PSI)       |   ❌   | Not started |
| Syntax highlighting          |   ❌   | Not started |
| Units & program structure    |   ❌   | `program`, `unit`, `library`, `uses` |
| Declarations                 |   ❌   | const, type, var, procedure/function |
| Types                        |   ❌   | records, classes, enums, sets, arrays, generics |
| Statements & expressions     |   ❌   | control flow, operators |
| Classes & OOP                 |   ❌   | classes, interfaces, properties, visibility |
| Generics                     |   ❌   | generic types and methods |
| Compiler directives          |   ❌   | `{$...}` directives, conditional compilation |
| Dialects / language versions |   ❌   | FPC dialect levels (e.g. `objfpc`, `delphi`) |

## Notes

Update the table above (and add detail subsections as needed) whenever language support changes.
