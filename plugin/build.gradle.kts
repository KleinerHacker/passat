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
    testImplementation(libs.junit)

    // Bundle the reusable language core into the plugin distribution.
    implementation(project(":language"))

    intellijPlatform {
        intellijIdea(libs.versions.platform.get())
        testFramework(TestFrameworkType.Platform)
    }
}

// CHANGELOG.md lives at the repository root, not in this module.
changelog {
    path = rootProject.file("CHANGELOG.md").canonicalPath
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
