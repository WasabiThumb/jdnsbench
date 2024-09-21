import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("io.github.goooler.shadow") version "8.1.8"
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.jline:jline:3.26.3")
}

tasks.compileJava {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes(mapOf(
            "Main-Class" to "io.github.wasabithumb.jdnsbench.Main",
            "App-Version" to rootProject.version.toString(),
            "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
            "Build-JDK" to System.getProperties()["java.version"],
            "Wasabi" to "Was-Here"
        ))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
