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

sourceSets {
    main {
        resources {
            srcDir(".")
            include("compass.properties")
        }
    }
}

dependencies {
    compileOnly(files("${rootProject.projectDir}/libs/wurmmodloader-client-api-0.3.0.jar"))

    compileOnly(files("$wurmClientDir/client.jar"))
    compileOnly(files("$wurmClientDir/common.jar"))
}

tasks.jar {
    archiveBaseName.set("compass")
    archiveVersion.set("")

    manifest {
        attributes(
            "Implementation-Title" to "Compass Client Mod",
            "Implementation-Version" to project.version
        )
    }
}

tasks.register<Copy>("modDistribution") {
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile) {
        into("mods/compass")
    }
    from("compass.properties") {
        into("mods")
    }
    into("${projectDir}/dist")
}

tasks.build {
    dependsOn(tasks.named("modDistribution"))
}

tasks.register<Copy>("deployMod") {
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile) {
        into("compass")
    }
    from("compass.properties")
    into("$wurmClientDir/mods")
}
