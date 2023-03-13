/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("daily_planner.kotlin-application-conventions")
    kotlin("jvm") version "1.7.10"
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    //implementation(project(":stubs"))
    // This dependency is used by the application.
    listOf(
        "armeria",
        "armeria-brave",
        "armeria-grpc",
        "armeria-jetty9",
        "armeria-kafka",
        "armeria-logback",
        "armeria-retrofit2",
        "armeria-rxjava3",
        "armeria-zookeeper3").forEach { implementation("com.linecorp.armeria:${it}:1.22.0") }

    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.inject:guice:5.1.0")
    api(project( ":stubs"))

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.36")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-11")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.linecorp.armeria:armeria-kotlin:1.22.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(15))
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("1.7.10")

            dependencies {
                // Use newer version of JUnit Engine for Kotlin Test
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

application {
    // Define the main class for the application.
    mainClass.set("daily_planner.app.AppKt")
}
