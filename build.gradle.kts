import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "io.github.amerebagatelle"
version = "1.1.2"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.4-SNAPSHOT")

    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin:0.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("io.github.brennenputh.campbotkotlin.MainKt")
}