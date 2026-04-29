# bounty

Coin-on-kill bounty + pluggable corpse-loot, ported from Sindusk's
WyvernMods (`mod.sin.wyvern.Bounty`, `PlayerBounty`, `LootBounty`). Only
the vanilla-applicable subset ships here; custom-creature drops (Titan
plate, Wyvern unique masks, AffinityOrb, the cache items, etc.) live in
their owning submods and plug in via {@link BountyRegistry}.

## How it works

Two reward paths, deliberately decoupled:

- **Player bounty** (`enablePlayerBounty`) — a `CreatureDeathEvent`
  subscriber. The framework already names a single canonical killer
  (highest-damage attacker captured at death), so we don't replicate the
  upstream "combatant in last 2 minutes" map walk. Servers wanting
  group-play rewards register a `BountyRegistry.KillerRewardHandler`.
- **Corpse loot** (`enableCorpseLoot`) — a javassist patch on
  `Creature.die` that walks the registered `CorpseLootHandler` chain
  after the corpse is placed.

## Configurability surface

`mod.properties` exposes four open-ended schemas — see the file for
inline docs.

| Key | Notes |
| --- | --- |
| `enablePlayerBounty`, `enableCorpseLoot` | Master toggles for each path. |
| `enableGoblinMetalDrop`, `enableChampionLoot`, `broadcastInterestingLoot` | Built-in corpse-loot handlers (gated individually). |
| `pvpBountyMultiplier`, `bountyFloorIron`, `bountyCeilingIron` | Coin clamps + PvP bonus. |
| `strength.scale`, `strength.floor`, `strength.cap` | Default formula's tunables. Replace the formula entirely with `BountyRegistry.setStrengthFunction(...)`. |
| `prefix.<name>=<mult>` | Suffix-match multipliers on `Creature.getPrefixes()`. Add as many as you like. |
| `rewardOverride.<creatureName>=<iron>` | Per-creature fixed reward, beats the strength formula. |

## Extending it

`BountyRegistry` is a public static-method SPI. From any other mod's
`onServerStarted`:

```java
// Fixed reward overrides
BountyRegistry.setReward("titan", 5_000_000L);
BountyRegistry.setReward(ItemList.troll, 50_000L);

// Custom prefix multipliers
BountyRegistry.setTypePrefixMultiplier("corrupted", 2.5);

// Custom corpse loot — runs after vanilla loot is placed
BountyRegistry.addCorpseLootHandler((victim, corpse, strength) -> {
    if (TitanRegistry.isTitan(victim)) {
        // insert titan plate armour into corpse
    }
});

// Extra killer rewards — runs after the coin payout
BountyRegistry.addKillerRewardHandler((player, victim, strength) -> {
    if (TitanRegistry.isTitan(victim)) {
        TitleRegistry.grant(player, "TitanSlayer");
    }
});

// Replace the strength formula entirely
BountyRegistry.setStrengthFunction(mob ->
        mob.getStatus().getMaxHealth() / 100.0);
```

Built-in handlers (`BuiltInLootHandlers.GOBLIN_METAL_LUMP`,
`CHAMPION_LOOT`) are package-private singletons registered against
their config toggles; replace them by toggling off + adding your own.

## Deferred

- Custom-creature drops (Wyvern uniques' unique masks, Titan plate, the
  cache items) — those land with their owning submods (#38 Titan, #39
  RareSpawn, #43 TreasureChest, #45 Caches).
- The "killer was a damager in last 2 minutes" reward fan-out — the
  framework already supplies a single attributed killer; servers wanting
  group rewards register a `KillerRewardHandler` that walks their own
  damage map.
