# teleport

Custom teleport actions and arena-server transfer landing, ported from
Sindusk's WyvernMods teleport module. Each behavior is a toggle in
`mods/teleport.properties` — turn off the ones you don't want.

## What it does

### Arena landing redirect
- **useArenaTeleportMethod** — patch `PlayerMetaData.save` so any player
  whose stored position is in the vanilla 4000–4050 transfer-landing box
  gets redirected on next save:
  - if a citizen of a deed → the deed's token
  - else on a PvE server → JENNX/JENNY
  - else on a PvP server → a random walkable surface tile not inside a deed.

  Set false on installs that don't use the 4000–4050 transfer landing
  convention (single-server installs, custom maps, etc.).

### Custom action: Arena Teleport / Arena Escape
- **actionArenaTeleports** — adds two body-targeted actions:
  - **Arena Teleport** (PvE only): 60-second meditation channel that
    transfers the player to the configured arena server
    (`Servers.localServer.serverNorth`).
  - **Arena Escape** (PvP only): 180-second meditation channel that
    transfers the player back to the home server
    (`Servers.localServer.serverSouth`). Blocked by stealth, nearby
    enemies, or active combat.

  Disable on installs without a north/south server pair configured.

### Custom action: Village Teleport
- **actionVillageTeleport** — adds a body-targeted "Village Teleport"
  action (PvE, citizens only). 60-second channel teleporting the player to
  their citizen-village token. Off by default — upstream defined the action
  class but never registered it; opting in restores the feature.

## Provenance

Ported from the `## >> TELEPORT MODULE << ##` block of Sindusk's
WyvernMods (`mod.sin.wyvern.TeleportHandler`,
`mod.sin.actions.{ArenaTeleport,ArenaEscape,VillageTeleport}Action`).
Toggle names preserved one-for-one with upstream so existing operators can
copy their `WyvernMods.properties` values across without renaming. The
upstream `enableTeleportModule` master flag is omitted — under our split,
the presence of the jar *is* the master.

The three custom actions are wired through the framework's
`BodyMenuPopulateEvent` rather than per-action `BehaviourProvider`
classes — the mod registers the `ActionEntry` + `ActionPerformer` once on
server start, then conditionally adds the entry to the body menu in the
event handler.
