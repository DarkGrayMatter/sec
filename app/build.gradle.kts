import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    application
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Command Line support
    implementation("info.picocli:picocli:4.6.1")
    kapt("info.picocli:picocli-codegen:4.6.1")

    // palantir
    implementation("com.palantir.config.crypto:encrypted-config-value:2.1.0")
}

application {
    mainClass.set("me.andriefc.secj.AppKt")
}

tasks.withType<CreateStartScripts> {
    applicationName = "secj"
}

val javacTarget = JavaVersion.VERSION_1_8.toString()

tasks.withType<JavaCompile> {
    sourceCompatibility = javacTarget
    targetCompatibility = javacTarget
}


tasks.withType<Jar> {
    archiveBaseName.set("secj")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javacTarget
    }
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}
