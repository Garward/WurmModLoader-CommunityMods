# WyvernMods Port — Entry Convention (TEMPORARY)

> **DELETE THIS FILE WHEN THE WYVERNMODS PORT IS COMPLETE.**
> Live reference: `mods/qualityoflife/` (the canonical template).
> Locked rules: see memory `project_wyvernmods_port.md`.

---

## Per-submod file checklist

Each ported WyvernMods submod ships as its own gradle module:

```
mods/<feature>/
├── build.gradle.kts
├── README.md
└── src/
    ├── main/java/com/garward/wurmmodloader/mods/<feature>/<Feature>Mod.java
    └── dist/mod.properties
```

Then register it in root `settings.gradle.kts`:

```kotlin
include("mods:<feature>")
project(":mods:<feature>").projectDir = file("mods/<feature>")
```

## Naming convention

- Folder name = lowercase feature name from the locked table (no `wyvern` prefix).
- Java package: `com.garward.wurmmodloader.mods.<feature>` (no extra subpackages
  unless the submod is genuinely large).
- Entry class: `<Feature>Mod` (PascalCase).
- `mod.properties` `classname` field points at the entry class FQN.
- Disambiguations already locked: `customactions` (not `actions`),
  `bestiary` (not `creatures`), `customitems` (not `items`),
  `keyofheavens` (not `keys`).

## build.gradle.kts skeleton

Copy from `mods/qualityoflife/build.gradle.kts`. Substitute `qualityoflife`
with the new feature name (5 occurrences: `archiveBaseName`, two
`destinationDirectory`/`into` paths under `dist/mods/`, one `from` for
`deployMod`, one `into` under `$wurmServerDir/mods/`).

Framework jars are at version `0.10.1` and ship in `libs/`; do not bump
without coordinating with the framework repo.

## Entry class skeleton

Copy from `QualityOfLifeMod.java`. Pattern:

- Implements `WurmServerMod` + `Configurable` + (one of `PreInitable` /
  `Initable` / `ServerStartedListener`) from
  `com.garward.wurmmodloader.modloader.interfaces.*`.
- All sub-toggles as `boolean` fields with sensible defaults.
- `configure(Properties)` reads each toggle via the local `bool(...)` helper.
- Lifecycle method does the actual wiring (bytecode patches, event
  subscriptions, etc.). Skeleton just logs `"loaded skeleton — behaviors
  not yet ported"` until the port lands.

## mod.properties skeleton

```
classname=com.garward.wurmmodloader.mods.<feature>.<Feature>Mod
sharedClassLoader=true

# <inline comment per toggle, preserved/translated from upstream>
<toggleName>=<default>
```

Toggle names should mirror upstream `WyvernMods.properties` so existing
operators can copy values 1:1.

## README.md required sections

1. **`# <feature>`** — one-line tagline.
2. **What it does** — bullet per behavior (matches the toggles in
   `mod.properties`); each line is at most a sentence or two.
3. **Soft dependencies** — note any other community-mod toggle this submod
   relies on (e.g. `qualityoflife`'s `*ToVehicle` toggles need
   `fatigueActionOverride` from `miscchanges`). Use events when feasible.
4. **Provenance** — name the upstream module/file you ported from, e.g.
   `Ported from the enableQualityOfLifeModule block of Sindusk's WyvernMods
   (mod.sin.wyvern.QualityOfLife). Property names preserved one-for-one.`

Don't include code samples or install instructions in per-mod READMEs; the
top-level repo README covers that.

## Build & verify

```bash
./gradlew :mods:<feature>:build
ls mods/<feature>/dist/mods/<feature>/
# expected: <feature>.jar  mod.properties
```

The `dist/mods/<feature>/` layout is the deploy artifact — the entire
distribution must live in that single subfolder (no loose files at the
mod root).

## Inter-mod coupling

Per locked rule #3: prefer events over imports/depends. If a submod *can*
react to another submod's behavior via the framework event bus or a
generic `ModActionEvent` channel, do that instead of a hard dependency.
Hard deps require a memory entry justifying them.

## Replacements for `mod.sin.lib.*`

Upstream WyvernMods leans on Sindusk's helper jar. We don't ship that jar.
Each port substitutes:

