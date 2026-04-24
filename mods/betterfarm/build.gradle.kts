plugins {
    java
}

group = "com.garward.wurmmods"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
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

tasks.register<Jar>("modJar") {
    from(sourceSets.main.get().output)
    archiveBaseName.set("betterfarm")
    archiveVersion.set("")
    destinationDirectory.set(file("${projectDir}/dist/mods/betterfarm"))
}

tasks.register<Copy>("modDistribution") {
    dependsOn("modJar")
    from("src/dist")
    into("${projectDir}/dist/mods/betterfarm")
}

tasks.named("build") {
    dependsOn("modDistribution")
}

tasks.register<Copy>("deployMod") {
    dependsOn("modDistribution")
    val wurmServerDir: String by rootProject.extra
    from(layout.projectDirectory.dir("dist/mods/betterfarm"))
    into("$wurmServerDir/mods/betterfarm")
}
