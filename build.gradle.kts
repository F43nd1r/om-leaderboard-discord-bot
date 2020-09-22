import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.0"
    id("com.palantir.docker") version "0.25.0"
    id("io.freefair.lombok") version "5.2.1"
}

group = "com.faendir.zachtronics.bot"
version = "1.1-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://dl.bintray.com/f43nd1r/maven") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("net.dv8tion:JDA:4.2.0_198")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.8.1.202007141445-r")
    implementation("com.github.rockswang:java-curl:1.2.2.2")
    implementation("com.faendir.jraw:JRAW:1.2.0")
    implementation("com.marcinmoskala:DiscreteMathToolkit:1.0.3")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.sf.trove4j:trove4j:3.0.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjvm-default=enable"
    }
}

tasks.getByName<BootJar>("bootJar") {
    layered()
}

docker {
    name = "f43nd1r/om-leaderboard-discord-bot:latest"
    files(tasks.getByName<BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}
