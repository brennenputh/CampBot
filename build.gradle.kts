import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.6.10"
    java
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "io.github.amerebagatelle"
version = "1.1.1"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.0-SNAPSHOT")

    implementation("org.slf4j:slf4j-simple:1.7.32")

    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("com.beust:klaxon:5.5")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin:0.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("io.github.amerebagatelle.campbotkotlin.MainKt")
}