| Upstream symbol | Replacement |
| --- | --- |
| `Util.instrumentDeclared / instrumentDescribed / insertBefore* / insertAfter* / setBody*` and the `*Count` variants (~310 calls) | A proper framework patch class under `core/bytecode/patches/` (or per-mod patch registered via the framework patch SPI). Drop the success-string sugar — framework patches already log per-class. |
| `Util.setReason(String)` (~195 calls) | Delete. It only set a log suffix for the next Util call; framework patch logs already identify the patch by class name. |
| `Util.getField / setPrivateField / getPrivateField / getMethod / callPrivateMethod` (~140 calls) | `com.garward.wurmmodloader.modloader.ReflectionUtil` (public in api jar — see memory `feedback_public_modloader_api.md`). |
| `Util.applyEnchant / createEnchantOrb / sorceryIds / isSorcery / createRandomSorcery / isSingleUseRune / createTreasureBox / createRandom*` (~25 calls) | Inline into the consuming submod (treasureChest, customitems, etc.). These are gameplay helpers, not framework infrastructure — no reuse value across submods today. Revisit only if two submods end up duplicating. |
| `Prop.getBooleanProperty / getIntegerProperty / getLongProperty / getFloatProperty / getStringProperty` (~275 calls) | Per-submod local helpers in the entry class, mirroring qualityoflife's `bool(p, key, def)`. Add `intp / longp / floatp / strp` siblings as needed. Don't ship a shared `Prop` class. |
| `SkillAssist.getSkill(String) / getSkill(int)` (6 calls — only in `WyvernMods.java` boot) | Inline a `HashMap<String,Integer>` in whichever submod actually needs name→id. Most upstream calls were for the dispatcher's parsing; framework `#giveskill` already does fuzzy skill resolution from the console. |
| `WoundAssist` / `ArmourAssist` (combat-only, not used by any non-combat submod) | Defer to combat phase (#53–55). When porting `combatchanges`, decide whether to inline or to lift these maps into framework `eventlogic`. |
| `SinduskLibrary` boot class (initializes the `Skill/Wound/Armour` maps) | Don't ship. Each replacement above is self-initializing or scoped to its submod. |

**Net effect:** the SinduskLibrary jar is *not* a dependency of any ported
submod. Sindusk users running our split mods alongside *his* mods can keep
his jar — there's no namespace clash because we're under
`com.garward.wurmmodloader.mods.*`.

## Replacements for `mod.sin.wyvern.DatabaseHelper`

Upstream WyvernMods bundles persistence for every feature into a single
`DatabaseHelper` class with two entry points (`onServerStarted` to create
tables, `onPlayerLogin` to ensure per-player rows). Under our split, each
submod owns its own persistence — no central helper.

**Use canonical, not legacy:** all DB access goes through
`com.garward.wurmmodloader.modsupport.ModSupportDb` (in
`wurmmodloader-modsupport.jar`). Do **not** import
`org.gotti.wurmunlimited.modsupport.ModSupportDb` — that's the legacy-compat
shim and shouldn't appear in new ports.

**`DatabaseBackend` SPI is unrelated.** That SPI
(`com.garward.wurmmodloader.api.database.DatabaseBackend`) plugs
Postgres/MariaDB into Wurm's *main* DB layer at the server level. Per-mod
scratch tables ride on the modsupport SQLite file regardless of which
backend the operator picked, so a submod doesn't implement this SPI to
get a place to store data. (If a server runs the postgresbackend mod, the
modsupport DB still resides in `sqlite/modsupport.db` — separate file.)

**Per-table ownership, fanned out across submods:**

| Upstream table | Owning submod after split | Notes |
| --- | --- | --- |
| `LeaderboardOpt` | port deferred — no current submod consumes it | Audit at the bounty / customtitles port; if no consumer surfaces, drop. |
| `SteamIdMap` | port deferred | Upstream populates but no in-tree reader. Defer until a feature needs it. |
| `PlayerStats` (kills/deaths/depots/hotas/titans/uniques) | `bounty` owns the schema; `supplydepots`, `titans`, `bestiary`/`rarespawns` write their columns via events the bounty submod subscribes to | Keep schema in bounty so we don't fragment the row. Other submods publish `PlayerStatIncrement(player, key, delta)` ModActionEvents; bounty persists. |
| `ObjectiveTimers` row `DEPOT` | `supplydepots` | Each submod runs `CREATE TABLE IF NOT EXISTS ObjectiveTimers (ID VARCHAR(30) PK, TIMER LONG)` then upserts its own row; co-existence is safe. |
| `ObjectiveTimers` row `TITAN` | `titans` | Same shared schema. |

**Per-submod boot pattern** (replaces upstream `onServerStarted` for each):

```java
// in <Feature>Mod.serverStarted() or @SubscribeEvent on ServerStartedEvent
try (Connection con = ModSupportDb.getModSupportDb()) {
    if (!ModSupportDb.hasTable(con, "ObjectiveTimers")) {
        try (PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE ObjectiveTimers (ID VARCHAR(30) NOT NULL PRIMARY KEY, " +
            "TIMER BIGINT NOT NULL DEFAULT 0)")) {
            ps.execute();
        }
    }
    // ensure this submod's row exists
    try (PreparedStatement ps = con.prepareStatement(
        "INSERT OR IGNORE INTO ObjectiveTimers (ID, TIMER) VALUES (?, 0)")) {
        ps.setString(1, "DEPOT"); // or "TITAN" in the titans submod
        ps.executeUpdate();
    }
}
```

**Per-player row ensure** (replaces upstream `onPlayerLogin`): subscribe to
the framework's `PlayerLoginEvent` from the submod that owns the row,
not from a shared helper. If two submods both need a row in the same
table, the schema-owning submod handles the ensure and the others trust
the row is present.

**Don't reintroduce a central helper.** A single Wyvern-shared persistence
module would re-couple everything we just split. If three submods end up
duplicating boilerplate, lift the boilerplate (not the schema) into
framework `eventlogic` — never into a wyvern-specific shared jar.

## Combat & shared-lib decisions

- Combat trio (`combatchanges`, `combatrating`, anything that touches
  damage math) is deferred until last — needs a diff against `armoury`
  and `DUSKombat` first.
- `wyvernshared` jar (cross-submod helpers) is a deferred decision; for
  now duplicate small helpers, audit at the end.
- `mod.sin.lib.{Prop, Util, SkillAssist}` usages get triaged in task #22 —
  most should fold into framework `eventlogic` or be inlined.

---

When the port is done: delete this file, update memory
`project_wyvernmods_port.md` with the final shape, and remove the
`WyvernMods/` source folder from the repo.
