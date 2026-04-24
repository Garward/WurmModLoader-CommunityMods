plugins {
    java
}

group = "com.garward.wurmmods"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // WurmModLoader API

    // Wurm server JARs
    val wurmServerDir: String by rootProject.extra
    compileOnly(files("$wurmServerDir/server.jar", "$wurmServerDir/common.jar"))

    // Javassist
    compileOnly("org.javassist:javassist:3.23.1-GA")
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
    archiveBaseName.set("testmod")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/mods/testmod"))
}

// Create mod distribution (JAR + properties/config files)
tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")
    from("src/dist")
    into("${projectDir}/dist/mods/testmod")
}

tasks.named("build") {
    dependsOn("modDistribution")
}

tasks.register<Copy>("deployMod") {
    dependsOn("modDistribution")
    val wurmServerDir: String by rootProject.extra
    from(layout.projectDirectory.dir("dist/mods/testmod"))
    into("$wurmServerDir/mods/testmod")
}
