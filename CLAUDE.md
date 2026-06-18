# CLAUDE.md

This file gives Claude Code (and contributors) the full context, vision, and roadmap for the
**Passat** project. All project content — code, comments, documentation — is written in **English**.

## Project summary

Passat is an **Open-Platform-based IDE for Object Pascal**, built on the IntelliJ Platform.
Its goal is first-class support for Object Pascal on top of a **free Object Pascal compiler**,
using the **Free Pascal Compiler (FPC)** as the reference toolchain. The compiler and
language-version handling are abstracted so that other free Object Pascal compilers can be
added later.

Passat is developed **plugin-first**: it is built and shipped as a plugin that can be installed
into existing JetBrains IDEs. A **standalone IDE** distribution is then packaged on top of the
same codebase at a later stage.

## Goals

- **Dedicated Pascal project & module type** — analogous to Java and Kotlin modules, each Pascal
  module carries:
  - a **target compiler** (an FPC installation registered as an SDK/toolchain),
  - a **target language version** (the Object Pascal dialect/version level),
  - a **dependency list**.
- **Full language support** — lexer/parser producing a PSI tree, syntax highlighting, code
  completion, navigation (go to declaration/usages), structure view, refactoring, and inspections.
- **Build & run** — compile and run projects against the configured compiler via dedicated run
  configurations.
- **Debugging** — full IDE debugging support (breakpoints, stepping, variable inspection,
  evaluate expression).
- **Dual delivery** — installable plugin for existing JetBrains IDEs **and** a standalone IDE
  distribution from one codebase.

## Module layout

Passat is a **multi-module Gradle build** (`settings.gradle.kts` includes all three). All code lives
under the root package **`org.pcsoft.passat`** and the Gradle `group` is `org.pcsoft.passat`.

- **`:language`** (`org.pcsoft.passat.language`) — the **reusable** Object Pascal language core:
  lexer, parser, PSI, and *all PSI-level IDE features* (syntax highlighting, brace matching, folding,
  commenter, structure view, completion, references/resolve, find usages, syntax-level annotators/
  inspections, formatter). It depends **only** on the platform's core language APIs — never on the
  project/module model, toolchain/SDK, build, run or debug — so it can be reused outside Passat.
  Uses the `org.jetbrains.intellij.platform.module` Gradle plugin and ships a content-module
  descriptor `passat.language.xml` (resources root).
- **`:plugin`** (`org.pcsoft.passat.plugin`) — the installable Passat plugin. Depends on `:language`
  (bundled as the `passat.language` content module, declared via `<content>` in `plugin.xml`) and
  adds everything needing project context: Pascal module/project type, FPC toolchain/SDK + language
  version, dependency list, run configurations, build/compile integration, debugger. Uses the
  `org.jetbrains.intellij.platform` Gradle plugin.
- **`:ide`** (`org.pcsoft.passat.ide`) — the standalone Passat IDE. **Scaffold only** for now
  (depends on `:plugin`); full standalone-IDE assembly is roadmap phase 5.

**Reuse boundary rule:** never add a project-model/toolchain/build/run/debug dependency to
`:language`. If a feature needs the project context, it belongs in `:plugin`.

Key tasks: `:plugin:runIde`, `:plugin:buildPlugin`, `:language:build`, `:plugin:check`,
`:plugin:verifyPlugin`. Note `CHANGELOG.md` is at the repo root and `:plugin` points the changelog
plugin at it.

## Architecture intent

- Built on the **IntelliJ Platform SDK**.
- **Language / parser layer:** lexer via JFlex, grammar/PSI via Grammar-Kit; PSI-based language
  features layered on top.
- **Toolchain abstraction:** a pluggable compiler/SDK model so FPC is the first implementation
  while keeping the door open for other free Object Pascal compilers. Target language version is
  a property of this model.
- **Project model:** a custom module type / facet exposing compiler, language version, and
  dependencies in project settings.
- **Build / run / debug:** run-configuration and debugger extension points wired to the selected
  toolchain.
- **Packaging:** plugin artifact first (`:buildPlugin`), standalone IDE distribution later.

## Phased roadmap

1. **Language parser & PSI** — lexer, grammar, PSI, basic highlighting.
2. **Project / module model** — Pascal module type with compiler SDK, language version, dependencies.
3. **Build & run** — compile and execute via FPC; run configurations.
4. **Debugging** — full debug feature set.
5. **Standalone IDE packaging** — ship Passat as its own IDE in addition to the plugin.

## Tech stack & key facts

- **Implementation language: Kotlin.** All Passat code is written in Kotlin (add `src/main/java`
  only if Java interop is strictly required).
- Language: **Kotlin** (add `src/main/java` if Java is needed).
- Build: **Gradle** with the **IntelliJ Platform Gradle Plugin 2.16.0**.
- Target platform: **IntelliJ IDEA 2025.3.5**.
- Root package / Gradle group: `org.pcsoft.passat`.
- Plugin manifest: `plugin/src/main/resources/META-INF/plugin.xml`.
- Key Gradle tasks: `:plugin:runIde` (launch a sandbox IDE with the plugin), `:plugin:buildPlugin`
  (build the installable zip), `:language:build`, `:plugin:check`, `:plugin:verifyPlugin`.
- Predefined Run/Debug configurations live in `.run/`.

## Object Pascal language status

The current implementation status of the **entire Object Pascal language** (which language
constructs, dialect features and version levels are already lexed/parsed/supported and which are
still missing) is tracked in [`PASCAL_STATUS.md`](PASCAL_STATUS.md) at the repo root. Keep this
file up to date as language support evolves, and consult it before working on the language core.

## Conventions

- **English everywhere** (code, comments, docs, commit messages).
- **Plugin-first development:** keep features installable into stock JetBrains IDEs; do not couple
  to a standalone-only assumption.
