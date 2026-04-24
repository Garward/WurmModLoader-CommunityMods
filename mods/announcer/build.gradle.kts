plugins {
    java
}

group = "com.garward.wurmmods"
version = "1.0.0"

repositories {
    mavenCentral()
}

val wurmServerDir: String by rootProject.extra

dependencies {
    // Wurm server JARs (path resolved by the root build.gradle.kts)
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
    archiveBaseName.set("announcer")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/mods/announcer"))
}

// Create mod distribution (JAR + properties/config files)
tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")
    from("src/dist")
    into("${projectDir}/dist/mods/announcer")
}

tasks.named("build") {
    dependsOn("modDistribution")
}

tasks.register<Copy>("deployMod") {
    dependsOn("modDistribution")
    val wurmServerDir: String by rootProject.extra
    from(layout.projectDirectory.dir("dist/mods/announcer"))
    into("$wurmServerDir/mods/announcer")
}
