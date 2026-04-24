plugins {
    java
}

group = "com.garward.wurmmods"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // WurmModLoader framework dependencies
    compileOnly(files(
        "../../libs/wurmmodloader-api-0.10.0.jar",
        "../../libs/wurmmodloader-legacy-0.10.0.jar"
    ))

    val wurmServerDir: String by rootProject.extra
    compileOnly(files(
        "$wurmServerDir/server.jar",
        "$wurmServerDir/common.jar"
    ))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}

// Create distribution-ready JAR in subfolder
tasks.register<Jar>("modJar") {
    from(sourceSets.main.get().output)
    archiveBaseName.set("serverpacks")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/serverpacks"))
}

// Create mod distribution (JAR + properties/config files)
tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")

    // Copy properties and config files to local dist/ root
    from("src/dist") {
        include("*.properties", "*.config")
    }
    into("${projectDir}/dist")
}

tasks.named("build") {
    dependsOn("modDistribution")
}
