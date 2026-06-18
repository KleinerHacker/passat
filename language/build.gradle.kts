import org.jetbrains.intellij.platform.gradle.TestFrameworkType

// Reusable Object Pascal language core: lexer, parser, PSI and all PSI-level IDE features
// (highlighting, brace matching, folding, commenter, structure view, completion, references,
// find usages, inspections, formatter). It depends ONLY on the platform's core language APIs so
// it can be reused outside Passat - no project model, toolchain, build, run or debug dependencies.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform.module")
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        intellijIdea(libs.versions.platform.get())
        testFramework(TestFrameworkType.Platform)
    }
}
