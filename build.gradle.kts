// Root project: shared configuration only. The actual artifacts are produced by the subprojects:
//   :language - reusable Object Pascal language core (parser + PSI-level IDE features)
//   :plugin   - the Passat plugin installable into existing JetBrains IDEs
//   :ide      - standalone Passat IDE (scaffold; full assembly is a later roadmap phase)
//
// Plugin/language logic is applied per-module. See each module's build.gradle.kts.

allprojects {
    group = providers.gradleProperty("group").get()
    version = providers.gradleProperty("version").get()
}
