<div class="passat-hero" markdown>
![Passat](assets/images/icon.png){ alt="Passat" }
</div>

# Passat

<p class="passat-tagline">An Open-Platform IDE for Object Pascal, built on the IntelliJ Platform.</p>

Passat brings first-class **Object Pascal** support to the IntelliJ Platform. It is built on top
of a **free Object Pascal compiler**, using the **Free Pascal Compiler (FPC)** as its reference
toolchain. The compiler and language-version handling are abstracted, so other free Object Pascal
compilers can be added later.

Passat is developed **plugin-first**: it ships as a plugin you can install into existing JetBrains
IDEs, and a **standalone IDE** distribution is packaged from the very same codebase at a later stage.

## Features

- **Dedicated Pascal project & module type** — like Java and Kotlin modules, each Pascal module
  carries a target compiler (an FPC installation registered as an SDK), a target language version
  (the Object Pascal dialect level) and its own dependency list.
- **Full language support** — lexer and parser producing a PSI tree, syntax highlighting, code
  completion, navigation (go to declaration / find usages), structure view, refactoring and
  inspections.
- **Build & run** — compile and run your projects against the configured compiler through dedicated
  run configurations.
- **Debugging** — full IDE debugging: breakpoints, stepping, variable inspection and expression
  evaluation.
- **Dual delivery** — one codebase, two products: an installable plugin for existing JetBrains IDEs
  **and** a standalone Passat IDE.

## Roadmap

Passat is built in phases:

1. **Language parser & PSI** — lexer, grammar, PSI and basic highlighting.
2. **Project / module model** — Pascal module type with compiler SDK, language version, dependencies.
3. **Build & run** — compile and execute via FPC; run configurations.
4. **Debugging** — the full debug feature set.
5. **Standalone IDE packaging** — ship Passat as its own IDE in addition to the plugin.
