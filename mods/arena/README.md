# arena

PvP-only behaviour patches ported from Sindusk's WyvernMods `Arena`
submodule. Drop-in jar; configure via `mod.properties`.

## What this mod is — and is not

This module **does not spawn any creatures or items**. It is a grab-bag
of bytecode patches that change PvP combat / theft / siege / death /
local-range behaviour. World bosses (Titans, Reaper, Spectral drake,
Blue wyvern) and supply depots live in their own submods (`titan`,
`rarespawn`, `supplydepot`).

## PvE servers: this mod is dormant

Every patch is gated at runtime on
`com.wurmonline.server.Servers.localServer.PVPSERVER`. On a single-server
PvE setup the jar loads, the patches install, but every inserted code
branch is skipped — the mod has no effect.

If you have no plans for a PvP server, you can safely leave `enabled=true`,
or set `enabled=false` to skip the patches entirely.

## Multi-server cluster vs single-server PvP

The upstream WyvernMods `Arena` module was built for a multi-server
cluster: a Freedom (PvE) server in `west`, an Arena (PvP) server in
`center`. The `sendNewSpawnQuestionOnPvP` and `respawnPlayer` features
do cross-server transfers (`p.sendTransfer(...)` to `serverWest` or
`serverSouth`).

- **Single-server PvP** — set `PVPSERVER=true` in `wurm.ini`. Patches
  fire. Cross-server transfer features (`Transfer to PvE` button on the
  respawn dialog) will display but `serverSouth`/`serverWest` will be
  null, and the user just gets an error message.
- **Multi-server cluster** — define your PvE server as `serverSouth` /
  `serverWest` in the cluster config and the transfer paths work as
  upstream intended.

## Cross-mod interaction

When `sameKingdomVillageWarfare=true`, village guards must be told not
to attack world bosses. Arena reaches into the `titan` and `rarespawn`
submods at runtime via `ArenaCrossMod` (reflective, optional):

- If `titan.jar` is present — `Village.isEnemy` returns `false` for any
  Lilith / Ifrit and their minions.
- If `rarespawn.jar` is present — same exemption for the Reaper and
  Spectral drake (Blue wyvern is intentionally not on the rare-creature
  list since it doubles as a mount).

Both bridges fail closed: if the submod is missing, the bridge returns
`false` and the patch keeps the upstream behaviour.

## Configuration

See `mod.properties`. Every flag defaults to `true`; set to `false` to
skip an individual patch. Master toggle is `enabled`.

| Flag | What it does |
|---|---|
| `equipHorseGearByLeading` | Equip / unequip horse gear without taming or branding (PvP only). |
| `lockpickingImprovements` | Allow lockpicking on PvP; allow it on PvE for unowned containers and treasure chests. |
| `placeDeedsOutsideKingdomInfluence` | Skip the kingdom-border check when founding a village. |
| `disablePMKs` | Block player-made kingdoms on PvP. |
| `disablePlayerChampions` | Block the real-death / champion question on PvP. |
| `arenaAggression` | Re-route `Player.getAttitude` through `ArenaAttitude` (GM, pet, team, citizens, allies → friendly; anyone else → enemy). |
| `enemyTitleHook` | Append `(ENEMY)` to enemy player names on PvP. |
| `enemyPresenceOnAggression` | "Enemy nearby" indicator uses attitude instead of kingdom membership. |
| `useAggressionForNearbyEnemies` | Action range checks use attitude instead of kingdom. |
| `adjustFightSkillGain` | 1.5× fight skill gain on PvP. |
| `allowSameKingdomFightSkillGains` | Get fight skill from killing same-kingdom enemies on PvP. |
| `disablePvPCorpseProtection` | No loot protection on player corpses on PvP. |
| `allowAttackingSameKingdomGuards` | Tower guards considered hostile on PvP if attitude says so. |
| `fixGuardsAttackingThemselves` | Spirit guards no longer added to village target lists. |
| `allowArcheringOnSameKingdomDeeds` | Allow archery on same-kingdom deeds on PvP. |
| `bypassHousePermissions` | Players can do actions inside houses they don't own on PvP. |
| `allowStealingAgainstDeityWishes` | Stealing on PvP doesn't anger your god. |
| `sameKingdomVehicleTheft` | Take ownership of unlocked vehicles on PvP. |
| `sameKingdomPermissionsAdjustments` | `isOkToKillBy` / `hasBeenAttackedBy` short-circuit to `true` on PvP. |
| `sameKingdomVillageWarfare` | Same-kingdom non-allied villages are enemies on PvP. Also exempts titans / rare creatures from village attacks (via `ArenaCrossMod`). |
| `bypassPlantedPermissionChecks` | Move planted items regardless of permissions on PvP. |
| `disableFarwalkerItems` | Block farwalker twigs / amulets / pendants on PvP. |
| `alwaysAllowAffinitySteal` | Always allow affinity steal / battle rank changes on PvP. |
| `adjustMineDoorDamage` | 3× bash damage to mine doors on PvP. |
| `disableCAHelpOnPvP` | Hide the CA Help window on PvP. |
| `capMaximumGuards` | Cap deeds at 5 spirit guards on PvP. |
| `disableTowerConstruction` | Block tower construction on PvP. |
| `adjustLocalRange` | Local channel reduced to 50 tiles on PvP. |
| `disableKarmaTeleport` | Block karma teleport on PvP. |
| `limitLeadCreatures` | Players can lead only one creature on PvP. |
| `adjustBashTimer` | 600s wall-bash timer on PvP. |
| `reducedMineDoorOpenTime` | Mine doors auto-close after 30s instead of 120s. |
| `sendNewSpawnQuestionOnPvP` | Replace the vanilla spawn question with the arena variant (random surface tile or transfer-to-PvE button). |
| `makeFreedomFavoredKingdom` | Favoured kingdom on PvP server is Freedom. |
| `crownInfluenceOnAggression` | Crown influence spreads by attitude, not kingdom. |
| `disableOWFL` | "Old wurm full loot" off — players keep items on death on PvP. |
| `resurrectionStonesProtectSkill` | Resurrection stones save knowledge. |
| `resurrectionStonesProtectFightSkill` | Resurrection stones save fight skill. |
| `resurrectionStonesProtectAffinities` | Resurrection stones save affinities. |

## Skipped vs upstream

- **`adjustHotARewards`** — depends on `KeyFragment` and `AffinityOrb`
  templates, which are part of the KeyEvent submod (not yet ported).
  Vanilla HotA prize behaviour is unchanged.
- **`discordRelayHotAMessages` / `sendArtifactDigsToDiscord`** —
  upstream relied on `DiscordRelay`; not ported.
- **`enemyTitleHook` (custom-title half)** — upstream's hook also
  appended a custom title from `PlayerTitles`. This port keeps only the
  `(ENEMY)` suffix. The `customtitles` submod ports `addTitle` /
  `awardTitle` but not runtime title suffixing.

## What goes in the surrounding submods

- `mods/titan` — Lilith / Ifrit raid bosses (spawn anywhere on the
  surface mesh).
- `mods/rarespawn` — Reaper / Spectral drake / Blue wyvern (poll-driven
  random surface tiles).
- `mods/supplydepot` — supply depots (random surface tiles).

These submods will gain a shared `spawnArea*` config later (region,
height range, slope, tile-type allowlist) so server owners can confine
spawns to a designated area on a single map without needing a separate
arena cluster server.
