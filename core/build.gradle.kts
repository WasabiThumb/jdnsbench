
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

tasks.processResources {
    val natives = project(":natives")
    dependsOn(natives.tasks.build)

    outputs.upToDateWhen { false }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    doFirst {
        val cmakeDir = natives.layout.buildDirectory.file("cmake").get().asFile
        cmakeDir.listFiles { file ->
            file.extension in listOf("so", "dll")
        }?.forEach { file ->
            from(file.absolutePath) to file.name
        }
    }
}
