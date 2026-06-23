// Standalone Passat IDE.
//
// SCAFFOLD ONLY: this module reserves the place for the standalone IDE distribution and wires the
// dependency on the Passat plugin. Assembling a full custom IDE on top of the IntelliJ Platform is
// deferred to a later roadmap phase and will require additional platform/product configuration.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    // The standalone IDE ships the Passat plugin (which itself bundles the :language core).
    implementation(project(":plugin"))

    intellijPlatform {
        intellijIdea("2025.3.5")
    }
}

// SCAFFOLD ONLY: there is no standalone-IDE assembly yet. Disable the auto-registered run/build
// tasks so an unqualified `./gradlew runIde` (or buildPlugin) does not launch/produce a second,
// redundant artifact alongside the real `:plugin` ones. Use `:plugin:runIde` for development.
tasks {
    runIde { enabled = false }
    buildPlugin { enabled = false }
}
