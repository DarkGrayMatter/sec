@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.IOException
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*


plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("kapt") version "1.4.30"
    id("org.jetbrains.dokka") version "1.4.20"
    application
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))

    // Need full reflection
    implementation(kotlin("reflect"))

    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit5"))

    // Command Line support
    implementation("info.picocli:picocli:4.6.1")
    kapt("info.picocli:picocli-codegen:4.6.1")

    // palantir
    implementation("com.palantir.config.crypto:encrypted-config-value:2.1.0") {
        exclude(group = "com.fasterxml.jackson.core")
    }

    // JUnit5
    val junitVersion = "5.7.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // Jackson Data Formats
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.1"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-properties")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")

    // Apache Commons 
    val apacheCommonsCodecVersion = "1.15"
    implementation("commons-codec:commons-codec:$apacheCommonsCodecVersion")

    // Ant Style Path Matcher
    implementation("io.github.azagniotov:ant-style-path-matcher:1.0.0")

}

val secToolAppMain = "graymatter.sec.App"

application {
    mainClass.set(secToolAppMain)
}

tasks.withType<CreateStartScripts> {
    applicationName = "sec"
}

val jvmTarget = JavaVersion.VERSION_1_8.toString()

tasks.withType<JavaCompile> {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}



tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = jvmTarget
}


tasks.withType<Jar> { archiveBaseName.set("sec") }

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
    }
}


java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.jetbrains.space/graymatter/p/sec/maven")
            credentials.username = project.property("graymatter_spaces_username") as String
            credentials.password = project.property("graymatter_spaces_password") as String
        }
    }
}

tasks.create("generateToolBuildInfo") {
    description = "Generates tool version file for command line inpsection."
    group = "Build"
    doLast {
        val version = Properties().run {
            put("version", "${project.version}")
            put("build.ts", "${LocalDateTime.now()}")
            put("build.platform.os.name", System.getProperty("os.name"))
            put("build.platform.os.version", System.getProperty("os.version"))
            put("build.platform.os.arch", System.getProperty("os.arch"))
            StringWriter().also { store(it, "SEC tool version file.") }.toString()
        }
        val versionFile = file("/build/resources/main/graymatter/sec/version.properties").apply {
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw IOException("Failed to create directory: $parent")
            }
        }
        versionFile.writeText(version)
    }
}

tasks.named("processResources") { dependsOn("generateToolBuildInfo") }
