plugins {
    java
}

group = "com.garward.wurmmods"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files(
        "../../libs/wurmmodloader-api-0.10.1.jar",
        "../../libs/wurmmodloader-core-0.10.1.jar",
        "../../libs/wurmmodloader-modsupport-0.10.1.jar"
    ))

    val wurmServerDir: String by rootProject.extra
    compileOnly(files("$wurmServerDir/server.jar", "$wurmServerDir/common.jar"))

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
    archiveBaseName.set("scriptrunner")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/mods/scriptrunner"))
}

// Create mod distribution (JAR + properties/config files)
tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")
    from("src/dist")
    into("${projectDir}/dist/mods/scriptrunner")
}

tasks.named("build") {
    dependsOn("modDistribution")
}

tasks.register<Copy>("deployMod") {
    dependsOn("modDistribution")
    val wurmServerDir: String by rootProject.extra
    from(layout.projectDirectory.dir("dist/mods/scriptrunner"))
    into("$wurmServerDir/mods/scriptrunner")
}
