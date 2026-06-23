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
| Lexer (tokens, comments)     |   🚧   | JFlex lexer; case-insensitive keywords `program`/`unit`/`uses`/`interface`/`implementation`/`initialization`/`finalization`/`in`/`begin`/`end`, identifier, single-quoted string, `;`, `,`, `.`. No comments yet |
| Parser / grammar (PSI)       |   🚧   | Grammar-Kit; empty `program` and `unit` (with optional `initialization`/`finalization` sections) with one or more consecutive `uses` clauses |
| Syntax highlighting          |   🚧   | Keyword highlighting (Java keyword color, theme-aware) |
| Units & program structure    |   🚧   | `program` and `unit` (interface/implementation, optional `initialization`/`finalization`) shells with `uses` clauses; `library` and section statements missing |
| `uses` clause resolution     |   ✅   | Completion (suggestion box) + reference resolution of imported units; unresolved units flagged red. Resolves project/module units and FPC SDK units (`.ppu`/source) |
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

### `uses` clause — completion & resolution

The grammar now accepts a `program` and a `unit` with `interface`/`implementation` sections, plus
optional `initialization` and `finalization` sections (in that order). Like Java imports, each of
these places may carry several consecutive `uses` clauses:

```pascal
program Demo;
uses SysUtils, Classes;
begin
end.
```

Each imported unit is a resolvable reference. Available units are taken from the importing file's
resolve scope, i.e. project/module units **and** the FPC SDK's units. A unit resolves by file name
(case-insensitively) against Object Pascal sources (`.pas`/`.pp`) and compiled units (`.ppu`);
program files (`.lpr`) are excluded. New features:

- **Completion** (`completion.contributor`, in `:language`): a suggestion box listing every unit in
  scope inside a `uses` clause.
- **Resolution** (`ObjectPascalUsedUnitReference` via the `unitReference` mixin): Ctrl-click/navigate
  to the declaring unit file; backed by the platform `FileTypeIndex`/`FilenameIndex` only, keeping
  the `:language` core free of project-model dependencies.
- **Unresolved = error** (`annotator`): an unknown unit is highlighted red.

So the FPC SDK's standard units (e.g. `SysUtils`) are visible, the `:plugin` `FpcSdkType` attaches
the installation's `units/**` (`.ppu`) as SDK class roots and `source/**` as source roots
(`FpcSdkUtil.findUnitDirectories` / `findSourceDirectories`).
