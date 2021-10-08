import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version ("6.0.0")
}

group = "io.github.amerebagatelle"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
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

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    manifest {
        attributes["Main-Class"] = "io.github.amerebagatelle.campbotkotlin.MainKt"
    }
}