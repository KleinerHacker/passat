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

- Language: **Kotlin** (add `src/main/java` if Java is needed).
- Build: **Gradle** with the **IntelliJ Platform Gradle Plugin 2.16.0**.
- Target platform: **IntelliJ IDEA 2025.3.5**.
- Base package: `org.pcsoft.intellij.plugin`.
- Plugin manifest: `src/main/resources/META-INF/plugin.xml`.
- Key Gradle tasks: `runIde` (launch a sandbox IDE with the plugin), `buildPlugin` (build the
  installable zip), `test`, `verifyPlugin`.
- Predefined Run/Debug configurations live in `.run/`.

## Conventions

- **English everywhere** (code, comments, docs, commit messages).
- **Plugin-first development:** keep features installable into stock JetBrains IDEs; do not couple
  to a standalone-only assumption.
