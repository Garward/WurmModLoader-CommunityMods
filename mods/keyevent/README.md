# keyevent

"Call upon the heavens" ritual + four tomes (key fragment, enchant orb,
eternal orb, affinity orb) ported from Sindusk's WyvernMods.

## The ritual

A player activates a **key fragment** and chooses _Call upon the heavens_.
The mod broadcasts six rounds of cross-server dialogue (~6 minutes total)
in which each major deity demands a response from the casting player. The
performer must shout the correct answer inside each ~30-second window:

| Phase | ~Time | Required answers |
|-------|-------|------------------|
| Desire | 40-60s | "wealth", "honor", "knowledge", "power" |
| Fo | 120-150s | one of Fo's listed powers |
| Magranon | 170-200s | one of Magranon's listed powers |
| Vynora | 220-250s | one of Vynora's listed powers |
| Libila | 270-300s | one of Libila's listed powers |
| Ascend | 340-370s | "ascend" |

Available power lists vary per round based on which categories
(weapon/creature/industry enchant, heal, tame) the player has already
unlocked. Matching is fuzzy substring — the prompts in chat tell the
player exactly what to say.

On completion the action consumes `fragmentsRequired` (default 50) key
fragments from the inventory and spawns one **`ItemList.keyHeavens`** at
QL 99 in the player's hand. The ritual is **single-active globally** —
only one performer at a time.

## Item templates

| Resource id | Item | Image | Notes |
|-------------|------|-------|-------|
| `mod.fragment.key` | "key fragment [1/50]" | 462 | Key fragments combine into the ritual; 50 are consumed on success. |
| `wyvern.enchantorb` | "enchant orb" | 819 | Holds a single transferred enchant; activate × enchanted item to apply. |
| `mod.item.eternal.orb` | "eternal orb" | 819 | Activate × enchanted item to drain its enchants into a fresh enchant orb (consumed). |
| `wyvern.affinityorb` | "affinity orb" | 919 | Activate to pick one of 10 random skills (deterministic on `auxData` seed) and gain an affinity (cap 5). |

## Actions

### Call upon the heavens (key fragment, body menu)
Starts the ritual. Locks the performer for ~6 minutes; chat lines from
the performer are matched against the active deity's expected answers.

### Apply enchants (enchant orb, source × target)
Transfers the orb's stored spell effects into the target item. Handles
runes (eff.type < -60), already-max-power, partial transfer when the
existing power is higher, and full replacement when target had nothing.

### Absorb enchants (eternal orb, source × target)
Spawns a new enchant orb of source QL with the target's spell effects
copied over and tagged in the description (first letter + power, e.g.
"L99 W90"). Source eternal orb is consumed. Type 120 enchants block
absorption; arrows are also blocked.

### Gain affinity (affinity orb, body menu)
Opens a BML chooser of 10 random skill names seeded by the orb's
auxData byte (so re-opening shows the same list). Selecting one consumes
the orb and grants one affinity in that skill (capped at level 5).

## Configuration (`mods/keyevent.properties`)

| Key | Default | Effect |
|-----|---------|--------|
| `enabled` | `true` | Master toggle. When false, no templates and no actions register. |
| `fragmentsRequired` | `50` | Number of key fragments consumed on a successful ritual. Must be ≥ 1. |

## Cross-submod coupling

The `wyvern.enchantorb` template id is consumed by:

- **supplydepot** — drops enchant orbs through `enchantOrbTemplateId`. Pin it
  to the id this submod logs at boot.
- **caches** — likewise via its own enchant-orb config key. Pin from the
  same boot log.

Look up the assigned ids in the server log:

    [keyevent] registered mod.fragment.key (id=NNN)
    [keyevent] registered wyvern.enchantorb (id=NNN)
    [keyevent] registered mod.item.eternal.orb (id=NNN)
    [keyevent] registered wyvern.affinityorb (id=NNN)

## Implementation notes

- `KeyEventState` (renamed from upstream `KeyEvent` to avoid clashing with
  the framework's `*Event` naming) holds all ritual state in static
  fields and is single-active globally.
- `AffinityOrbQuestion` lives under `com.wurmonline.server.questions` so
  it can extend the package-private `Question` base class. Its BML form
  is hand-built (no `net.coldie.tools.BmlForm` dependency).
- `FreedomBroadcast.send` walks `Players.getInstance().getPlayers()` and
  sends one `Message((byte) 10, "GL-Freedom", ...)` per player; the
  upstream `WcKingdomChat` cross-server piece is dropped (single-server
  PvE focus).
- `PlayerMessageEvent` already delivers the raw chat body without the
  leading `<name>: ` prefix, so `KeyEventState.handlePlayerMessage` is
  fed `event.getMessage()` directly.

## Provenance

Ported from `ModSources/upstream/sindusk/wyvernmods`:

- `mod/sin/wyvern/KeyEvent.java` → `KeyEventState`.
- `mod/sin/items/KeyFragment.java`, `EnchantOrb.java`, `EternalOrb.java`,
  `AffinityOrb.java` → `KeyEventTemplates`.
- `mod/sin/actions/items/KeyCombinationAction.java`,
  `EnchantOrbAction.java`, `EternalOrbAction.java`,
  `AffinityOrbAction.java` → same class names, namespace rebased.
- `com/wurmonline/server/questions/AffinityOrbQuestion.java` → kept under
  the same `questions` package.
