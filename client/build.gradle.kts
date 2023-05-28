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

    listOf(
        "armeria",
        "armeria-grpc",
        "armeria-jetty9",
        "armeria-kafka",
        "armeria-kotlin",
        "armeria-logback",
        "armeria-retrofit2",
        "armeria-zookeeper3").forEach { implementation("com.linecorp.armeria:${it}:1.22.0") }

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.5")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-11")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.6")
    runtimeOnly("org.slf4j:log4j-over-slf4j:2.0.5")
    implementation("com.google.inject:guice:5.1.0")

    //date
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.15.1")
    //implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.5")


}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(15))
}
repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

application {
    // Define the main class for the application.
    mainClass.set("daily_planner.client.ClientKt")
}
