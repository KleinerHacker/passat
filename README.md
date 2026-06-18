# Passat

[![JetBrains Platform](https://img.shields.io/badge/JetBrains%20Platform-IntelliJ-blue)][docs]

**Passat** is an Open-Platform-based IDE and plugin for **Object Pascal**, built on the
IntelliJ Platform. It brings modern IDE tooling — a real language parser, smart editing,
project management, build, run, and debugging — to Object Pascal on top of a **free Object
Pascal compiler**, using the [Free Pascal Compiler (FPC)][fpc] as the reference toolchain.

> Status: **early development.** Most features described below are planned. See the
> [Roadmap](#roadmap) for the current direction.

## Background

Object Pascal remains a productive, widely used language, but free, modern IDE tooling for it
is limited. Passat aims to deliver a full IntelliJ-grade experience for Object Pascal while
staying built entirely on free compilers.

Passat is developed **plugin-first**: it is built and shipped as a plugin that installs into
existing JetBrains IDEs. From the same codebase, a **standalone Passat IDE** distribution is
packaged at a later stage. The compiler and language-version handling are abstracted, so FPC is
the first supported compiler while other free Object Pascal compilers can be added later.

## Features (planned)

- **Dedicated Pascal project & module type** — like Java and Kotlin modules, each Pascal module
  defines a **target compiler**, a **target language version**, and a **dependency list**.
- **Language tooling** — lexer/parser with a PSI tree, syntax highlighting, code completion,
  navigation, structure view, refactorings, and inspections.
- **Build & run** — compile and run projects against the configured compiler.
- **Debugging** — breakpoints, stepping, variable inspection, and expression evaluation.
- **Dual delivery** — install as a plugin into existing JetBrains IDEs, or use the standalone
  Passat IDE.

## Requirements

- A **JDK** compatible with the IntelliJ Platform (provided/resolved via the Gradle toolchain).
- A working **[Free Pascal Compiler (FPC)][fpc]** installation for building and running Pascal code.

## Getting started

Clone the repository:

```bash
git clone <repository-url>
cd passat
```

Run the plugin in a sandbox IDE:

```bash
# Linux / macOS
./gradlew :plugin:runIde

# Windows
gradlew.bat :plugin:runIde
```

This launches an IntelliJ IDEA sandbox with the Passat plugin loaded. You can also use the
predefined **Run/Debug configurations** in the `.run/` directory from within your IDE.

### Other useful tasks

| Task                             | Description                                                         |
|----------------------------------|--------------------------------------------------------------------|
| `./gradlew :plugin:runIde`       | Launch a sandbox IDE with the plugin (use Debug to debug it).      |
| `./gradlew :plugin:buildPlugin`  | Build the installable plugin zip (under `plugin/build/distributions/`). |
| `./gradlew :language:build`      | Build the reusable language core on its own.                       |
| `./gradlew :plugin:check`        | Run the test suite.                                                |
| `./gradlew :plugin:verifyPlugin` | Verify plugin compatibility against the targeted IntelliJ IDEs.    |

## Installing into an existing JetBrains IDE

1. Build the plugin: `./gradlew :plugin:buildPlugin`.
2. In your JetBrains IDE, go to **Settings/Preferences → Plugins → ⚙ → Install Plugin from Disk…**.
3. Select the zip from `plugin/build/distributions/`.

## Project structure

Passat is a multi-module Gradle build that separates concerns so the language tooling can be
reused independently of the IDE/plugin packaging:

```
.
├── .run/                       Predefined Run/Debug configurations
├── gradle/
│   ├── wrapper/                Gradle Wrapper
│   └── libs.versions.toml      Version catalog (incl. platform version)
├── language/                   :language — reusable Object Pascal language core
│   └── src/main/
│       ├── kotlin/             org.pcsoft.passat.language — lexer, parser, PSI, PSI-level features
│       └── resources/          passat.language.xml — content-module descriptor
├── plugin/                     :plugin — Passat plugin for existing JetBrains IDEs
│   └── src/main/
│       ├── kotlin/             org.pcsoft.passat.plugin — project model, FPC toolchain, run/build/debug
│       └── resources/META-INF/ plugin.xml, pluginIcon.svg
├── ide/                        :ide — standalone Passat IDE (scaffold; assembly is roadmap phase 5)
│   └── src/main/kotlin/        org.pcsoft.passat.ide
├── build.gradle.kts            Root build (shared configuration)
├── settings.gradle.kts         Module includes
├── gradle.properties           Gradle configuration properties
├── CLAUDE.md                   Project vision, goals, and roadmap
└── README.md                   This file
```

The `:language` module holds the reusable parser and **all PSI-level IDE features** (highlighting,
brace matching, folding, completion, references, find usages, inspections, formatter, …) and depends
only on the platform's core language APIs — never on the project model, toolchain, build, run or
debug. It is bundled into `:plugin` as an IntelliJ Platform content module (`passat.language`).

## Roadmap

1. **Language parser & PSI** — lexer, grammar, PSI, basic highlighting.
2. **Project / module model** — Pascal module type with compiler SDK, language version, dependencies.
3. **Build & run** — compile and execute via FPC; run configurations.
4. **Debugging** — full debug feature set.
5. **Standalone IDE packaging** — ship Passat as its own IDE in addition to the plugin.

## Technology

- **Kotlin** + **Gradle**, built with the **IntelliJ Platform Gradle Plugin 2.16.0**.
- Targets **IntelliJ IDEA 2025.3.5**.

## Useful links

- [IntelliJ Platform SDK][docs]
- [IntelliJ Platform Gradle Plugin][gradle-plugin]
- [Free Pascal Compiler][fpc]

[docs]: https://plugins.jetbrains.com/docs/intellij
[gradle-plugin]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
[fpc]: https://www.freepascal.org/
