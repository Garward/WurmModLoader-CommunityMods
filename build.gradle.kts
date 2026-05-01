plugins {
    id("java-library")
    id("maven-publish")
}

// Resolve the Wurm Unlimited install directories once, here. Subprojects pick
// them up via `val wurmServerDir: String by rootProject.extra` (and the
// client equivalent). Set both in ~/.gradle/gradle.properties or via the
// WURM_SERVER_DIR / WURM_CLIENT_DIR env vars. See gradle.properties.example.
//
// Client dir is only required when building client mods under client-mods/,
// so missing it is lazy-errored from the mod's own build.gradle.kts.
val wurmServerDir: String by extra(
    (project.findProperty("wurmServerDir") as String?)
        ?: System.getenv("WURM_SERVER_DIR")
        ?: error(
            "Wurm Unlimited server directory not set. Add `wurmServerDir=/path/to/Wurm Unlimited Dedicated Server` " +
            "to ~/.gradle/gradle.properties, or set the WURM_SERVER_DIR environment variable. " +
            "See gradle.properties.example."
        )
)

val wurmClientDir: String by extra(
    (project.findProperty("wurmClientDir") as String?)
        ?: System.getenv("WURM_CLIENT_DIR")
        ?: ""
)

allprojects {
    group = "com.garward.wurmmodloader"
    version = "0.10.0"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://gotti.no-ip.org/maven/repository")
            name = "WurmUnlimited"
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        // Target Java 8 bytecode for compatibility with Wurm server
        // But use Java 17 toolchain for building (configured above)
        options.release.set(8)
    }

    dependencies {
        // Server framework JARs auto-injected for server mods only.
        // Client mods under client-mods/ wire up their own client framework JARs.
        if (!project.path.startsWith(":client-mods:")) {
            implementation(files("${rootProject.projectDir}/libs/wurmmodloader-api-0.10.1.jar"))
            implementation(files("${rootProject.projectDir}/libs/wurmmodloader-core-0.10.1.jar"))
            implementation(files("${rootProject.projectDir}/libs/wurmmodloader-modsupport-0.10.1.jar"))
            implementation(files("${rootProject.projectDir}/libs/wurmmodloader-legacy-0.10.1.jar"))
        }

        // Common test dependencies - JUnit 4 for legacy tests
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.assertj:assertj-core:3.24.2")

        // Also include JUnit 5 for future tests
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }

    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).apply {
                addBooleanOption("html5", true)
                addStringOption("Xdoclint:none", "-quiet")
                encoding = "UTF-8"
                docEncoding = "UTF-8"
                charSet = "UTF-8"

                links(
                    "https://docs.oracle.com/en/java/javase/17/docs/api/"
                )
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set("WurmModLoader ${project.name}")
                    description.set("Modern modding framework for Wurm Unlimited")
                    url.set("https://github.com/garward/WurmModLoader")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("garward")
                            name.set("Garward")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/garward/WurmModLoader.git")
                        developerConnection.set("scm:git:ssh://github.com:garward/WurmModLoader.git")
                        url.set("https://github.com/garward/WurmModLoader")
                    }
                }
            }
        }
    }
}

// Root project tasks
tasks.register("cleanAll") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
    description = "Clean all subprojects"
    group = "build"
}

tasks.register("buildAll") {
    dependsOn(subprojects.map { it.tasks.named("build") })
    description = "Build all subprojects"
    group = "build"
}

tasks.register("testAll") {
    dependsOn(subprojects.map { it.tasks.named("test") })
    description = "Run tests in all subprojects"
    group = "verification"
}

