# customdeity

Config-driven deity flag / affinity / template overrides. Operators map any
deity ID to a set of `Deity` setter values in `mods/customdeity.properties`,
and the mod applies them at `ServerStartedEvent`.

Generalized from Sindusk's WyvernMods `DeityChanges`, which hardcoded edits
to two specific Wyvern Reborn player gods. The Wyvern preset ships as a
commented-out example block — uncomment to apply it.

## Format

```
deity-<id>.<flag>=<value>
```

- `<id>` — numeric deity ID. Vanilla 1–12 (Fo / Magranon / Vynora / Libila /
  Wurm / Nogump / Walnut / Pharmakos / Jackal / Deathcrawler / Scavenger /
  Giant), player gods 31–36, custom gods 100+.
- `<flag>` — a `Deity` setter name in camelCase, dropping the `set` prefix.
- `<value>` — `true`/`false` for boolean setters; an integer / byte / float
  for numeric setters.

If a deity ID isn't loaded the line is logged and skipped (so the same config
works on servers with different deity rosters).

### Boolean flags

`roadProtector`, `warrior`, `favorRegenerator`, `befriendCreature`,
`befriendMonster`, `staminaBonus`, `foodBonus`, `healer`, `deathProtector`,
`deathItemProtector`, `allowsButchering`, `woodAffinity`, `metalAffinity`,
`clothAffinity`, `clayAffinity`, `meatAffinity`, `foodAffinity`, `learner`,
`itemProtector`, `repairer`, `waterGod`, `mountainGod`, `forestGod`,
`hateGod`.

### Numeric flags

| Flag | Type | Notes |
|---|---|---|
| `buildWallBonus` | float | Multiplier applied to wall-building skill checks for this deity's followers. |
| `templateDeity` | int *or* name | Numeric deity ID, or one of `FO` / `MAGRANON` / `VYNORA` / `LIBILA`. |
| `favoredKingdom` | byte | Kingdom this deity favors. |

## Example — re-skin player god 101 as a Magranon-template mountain warrior

```
deity-101.templateDeity=MAGRANON
deity-101.mountainGod=true
deity-101.hateGod=false
deity-101.metalAffinity=true
deity-101.deathProtector=true
deity-101.warrior=true
deity-101.learner=false
deity-101.repairer=false
deity-101.befriendCreature=false
deity-101.healer=false
deity-101.clayAffinity=false
deity-101.waterGod=false
```

## Provenance

The upstream `mod.sin.wyvern.DeityChanges` class hardcoded edits to deities
101 (Thelastdab) and 102 (Reevi) — the two custom player gods on Sindusk's
Wyvern Reborn server. Under our split, that hardcoding becomes config:
the mod is useful on any server that wants to retune deity flags without
recompiling.

Setter dispatch is reflective so the mod automatically picks up any new
boolean/numeric setter the server adds to `Deity` — no per-flag maintenance
required when vanilla evolves.

## Doesn't overlap with

- **spellcraft** — only calls `deity.addSpell(...)`. Different field set.
- **spellmod** — toggles `buildWallBonus` and `roadProtector` on Lurker /
  Continuum followers as part of its spell-add pipeline. If both mods set
  the same flag on the same deity, the load order wins; this mod runs at
  `ServerStartedEvent` like the others.
