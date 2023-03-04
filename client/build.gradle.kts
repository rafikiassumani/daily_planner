/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("daily_planner.kotlin-library-conventions")
    kotlin("jvm") version "1.7.10"
    application
}

dependencies {
    api(project(":stubs"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-11")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.36")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(15))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

application {
    // Define the main class for the application.
    mainClass.set("daily_planner.client.ClientKt")
}
