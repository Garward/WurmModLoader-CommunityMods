# supplydepot

Periodically spawns a "supply depot" item somewhere on the world surface.
The first player who stands close enough and runs the **Capture depot**
action gets a reward bundle: an arena cache, a sorcery fragment, optional
caches from the `caches` submod, kingdom tokens, seryll, sleep powder,
a small chance at a HotA statue, and a copper purse. Ported from
WyvernMods's `SupplyDepots`.

The upstream version was hardwired to PvP-arena maps and spawned in a
"20-80% of world tile size" ring. This port keeps the upstream behavior
available behind a flag, but **defaults to PvE** and exposes a real
**spawn filter** so you can pin depots to a region (e.g. mountain peaks,
a snow biome, a desert pocket) on any world map.

## What ships in the jar

- Three custom item templates — registered automatically:
  - `mod.item.arena.depot` — the depot itself
  - `mod.item.arenacache` — per-capture cache reward
  - `mod.fragment.sorcery` — sorcery fragment reward
- One bytecode patch — `Players.sendAltarsToPlayer` is hooked so the
  depot's light effect re-syncs to clients on login. Skipped entirely
  when `useDepotLights=false`.
- A new GM-visible action — `Capture depot`, registered into the
  ItemMenuBuildEvent for any item that resolves to the depot template.

## How it spawns

Every `pollIntervalSeconds` (default 30) the module checks whether the
last spawn was longer than `respawnTimeMs` ago (default 2 hours). If so
and there isn't already a live depot on the world, it picks a tile via
the spawn filter, drops the depot, and broadcasts the location through
both a `[arena]` server tab and the global Freedom chat.

Surface tiles must satisfy **all** of:

1. Inside the optional region disc (`spawnAreaCenter*` / `spawnAreaRadius`).
2. Height between `spawnMinHeight` and `spawnMaxHeight`.
3. Slope ≤ `spawnMaxSlope`.
4. Tile type in `spawnTileTypes` (when set).
5. No deeded tile within ±50 tiles.

The picker takes 200 attempts. If none pass, it logs a warning and
re-tries on the next poll — which is why broadly impossible filters
(min height = 999) won't crash the server, just stall depots.

## PvE / PvP behavior

- **`requirePvpServer=false` (default):** depots spawn on any server.
- **`requirePvpServer=true`:** depots only spawn while
  `Servers.localServer.PVPSERVER` is true. This restores the upstream
  WyvernMods gating; useful if you're running a multi-server cluster
  where one of the worlds is the PvP arena.

## Multi-server vs single-server

Upstream WyvernMods assumed a cluster: a PvE login world plus a PvP
"arena" map where the depots actually spawned. None of that is required
here — on a single PvE server, leave `requirePvpServer=false` and use
the spawn filter to confine depots to whatever biome you want them in.

If you do run the depot mod alongside the `arena` mod on a dedicated
arena world, set `useDefaultArenaRing=true` to restore the upstream
20-80% ring.

## Spawn filter recipes

**Mountain peaks, snow-capped only**
```
spawnMinHeight=400
spawnMaxHeight=999
spawnMaxSlope=80
spawnTileTypes=TILE_SNOW,TILE_CLIFF
```

**Sand desert pocket centered on tile 1200,800**
```
spawnAreaCenterX=1200
spawnAreaCenterY=800
spawnAreaRadius=250
spawnTileTypes=TILE_SAND
```

**Steppe grasslands anywhere**
```
spawnTileTypes=TILE_STEPPE,TILE_GRASS
```

**Faithful upstream WyvernMods (PvP arena map, ring spawn, no filter)**
```
useDefaultArenaRing=true
requirePvpServer=true
```

## Capture mechanics

The action is a no-move 1/10 second tick action. First tick starts a
`captureTimer`-tick countdown (default 2400 = 4 minutes), broadcasts
"X is beginning to capture an Arena depot!" globally (rate-limited by
`captureMessageInterval`), and sends a "Capturing" progress bar.
Subsequent ticks check elapsed time. If the player walks out of
`captureRadius`, the action is interrupted by vanilla and they have to
restart. Completion drops the reward bundle, destroys the depot, and
broadcasts the win.

Players need at least `fightingSkillRequirement` (default 25) fighting
skill — flavor + a soft brake against alts grabbing fresh-spawn loot.

## Reward bundle

Always:

- 1 × arena cache (90-100ql)
- 1 × sorcery fragment (90-100ql)
- 1 × seryll bar (80-100ql)
- 1 × sleep powder (99ql)
- `minCopperReward`-`maxCopperReward` copper, in coin form

Conditional:

- `enchantOrbTemplateId` if > 0 → 1 × enchant orb (60-120 power)
- `caches` submod loaded → `minCaches`-`maxCaches` random caches drawn
  from the weighted pool (see *Cross-submod integration* below)
- `kingdomTokenTemplateId` > 0 → `minKingdomTokens`-`maxKingdomTokens`
  kingdom tokens (40-90ql)
- `hotaStatueChancePercent` roll → 1 × HotA statue (random aux), 50kg

## Cross-submod integration

The `caches` submod (when installed and loaded — alphabetical order
puts it before supplydepot) exposes static template ids for its tiered
loot caches: `armourId`, `artifactId`, `crystalId` (×3 weight),
`dragonId` (×2), `gemId`, `moonId` (×2), `riftId`, `treasureMapId`.
This module reads those reflectively at first reward roll and skips
the grab-bag silently if the class isn't present. There's no
compile-time dependency between the two submods.

The enchant orb integration is the same idea but explicit — set
`enchantOrbTemplateId` to whatever id your enchant-orb mod registers
its template with. -1 (default) means "no orb in the bundle."

## Configuration reference

See `mod.properties` — every field has an inline comment with default
and meaning.

## Limitations / known gaps

- Capture only requires presence and skill — there's no contestation /
  PvP capture-the-flag scoring. Upstream had this on the arena server
  and it relied on map-side mechanics not ported here.
- Spawn filter currently picks a random tile and re-rolls; for very
  narrow filters on very large maps that's wasteful. We can switch to
  pre-indexed tile lists later if it becomes a bottleneck.
- Depot lights use effect id 25 hardcoded. Configurable color is a
  known nice-to-have.
