import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
}

group = "me.ele"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    testImplementation(kotlin("test-junit"))

    implementation(
        "net.openhft:zero-allocation-hashing:0.9"
    )
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}