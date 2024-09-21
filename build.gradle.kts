
allprojects {
    group = "io.github.wasabithumb"
    version = "1.0.0"
}

subprojects {
    tasks.withType(Jar::class.java) {
        archiveBaseName.set("${rootProject.name}-${project.name}")
    }
}
