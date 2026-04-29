# caches

Tiered loot caches ported from Sindusk's WyvernMods (`Caches` +
`TreasureCacheOpenAction`). Players right-click a cache item, wait
through a short timer, and receive rolled loot scaled by the cache's
quality and rarity.

## Cache types

| Cache | Basic loot | Extra (rare) loot |
|-------|-----------|-------------------|
| **armour** | Cloth, leather, studded, chain, plate pieces | Painted (RGB 100/100/100) armour piece |
| **artifact** | Vanilla weapons (swords, axes, mauls, spear, halberd, staff) plus any custom-weapon ids configured | — |
| **crystal** | Custom crystal ids configured by the operator | — |
| **dragon** | Drake hide / dragon scale (weight-scaled) | Dragon-leather/scale armour piece |
| **gem** | Diamond / emerald / opal / ruby / sapphire | Black opal / star gems |
| **moon** | Adamantine / glimmer / seryll bars (weight-scaled) | Adamantine or glimmer chain/plate piece |
| **potion** | Skill-tick potions across all crafts | — |
| **rift** | Rift crystal / wood / stone | — |
| **titan** | Random tool/weapon with Titanforged enchant + special material | — |
| **tool** | Smithing/woodworking/farming tools with rune + WoA/CoC/Efficiency/BotD/Titanforged enchants | — |
| **treasure-map** | A new treasure map (requires the treasure-hunting mod — see below) | — |

Cache rarity boosts every basic-loot item to at least the cache's
rarity. Higher quality drives higher tiers, more enchants, and the
chance to upgrade rarity past the cache's own roll.

## Caches don't spawn themselves

This submod registers the cache *templates* and the open action — it
doesn't create cache items. Caches are typically dropped by other
systems (rare-spawn loot, supply depots, GM creation, mission rewards).
To hand-spawn one for testing, use the `#createitem` console command
once your server is running:

```
#createitem armour cache 80
```

## Toggles

`mod.properties` keeps two CSV pools open for cross-submod content:

- `artifactCacheCustomTemplateIds` — custom weapon ids appended to the
  artifact pool. Upstream included Club / BattleYoyo / Knuckles /
  Warhammer here; those custom weapons land with the future combat
  submod, until then leave this blank.
- `crystalCacheTemplateIds` — the *entire* crystal-cache pool. Upstream
  used ChaosCrystal + EnchantersCrystal (deferred to the crystals
  submod); leave this blank until those land or crystal caches will
  yield nothing.

Unregistered ids are silently skipped at draw time (the loot routine
checks `ItemTemplateFactory` before each `createItem` call), so a
typo or stale id won't crash the cache opening.

## Treasure-map cache requires treasurehunting

The treasure-map cache calls
`com.pveplands.treasurehunting.Treasuremap.CreateTreasuremap`
reflectively. Without that mod deployed alongside, opening a
treasure-map cache reports a polite refund message and yields nothing.
The cache template still registers either way.

## Provenance

Ported from
`mods/WyvernMods/src/main/java/mod/sin/wyvern/Caches.java` and the
matching `mod.sin.actions.items.TreasureCacheOpenAction`. The thirteen
upstream cache template classes
(`mod.sin.items.caches.{Animal,Armour,…}Cache.java`) collapsed into a
single registration helper since they only differed by id string,
display name, description, and value. AnimalCache and WeaponCache were
defined upstream but never wired into the open pipeline (no entries in
`CACHE_IDS`, no branches in `getBasicTemplates`); we omit them here to
match runtime behaviour.
