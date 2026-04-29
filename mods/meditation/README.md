# meditation

Meditation-path balance changes (Love / Hate / Insanity / Knowledge / Power /
None) — terrain-based path selection, ability cooldowns, and a stack of
level-gated passive bonuses. Ported from Sindusk's WyvernMods
`MeditationPerks` module. Each behavior is a toggle in
`mods/meditation.properties` — turn off the ones you don't want.

## What it does

### Path selection

- **simplifyMeditationTerrain** — replaces vanilla's distance-to-landmark
  path picker with a flat tile-type lookup:
  - grass / bush / tree → **Love**
  - mycelium / mycelium-bush / mycelium-tree → **Hate**
  - sand → **Knowledge**
  - rock / cliff → **Power**
  - underground (any) → **Insanity**
  - everything else → **None**

### Ability removals

- **removeInsanitySotG** — `Cultist.getHalfDamagePercentage` → `0.0f`. Drops
  the Insanity "shield of the gone" half-damage perk entirely.
- **removeHateWarBonus** — `mayStartDoubleWarDamage` and `doubleWarDamage` →
  `false`. Drops the Hate war-damage doubling perk. *(If
  `enableMeditationAbilityCooldowns` is also on, that toggle's rewrite of
  `mayStartDoubleWarDamage` overrides this — last write wins.)*

### Passive scaling perks

- **insanitySpeedBonus** — replaces `Actions.getStaminaModiferFor` with a
  version that subtracts 2% stamina cost per level above 6 for Insanity
  cultists.
- **hateMovementBonus** — adds 1% movement speed per level above 2 for Hate
  cultists. Multiplied into `MovementScheme.getSpeedModifier` after vanilla.
- **scalingPowerStaminaBonus** — adds 5% stamina regen per level above 6
  for Power cultists. Patched into `CreatureStatus.modifyStamina` before the
  `usesNoStamina` short-circuit.
- **scalingKnowledgeSkillGain** — adds 5% skill gain per level above 6 for
  Knowledge cultists. Patched into `Skill.alterSkill(D,Z,F,Z,D)V` before the
  `levelElevenSkillgain` branch.
- **newMeditationBuffs** — pushes passive-buff icons to the client so the
  perks above show up in the player's spell-effect bar (Insanity sotg-gone
  marker, Knowledge skill-gain banner, Power stamina-bonus, Hate
  move-bonus). Inserts before vanilla's `Cultist.sendPassiveBuffs`.

### Tick timer + ability cooldowns

- **removeMeditationTickTimer** — strips the artificial `getLastMeditated`
  gate from `Cults.meditate`. Vanilla's per-skill `tickTime` is the only
  remaining gate (configure that via the `skill` submod).
- **enableMeditationAbilityCooldowns** — rewrites all 11 `Cultist.may*()`
  ability gates to consult the per-ability cooldowns in the properties
  file. Defaults are 18 hours (`64800000` ms) across the board, matching
  upstream.
  - Love: `loveRefreshCooldown` (refresh, lvl 4+),
    `loveEnchantNatureCooldown` (enchant nature, lvl 7+),
    `loveLoveEffectCooldown` (love effect, lvl 9+)
  - Hate: `hateWarDamageCooldown` (lvl 7+), `hateStructureDamageCooldown`
    (lvl 4+), `hateFearCooldown` (lvl 9+)
  - Power: `powerElementalImmunityCooldown` (lvl 9+),
    `powerEruptFreezeCooldown` (lvl 7+), `powerIgnoreTrapsCooldown` (lvl 4+)
  - Knowledge: `knowledgeInfoCreatureCooldown` (lvl 4+),
    `knowledgeInfoTileCooldown` (lvl 7+)

## Provenance

Ported from `mod.sin.wyvern.MeditationPerks` (WyvernMods). Toggle names
preserved one-for-one with upstream so existing operators can copy values
from `WyvernMods.properties` without renaming. Upstream's
`enableMeditationModule` master flag is omitted — under our split, the
presence of the jar *is* the master.

`mod.sin.lib.Util` (`setBodyDeclared` / `insertBeforeDeclared` / etc.) is
re-implemented locally on top of raw `javassist.CtClass` + `ExprEditor` so
this submod doesn't carry a compileOnly dependency on sindusklibrary.

The framework's `SkillGainMultiplierEvent` (used by the `skill` submod for
its hybrid curve) sits one level above the patch site here —
`scalingKnowledgeSkillGain` runs *inside* `alterSkill`, after the
multiplier event has already chosen a skill-gain rate. The two stack
multiplicatively when both are enabled.
