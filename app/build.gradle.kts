@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


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

    // YAML - Jackson
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.1"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-properties")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")

    // Apache Commons Codecs compile group: 'commons-codec', name: 'commons-codec', version: '1.15'
    val apacheCommonsCodevVersion = "1.15"
    implementation("commons-codec:commons-codec:$apacheCommonsCodevVersion")

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
    kotlinOptions {
        jvmTarget = this@Build_gradle.jvmTarget
    }
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
