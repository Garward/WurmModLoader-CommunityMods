group = "com.garward.mods"
version = "0.1.0"

val wurmClientDir: String by rootProject.extra
if (wurmClientDir.isEmpty()) {
    error(
        "Wurm Unlimited client directory not set. Add `wurmClientDir=/path/to/Wurm Unlimited/WurmLauncher` " +
        "to ~/.gradle/gradle.properties, or set the WURM_CLIENT_DIR environment variable. " +
        "See gradle.properties.example."
    )
}

dependencies {
    compileOnly(files("${rootProject.projectDir}/libs/wurmmodloader-client-api-0.4.0.jar"))
    compileOnly(files("${rootProject.projectDir}/libs/wurmmodloader-client-core-0.4.0.jar"))

    compileOnly(files("$wurmClientDir/client.jar"))
    compileOnly(files("$wurmClientDir/common.jar"))
}

tasks.jar {
    archiveBaseName.set("action")
    archiveVersion.set("")

    manifest {
        attributes(
            "Implementation-Title" to "Custom Action Macros Client Mod",
            "Implementation-Version" to project.version
        )
    }
}

tasks.register<Copy>("modDistribution") {
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile) {
        into("mods/action")
    }
    from("src/dist") {
        into("mods/action")
    }
    into("${projectDir}/dist")
}

tasks.build {
    dependsOn(tasks.named("modDistribution"))
}

tasks.register<Copy>("deployMod") {
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile)
    from("src/dist")
    into("$wurmClientDir/mods/action")
}
