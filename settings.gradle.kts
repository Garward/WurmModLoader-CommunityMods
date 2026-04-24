rootProject.name = "wurmmodloader-communitymods"

// --- Local property overrides (gitignored) ---
// `gradle.properties.local` at this directory carries personal paths like
// wurmServerDir / wurmClientDir. Loaded into Gradle project properties so the
// build sees them just like committed gradle.properties entries.
val localProps = file("gradle.properties.local")
if (localProps.exists()) {
    val props = java.util.Properties()
    localProps.inputStream().use { props.load(it) }
    gradle.beforeProject {
        props.forEach { k, v -> extra[k.toString()] = v.toString() }
    }
}


// Framework modules are JAR dependencies in libs/ — drop the
// wurmmodloader-*.jar files there from a built WurmModLoader release.

// Power Fantasy RPG mods
include("mods:powerscaling")
project(":mods:powerscaling").projectDir = file("mods/powerscaling")

include("mods:armoury")
project(":mods:armoury").projectDir = file("mods/armoury")

include("mods:DUSKombat")
project(":mods:DUSKombat").projectDir = file("mods/DUSKombat")

include("mods:bagofholding")
project(":mods:bagofholding").projectDir = file("mods/bagofholding")

include("mods:betterdig")
project(":mods:betterdig").projectDir = file("mods/betterdig")

include("mods:betterfarm")
project(":mods:betterfarm").projectDir = file("mods/betterfarm")

// adminterrain, eventlister, materialsystem, soulboundgear, timerfix, upgradetree
// removed — empty stubs with no implementation.

// Ago's legacy mods (to be modernized)
include("mods:announcer")
project(":mods:announcer").projectDir = file("mods/announcer")

include("mods:creatureagemod")
project(":mods:creatureagemod").projectDir = file("mods/creatureagemod")

include("mods:cropmod")
project(":mods:cropmod").projectDir = file("mods/cropmod")

include("mods:harvesthelper")
project(":mods:harvesthelper").projectDir = file("mods/harvesthelper")

// httpserver has been promoted into the framework (wurmmodloader-core).
// See com.garward.wurmmodloader.httpserver.HttpServerSubsystem.

include("mods:inbreedwarning")
project(":mods:inbreedwarning").projectDir = file("mods/inbreedwarning")

// livemap has been promoted into the framework repo
// (WurmModLoader/mods/livemap) — Garward-maintained, not a community port.

include("mods:scriptrunner")
project(":mods:scriptrunner").projectDir = file("mods/scriptrunner")

// servermap removed — superseded by livemap (live HTTP tiles vs one-shot PNG).

include("mods:serverpacks")
project(":mods:serverpacks").projectDir = file("mods/serverpacks")

include("mods:spellmod")
project(":mods:spellmod").projectDir = file("mods/spellmod")

include("mods:spellcraft")
project(":mods:spellcraft").projectDir = file("mods/spellcraft")

include("mods:testmod")
project(":mods:testmod").projectDir = file("mods/testmod")

// Client-side mods (require wurmClientDir).
include("client-mods:action")
project(":client-mods:action").projectDir = file("client-mods/action")

include("client-mods:compass")
project(":client-mods:compass").projectDir = file("client-mods/compass")

include("client-mods:wurmesp")
project(":client-mods:wurmesp").projectDir = file("client-mods/wurmesp")
