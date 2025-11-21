plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "io.github.saidooubella"
version = "0.0.1-dev"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvmToolchain(11)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
    }
}

application {
    mainClass = "io.github.saidooubella.sash.playground.MainKt"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar { manifest { attributes["Main-Class"] = application.mainClass } }

tasks.distZip { archiveFileName = "sash.zip" }

tasks.startScripts { enabled = false }

tasks.classes { enabled = false }

tasks.distTar { enabled = false }

tasks.jar { enabled = false }
