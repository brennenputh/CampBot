import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

group = "io.github.amerebagatelle"
version = "1.1.2"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.4")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin:0.9.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.brennenputh.campbotkotlin.MainKt")
}