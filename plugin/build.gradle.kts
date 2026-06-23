import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

// The Passat plugin: installable into existing JetBrains IDEs. It bundles the reusable
// :language core and adds everything that needs project context: the Pascal module/project type,
// the FPC toolchain/SDK and target language version, the dependency list, run configurations,
// the build/compile integration and the debugger.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // Bundle the reusable language core into the plugin distribution.
    implementation(project(":language"))

    // Jackson parses the JSON emitted by `ppudump -Fjson`. The IntelliJ Platform already ships
    // jackson-databind/core and the Kotlin module at runtime, so depend on them compile-only (and
    // for tests) to avoid bundling a second, possibly conflicting, copy. The version is aligned with
    // the Jackson version bundled in the IntelliJ Platform.
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    // The Kotlin module is needed for Jackson to honour data-class defaults. Exclude its transitive
    // Kotlin stdlib/reflect: the platform (and Kotlin plugin) already provide Kotlin at runtime, and
    // letting Jackson drag in a different version corrupts the platform test runtime.
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3") { exclude(group = "org.jetbrains.kotlin") }
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3") { exclude(group = "org.jetbrains.kotlin") }

    intellijPlatform {
        intellijIdea("2025.3.5")
        testFramework(TestFrameworkType.Platform)
    }
}

// CHANGELOG.md lives at the repository root, not in this module.
changelog {
    path = rootProject.file("CHANGELOG.md").canonicalPath
}

// Passat targets Object Pascal, not Java. Disable the bundled Java plugin in the development
// sandbox so the IDE offers only the FPC SDK (no JDK) and no Java language level — matching the
// intended Pascal-only product. This affects runIde only, not the shipped plugin.
tasks {
    prepareSandbox {
        disabledPlugins.add("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        // Render the plugin change notes from CHANGELOG.md (current project version, else Unreleased).
        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(project.version.toString()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }
}
