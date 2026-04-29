# miscchanges

A grab-bag of vanilla-Wurm bug fixes and QoL adjustments lifted from
Sindusk's WyvernMods miscellaneous-changes module. Each behavior is a
toggle in `mods/miscchanges.properties` and ships independently — turn
off the ones you don't want.

> **Status:** skeleton release — the gradle module + mod.properties
> register cleanly and configuration is parsed, but no bytecode patches
> are wired up yet. Behaviors land in subsequent porting passes.

## What it does

### Information tab
- **enableInfoTab / infoTabName / infoTabLine-#** — pop a server-info
  tab into every player's chat on login, with one line per
  `infoTabLine-N` entry.

### Bridges, mailboxes, traders
- **ignoreBridgeChecks** — drop most bridge validity checks (build
  bridges into/over/through houses).
- **disableMailboxUsageWhileLoaded** — block mailbox use while it's
  loaded in a vehicle (paired with the loadable-mailbox mod).
- **uncapTraderItemCount** — let traders display their full stock
  instead of capping at 9 per item.

### Legendary creatures (Uniques)
- **increasedLegendaryCreatures / increasedLegendaryFrequency** — extra
  spawn rolls per 6-hour check (0 disables, 1 = vanilla, 5 = 5×).
- **allowFacebreykerNaturalSpawn** — add the WyvernMods custom
  Facebreyker to the natural unique spawn list.

### Titles
- **announcePlayerTitles** — server-broadcast title earnings (excluding
  PvP servers and GM-power players); relays to a Discord "event"
  channel via DiscordRelay if configured.
- **changePumpkinKingTitle** — female form of the 100-farming title
  becomes "Pumpkin Queen".

### Improvement & smithing
- **improveCombinedLeather** — allow leather of any QL during improve.
- **allowModdedImproveTemplates** — accept Spectral Hide / Glimmerscale
  as improve materials.
- **rareMaterialImprove** — rare materials may transfer rarity during
  normal improve, not just consumption.
- **rarityWindowBadLuckProtection** — failed rarity rolls increase the
  next roll's chance; successes reset.
- **rareCreationAdjustments** — propagate creation-material rarity to
  the created item (off by default; opens rarity-cascade exploits with
  sacrifice / lye chains).
- **alwaysArmourTitleBenefits** — armour title improvement bonus
  applies even when the title isn't equipped.

### Combat fixes
- **disableMinimumShieldDamage** — drop the 0.01 minimum-damage floor
  on shields.
- **creatureArcheryWander** — creatures wander a random short distance
  after each archery hit, breaking out of "free archery" range.
- **reduceActionInterruptOnDamage** — roughly halve the chance of an
  action being interrupted by incoming damage.
- **guardTargetChanges** — guards stop joining combat against modded
  Legendaries / Titans / Rare Spawns.

### Action grounding
- **fatigueActionOverride** — players can take fatiguing actions
  (mine, improve, farm, etc.) while not grounded. PvP destroy actions
  keep their mounted-block.

### Mounted / vehicles
- **fixVehicleSpeeds** — speed updates run every time, not behind a
  random gate (no more horses stuck at the wrong speed for 20 s after
  archery).
- **fixMountedBodyStrength** — mounted encumbrance calc respects modded
  body-strength carry caps.
- **mayorsCommandAbandonedVehicles** — mayors get temporary command of
  vehicles abandoned on their deed (owner offline 7 days; ends if they
  move the vehicle off-deed and disembark; cargo hold stays locked).

### Mail
- **reduceMailingCosts** — 1c → 10i per shipment.

### Food / drink
- **higherFoodAffinities** — affinity skill-gain potency 10% → 30%.
- **disableFoodFirstBiteBonus** — drop the vanilla 10× first-bite
  affinity duration bonus; every bite grants the same duration.
- **adjustedFoodBiteFill** — non-linear food fill (more at low QL,
  tapering at high QL).
- **opulenceFoodAffinityTimerIncrease** — Opulence cast adds 0.25% per
  power to affinity timer (100 power = +25%).
