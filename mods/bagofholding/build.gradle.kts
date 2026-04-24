plugins {
    java
}

group = "com.garward.wurmmodloader.mods"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // WurmModLoader API - for event system (future)

    // WurmModLoader modsupport - for ModActions

    // WurmModLoader legacy - for HookManager reflection hooks

    // Legacy interfaces for WurmServerMod
    compileOnly("org.gotti.wurmunlimited:modloader-shared:0.18")

    // Wurm server dependencies (provided at runtime) — resolved via rootProject `wurmServerDir`.
    val wurmServerDir: String by rootProject.extra
    compileOnly(files("$wurmServerDir/server.jar", "$wurmServerDir/common.jar"))

    // Testing
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.assertj:assertj-core:3.8.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}

// Create distribution-ready JAR in subfolder (LOCAL build directory)
tasks.register<Jar>("modJar") {
    from(sourceSets.main.get().output)
    archiveBaseName.set("bagofholding")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/bagofholding"))

    manifest {
        attributes(
            "Implementation-Title" to "Bag of Holding Mod",
            "Implementation-Version" to project.version,
            "Built-By" to "WurmModLoader"
        )
    }
}

// Create mod distribution (JAR in subfolder + properties/config in parent)
tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")

    // Copy properties and config files to local dist/ root
    from("src/dist/bagofholding.properties")
    from("src/dist/bagofholding.config")
    into("${projectDir}/dist")

    // Note: JAR is already in dist/bagofholding/ from modJar task
}

tasks.named("build") {
    dependsOn("modDistribution")
}
