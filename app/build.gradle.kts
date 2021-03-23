@file:Suppress("SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "6.1.0"
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
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
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

    // ID Generator (used internally to geneate opaque keys by the validation framework)
    implementation("org.hashids:hashids:1.0.3")

    // System lamdba to for testing
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.0")

    // String tamplating engine
    // implementation("com.jaliansystems:simple-template:1.1")
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


tasks.withType<ProcessResources> {

    val resourceReplacements = mapOf(
        "application.version" to project.version.toString(),
        "application.build.arch" to "${System.getProperty("os.name")}-${System.getProperty("os.arch")}",
        "application.build.date" to LocalDateTime.now().toLocalDate().toString()
    )

    fun replacment(key: String, variable:String): String {

        val replacement = when {
            project.hasProperty(variable) -> project.property(variable).toString()
            else -> resourceReplacements[variable]
        }

        return "$key=$replacement"
    }

    filter { property ->

        val (key,value) = when (val position = property.indexOf('=')) {
            -1 -> property to null
            else -> property.substring(0, position) to property.substring(position + 1)
        }

        when {
            value == null -> property
            value.startsWith("\${") and value.endsWith("}") -> {
                val variable = value.substring(2, value.length - 1)
                replacment(key, variable) ?: property
            }
            else -> property
        }
    }
}


tasks.withType<ShadowJar> {
    project.setProperty("mainClassName", secToolAppMain) // Need this to work arround groovy!
    archiveVersion.set("")
    archiveClassifier.set("app")

    mergeServiceFiles()
}

fun MavenPublication.configurePublication() {
    artifactId = "sec"
    pom {
        name.set("sectool")
        description.set("Security companion to the Palantir Configuration Values Library")
        licenses {
            license {
                name.set("Apache License Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0")
                description.set("Apache License Version 2.0, January 2004")
            }
        }
        scm {
            developerConnection.set("git@github.com:DarkGrayMatter/sec.git")
            connection.set("https://github.com/DarkGrayMatter/sec.git")
        }
        issueManagement {
            url.set("https://github.com/DarkGrayMatter/sec/issues")
        }
        developers {
            developers {
                organization {
                    name.set("DarkGrayMatter")
                    url.set("https://github.com/DarkGrayMatter")
                    contributors {
                        contributor {
                            name.set("andriesfc")
                            email.set("andriesfc@gmail.com")
                            roles.addAll("developer", "project admin")
                        }
                    }
                }
            }
        }
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

publishing {
    repositories {
        maven {
            name = "Gpr"
            url = uri("https://maven.pkg.github.com/DarkGrayMatter/sec")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GRP_TOKEN")
            }
        }
        maven {
            name = "Project"
            url = rootProject.file("repo").toURI()
        }
    }
    publications {
        create<MavenPublication>("sec") {
            from(components["java"])
            configurePublication()
        }
    }
}

tasks.create("dropProjectRepo") {
    description = "Cleans out the local project repostory"
    group = "Project"
    doLast {
        with(rootProject.file("repo/graymatter")) {
            if (exists()) {
                println("Deleting $this")
                deleteRecursively()
            }
        }
    }
}
