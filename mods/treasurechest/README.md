# treasurechest

Tiered treasure-chest loot ported from Sindusk's WyvernMods
`enableTreasureChestLootModule`. Affects vanilla treasure chests that
spawn naturally in the world (via `Zone.createTreasureChest`).

## What it does

Vanilla rolls auxData 0-9 per chest and fills with a flat loot pool.
This mod:

1. **Widens the auxData roll to 0-99** (`boostTierDistribution`). This
   is what enables tiered chests — without it, every roll would land in
   the rare-tier branch.
2. **Replaces `Item.fillTreasureChest`** (`replaceLootTable`) with a
   three-tier table:
   - **Rare (0-59)** — sets chest rarity to 1. Source crystal,
     adamantine, glimmer steel, gem; chance of seryll, lump, illusion
     potion, fireworks, affinity orb, random potion, one rift item.
   - **Supreme (60-89)** — sets rarity to 2. Source + star/normal gem,
     adamantine + glimmer (2-3), seryll (1-3), chance of fireworks,
     orb, dragon hide/scale; one of each rift item.
   - **Fantastic (90-99)** — sets rarity to 3. Source + star gem,
     adamantine + glimmer (3-5), seryll (2-4), chance of orb,
     fireworks, hide/scale, spyglass, bag of keeping, more rift items;
     plus one OP roll out of 500 (drake hide ×3, dragon scale ×3, hota
     statue, bone collar, sorcery, dragon egg).

## Toggles

See `mod.properties`. Both patches gate on individual flags, so you can
ship the tier-distribution boost without the loot-table replacement
(useful with other treasure-chest mods).

## Affinity Orb dependency

Upstream's loot table includes Affinity Orbs (template id 22767) in the
higher-tier branches. That's a custom item shipped by upstream's
`ItemMod`; it isn't ported in our split yet. The orb spawns silently
no-op if the template isn't registered (the `affinityOrbTemplateId` is
checked against `ItemTemplateFactory` before insertion). Set
`affinityOrbTemplateId=0` to disable orb spawns regardless.

## Provenance

Ported from `mods/WyvernMods/src/main/java/mod/sin/wyvern/TreasureChests.java`
(`enableTreasureChestLootModule` toggle in upstream
`WyvernMods.properties`). No `depend.requires` — this is self-contained;
it just patches vanilla treasure chests.
