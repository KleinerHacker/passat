import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

// Reusable Object Pascal language core: lexer, parser, PSI and all PSI-level IDE features
// (highlighting, brace matching, folding, commenter, structure view, completion, references,
// find usages, inspections, formatter). It depends ONLY on the platform's core language APIs so
// it can be reused outside Passat - no project model, toolchain, build, run or debug dependencies.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform.module")
    id("org.jetbrains.grammarkit")
}

// The Grammar-Kit plugin declares project-level repositories, which makes Gradle ignore the
// repositories from settings.gradle.kts for this module. Re-declare them here so the IntelliJ
// Platform dependency can still be resolved.
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        intellijIdea(libs.versions.platform.get())
        testFramework(TestFrameworkType.Platform)
    }
}

// The lexer (JFlex) and parser/PSI (Grammar-Kit) are generated from the committed .flex/.bnf
// grammar sources into the build directory. Generated sources are never committed.
private val generatedRoot = layout.buildDirectory.dir("generated/sources/grammarkit")

val generateObjectPascalLexer by tasks.registering(GenerateLexerTask::class) {
    sourceFile = file("src/main/grammar/ObjectPascal.flex")
    targetOutputDir = generatedRoot.map { it.dir("org/pcsoft/passat/language/parser") }
    purgeOldFiles = true
}

val generateObjectPascalParser by tasks.registering(GenerateParserTask::class) {
    sourceFile = file("src/main/grammar/ObjectPascal.bnf")
    targetRootOutputDir = generatedRoot
    pathToParser = "org/pcsoft/passat/language/parser/ObjectPascalParser.java"
    pathToPsiRoot = "org/pcsoft/passat/language/parser/psi"
    purgeOldFiles = true
}

sourceSets {
    main {
        java.srcDir(generatedRoot)
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(generateObjectPascalLexer, generateObjectPascalParser)
    }
    withType<JavaCompile> {
        dependsOn(generateObjectPascalLexer, generateObjectPascalParser)
    }
}
