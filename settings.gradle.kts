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

// declarativeui folded into the framework as a built-in service
// (com.garward.wurmmodloader.modsupport.declarativeui.DeclarativeUiService).
// No mod jar is needed; mods just post `ui:open_window` ModActionEvents.

include("mods:spellmod")
project(":mods:spellmod").projectDir = file("mods/spellmod")

include("mods:spellcraft")
project(":mods:spellcraft").projectDir = file("mods/spellcraft")

include("mods:testmod")
project(":mods:testmod").projectDir = file("mods/testmod")

// WyvernMods split (each upstream submod becomes its own jar — see
// memory: project_wyvernmods_port.md). Add as each port lands.
include("mods:qualityoflife")
project(":mods:qualityoflife").projectDir = file("mods/qualityoflife")

include("mods:miscchanges")
project(":mods:miscchanges").projectDir = file("mods/miscchanges")

include("mods:anticheat")
project(":mods:anticheat").projectDir = file("mods/anticheat")

include("mods:teleport")
project(":mods:teleport").projectDir = file("mods/teleport")

include("mods:skill")
project(":mods:skill").projectDir = file("mods/skill")

include("mods:achievementchanges")
project(":mods:achievementchanges").projectDir = file("mods/achievementchanges")

include("mods:customdeity")
project(":mods:customdeity").projectDir = file("mods/customdeity")

include("mods:meditation")
project(":mods:meditation").projectDir = file("mods/meditation")

include("mods:economy")
project(":mods:economy").projectDir = file("mods/economy")

include("mods:mounted")
project(":mods:mounted").projectDir = file("mods/mounted")

include("mods:customtitles")
project(":mods:customtitles").projectDir = file("mods/customtitles")

include("mods:extraactions")
project(":mods:extraactions").projectDir = file("mods/extraactions")

include("mods:erosion")
project(":mods:erosion").projectDir = file("mods/erosion")

include("mods:bestiary")
project(":mods:bestiary").projectDir = file("mods/bestiary")

include("mods:mission")
project(":mods:mission").projectDir = file("mods/mission")

include("mods:bounty")
project(":mods:bounty").projectDir = file("mods/bounty")

include("mods:treasurechest")
project(":mods:treasurechest").projectDir = file("mods/treasurechest")

include("mods:caches")
project(":mods:caches").projectDir = file("mods/caches")

include("mods:mastercraft")
project(":mods:mastercraft").projectDir = file("mods/mastercraft")

include("mods:soulstealing")
project(":mods:soulstealing").projectDir = file("mods/soulstealing")

include("mods:crystals")
project(":mods:crystals").projectDir = file("mods/crystals")

include("mods:titan")
project(":mods:titan").projectDir = file("mods/titan")

include("mods:rarespawn")
project(":mods:rarespawn").projectDir = file("mods/rarespawn")

include("mods:arena")
project(":mods:arena").projectDir = file("mods/arena")

include("mods:supplydepot")
project(":mods:supplydepot").projectDir = file("mods/supplydepot")

include("mods:keyevent")
project(":mods:keyevent").projectDir = file("mods/keyevent")

include("mods:gemaugmentation")
project(":mods:gemaugmentation").projectDir = file("mods/gemaugmentation")

include("mods:wyverncombat")
project(":mods:wyverncombat").projectDir = file("mods/wyverncombat")

// Vanilla item-template tweaks (upstream ItemMod.modifyItems +
// onServerStarted) live in mods/miscchanges. Custom items from WyvernMods
// are owned by their submods (caches, crystals, soulstealing, combat
// residuals); see docs/research/wyvernmods-item-gaps.md for the gap analysis.

// Client-side mods (require wurmClientDir).
include("client-mods:action")
project(":client-mods:action").projectDir = file("client-mods/action")

include("client-mods:automine")
project(":client-mods:automine").projectDir = file("client-mods/automine")

include("client-mods:compass")
project(":client-mods:compass").projectDir = file("client-mods/compass")

include("client-mods:wurmesp")
project(":client-mods:wurmesp").projectDir = file("client-mods/wurmesp")

include("client-mods:tooltips")
project(":client-mods:tooltips").projectDir = file("client-mods/tooltips")
