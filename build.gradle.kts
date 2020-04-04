import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.grammarkit.tasks.GenerateParser
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val generatedSourceDir = "gen"


plugins {
    id("idea")
    id("org.jetbrains.intellij") version "0.4.16"
    id("org.jetbrains.grammarkit") version "2020.1.2"
    java
    kotlin("jvm") version "1.3.70"
}

group = "kuruvila"
version = "0.1.0"

repositories {
    mavenCentral()
    //maven { url "http://dl.bintray.com/jetbrains/intellij-plugin-service" }
}

intellij {
    version = property("ideaVersion").toString()
    setPlugins("java", "coverage")
    pluginName = "ocaml-idea"
    updateSinceUntilBuild = false
}

idea {
    project {
        jdkName = "8"
        languageLevel = IdeaLanguageLevel("8")
    }
    module {
        generatedSourceDirs = mutableSetOf(file(generatedSourceDir))
    }
}

sourceSets {
    main {
        java.srcDirs(generatedSourceDir)
    }
}

tasks {
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    compileTestJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")

    testCompile("junit:junit:4.12")
}

task<GenerateLexer>("generateOcamlLexer") {
    source = "parser/OcamlLexer.flex"
    targetDir = "$generatedSourceDir/org/ocaml/lang/lexer/"
    targetClass = "_OcamlLexer"
    purgeOldFiles = true
}

task<GenerateLexer>("generateSexpLexer") {
    source = "parser/sexp/SexpLexer.flex"
    targetDir = "$generatedSourceDir/org/ocaml/sexp/lexer/"
    targetClass = "_SexpLexer"
    purgeOldFiles = true
}

task<GenerateParser>("generateOcamlPsiAndParser") {
    source = "parser/ocaml.bnf"
    targetRoot = generatedSourceDir
    pathToPsiRoot = "org/ocaml/lang/parser/psi"
    pathToParser = "org/ocaml/lang/parser/OcamlParser.java"
    purgeOldFiles = true
}

task<GenerateParser>("generateSexpPsiAndParser") {
    source = "parser/sexp/sexp.bnf"
    targetRoot = generatedSourceDir
    pathToPsiRoot = "org/ocaml/sexp/parser/psi"
    pathToParser = "org/ocaml/sexp/parser/SexpParser.java"
    purgeOldFiles = true
}

task<GenerateParser>("generateJbuildPsiAndParser") {
    source = "parser/jbuild/jbuild.bnf"
    targetRoot = generatedSourceDir
    pathToPsiRoot = "org/ocaml/jbuild/parser/psi"
    pathToParser = "org/ocaml/jbuild/parser/JbuildParser.java"
    purgeOldFiles = true
}

tasks.withType<KotlinCompile> {
    dependsOn(
        "generateOcamlLexer",
        "generateOcamlPsiAndParser",
        "generateSexpLexer",
        "generateSexpPsiAndParser",
        "generateJbuildPsiAndParser"
    )
}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.getByName("clean") {
    doLast {
        delete(generatedSourceDir)
    }
}

tasks.getByName<PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
        Add change notes here.<br>
        <em>most HTML tags may be used</em>"""
    )
}