- **lessFillingDrinks** — non-water drinks fill the water bar ~1/5 as
  much as vanilla.
- **royalCookNoFoodDecay** — Royal Cook title makes food immune to
  decay.

### Smithing speed
- **royalSmithImproveFaster** — Royal Smith title improves 10% faster.
- **fasterCharcoalBurn** — charcoal piles tick twice per interval
  (double burn rate, same yield).

### Production
- **disableSmeltingPots** — disable smelting pots without removing the
  item.

### Lockpicking & permissions
- **reduceLockpickBreaking** — +40 QL to break-check calc.

### Sorcery / tomes
- **tomeUsageAnyAltar** — allow tome usage at any flat 3×3 altar
  regardless of height / water / cave.
- **keyOfHeavensLoginOnly** — restrict Key of the Heavens usage to the
  login server only (cluster-deity safety).
- **hideSorceryBuffBar** — hide sorcery buffs from the buff bar (still
  listed under Spell Effects).

### Houses
- **largerHouses** — double the building size players can construct
  (multiplies effective carpentry during planning).

### Sleep
- **bedQualitySleepBonus** — bed QL boosts sleep bonus by 0.5% per QL.

### Mycelium
- **allowFreedomMyceliumAbsorb** — Libila followers on Freedom can
  absorb mycelium.

### Anti-macro logging
- **logExcessiveActions** — log players issuing >10 commands per
  second. Catches macros, also catches held movement keys.

### Death broadcast
- **globalDeathTabs** — broadcast death tabs to GL-Freedom across all
  servers.
- **disablePvPOnlyDeathTabs** — allow PvP deaths to appear in the
  Deaths tab on PvE servers.

### Skill gain curve
- **useDynamicSkillRate** — Revenant dynamic skill rate (gain adjusts
  with skill level). See the curve at
  https://i.imgur.com/XQeMJjV.png.

### Imbues
- **reduceImbuePower** — imbue smears apply 1/5 of vanilla power.

### GM / help
- **disableGMEmoteLimit** — GMs (power ≥ 1) bypass the 5 s emote sound
  cooldown.
- **disableHelpGMCommands** — hide GM commands from `/help` for
  non-GMs.

### Mission system
- **fixMissionNullPointerException** — lazy-init the Epic Mission
  template list so a missing init can't NPE the server later.

### Cross-server
- **fixLibilaCrossingIssues** — old Libila-faith-reset workaround;
  probably unnecessary post-1.9 priest update, harmless to leave on.

### Achievement DB
- **sqlAchievementFix** — patch the achievement SQL errors introduced
  in WU 1.9.

### Deity passives
- **changeDeityPassives** — tweak demigod (deity IDs 101 / 102)
  passives. Revenant compatibility shim; will move to the `deity`
  submod once that port lands.

## Soft dependencies

- **DiscordRelay** — `announcePlayerTitles` will additionally relay to
  Discord if a DiscordRelay mod is loaded; otherwise just the in-game
  broadcast happens.
- Several `*ToVehicle` toggles in the **qualityoflife** submod assume
  `fatigueActionOverride` is on here — leave that toggle enabled if
  you also run qualityoflife with the vehicle-deposit options.

## Provenance

Ported from the `## >> MISCELLANEOUS CHANGES MODULE << ##` block of
Sindusk's WyvernMods (`mod.sin.wyvern.MiscChanges`). Toggle names
preserved one-for-one with upstream so existing operators can copy
their `WyvernMods.properties` values across without renaming. The
upstream `enableMiscChangesModule` master flag is omitted — under our
split, the presence of the jar *is* the master.

A handful of toggles (`tomeUsageAnyAltar`, `keyOfHeavensLoginOnly`,
`changeDeityPassives`) belong conceptually to other submods
(`keyofheavens`, `deity`). They live here at the skeleton stage to
preserve operator config compatibility; they will move to the owning
jar when those submod ports add the actual patches.
