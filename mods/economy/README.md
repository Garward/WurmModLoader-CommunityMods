# economy

NPC trader / economic balance changes. Ported from Sindusk's WyvernMods
`EconomicChanges` module. Each behavior is a toggle in
`mods/economy.properties` — turn off the ones you don't want.

## What it does

### voidTraderMoney

Drains 80% of currency that flows into NPC trader shop diff. Implemented as
a `ShopDiffEvent` subscription (framework). When the player is actually
buying something (i.e. `money + currentShopDiff > 0`), the resulting
shopDiff is scaled to 20% of what vanilla would have applied.

This uses the framework's `com.garward.wurmmodloader.api.events.trade.ShopDiffEvent`
— no mod-side bytecode patch.

### disableTraderRefill

Stops NPC traders from refilling their stock off the kingdom's coffers.
Bytecode patch on `Creature.removeRandomItems` forces the random
`Server.rand.nextInt(N)` roll to `1`, preventing the stock churn that
removes the player's deposits.

This is mod-side bytecode (a one-line `nextInt → 1` instrument) — too
narrow for a framework event.

## Provenance

Ported from `mod.sin.wyvern.EconomicChanges` (WyvernMods). Toggle names
preserved one-for-one with upstream.

Upstream's `adjustSealedMapValue` toggle is intentionally **not** ported
here. The logic depended on a custom `mod.sin.items.SealedMap` template
that doesn't exist yet under the split. When the future Item submod lands,
it can subscribe to its own `Item.getValue` hook for SealedMap pricing
without bringing this mod into the dependency.

Upstream's commented-out village-upkeep / disband-refund code is omitted —
it was already disabled in the original (`[2/4/19] Disabled`).

## Framework promotions

Porting this mod added one framework event:

- **`ShopDiffEvent`** (`api/events/trade/`) — fires inside
  `Trade.addShopDiff(long)` before vanilla's `shopDiff += money` accumulator.
  Mods can mutate the incoming amount via `setMoney(long)`. Reusable for
  any economy mod that wants to drain, inflate, or redirect NPC trader
  currency flow.
