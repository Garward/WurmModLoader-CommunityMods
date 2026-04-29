# WyvernMods → framework: item gaps surfaced by the #44 Item port

The vanilla template-tweak slice of upstream `ItemMod` (`modifyItems()`
+ `onServerStarted()` lockpick removal) was folded into `mods/miscchanges/`
rather than getting its own jar — it's a thin grab-bag of unrelated
toggles, shaped exactly like the rest of miscchanges. The custom-content
slice of upstream `ItemMod` belongs in downstream submods (#38 Titan,
#45 Caches, #48 Crystals, #54 Combat residuals). This doc captures what
the framework already gives those downstream submods and what they'll
have to provide themselves.

## Already in the framework

| Need | Where |
| --- | --- |
| Register a new `ItemTemplate` (basic items) | `com.garward.wurmmodloader.modsupport.ItemTemplateBuilder` (modern `ResourceLocation` constructor + legacy `String` constructor; both build & register) |
| Hook the right time to register | `ItemTemplatesCreatedEvent` (fires after vanilla `Items.createItems()`) |
| Reflection on private fields | `com.garward.wurmmodloader.modloader.ReflectionUtil.{getField, setPrivateField}` |
| Custom item model name | `ModItems.addModelNameProvider(int templateId, ModelNameProvider)` |
| Item examine string customisation | `ItemExamineEvent` |
| Item enchant-string customisation | `ItemEnchantmentStringsEvent` |
| Weapon stat customisation (per-stat) | `WeaponStatQueryEvent` |

## Missing — submods will have to type-import vanilla classes

Per `feedback_mods_type_imports.md` this is allowed for handler code,
but it's not idiomatic and means the downstream mod boilerplate
duplicates:

### 1. Custom `Weapon` registration

Upstream pattern:

```java
new Weapon(BattleYoyo.templateId, 6.85f, 3.75f, 0.012f, 2, 2, 0.0f, 0d);
```

This constructor mutates static state inside
`com.wurmonline.server.combat.Weapon` to register a non-vanilla weapon.
There is no framework helper. Submods that ship custom weapons (Titan
weaponry, Eviscerator, club/knuckles/warhammer/yoyo, Glimmerscale +
Spectral set members that have weapon stats) will each
`import com.wurmonline.server.combat.Weapon;` and call the constructor.

**Possible promotion:** add
`com.garward.wurmmodloader.modsupport.combat.WeaponBuilder` mirroring
the constructor with named setters and a `register()` terminal. Defer
until ≥3 submod consumers (rule of three) — currently only #54 Combat
residuals.

### 2. Custom `ArmourTemplate` registration

Upstream pattern:

```java
new ArmourTemplate(SpectralBoot.templateId,
                   ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON, 0.002f);
```

Same shape — constructor with side effects on static armour table.
Two upstream consumers (Spectral set + Glimmerscale set) both live in
the same conceptual "Wyvern armour" submod, so the rule-of-three
threshold won't be hit by porting alone. Skip promotion; let the armour
submod type-import `ArmourTemplate` directly.

### 3. `CreationEntry` skill-requirement helper

Pattern duplicated by upstream and by anyone who wants to drop a skill
gate from a recipe:

```java
ReflectionUtil.setPrivateField(entry,
    ReflectionUtil.getField(entry.getClass(), "hasMinimumSkillRequirement"),
    false);
ReflectionUtil.setPrivateField(entry,
    ReflectionUtil.getField(entry.getClass(), "minimumSkill"), 0.0);
```

This now lives in `mods/miscchanges/CreationTweaks`; the same trick
shows up in upstream `KingdomTemplates` lockpick branch and a few
joedobo27 recipe mods. **Promote** to a small helper —
`com.garward.wurmmodloader.modsupport.creation.CreationEntries.setSkillRequirement(int templateId, double minSkill)`
with a `clearSkillRequirement(int templateId)` overload — when the
third caller materialises (likely #54 Combat residuals or a future
recipe-tweak port).

### 4. `ItemTemplate.fragmentAmount` setter

Upstream's `setFragments(int templateId, int fragmentCount)` reflection
helper is private to `ItemMod`. We duplicated it in
`mods/miscchanges/ItemTweaks`. `adjustStatueFragmentCount` is the only
vanilla consumer; #45 Caches will be the second
(`useCustomCacheFragments` branch in upstream). **Promote** to a
one-liner on `ItemTemplateBuilder` — or as a static
`ItemTemplates.setFragmentAmount(int templateId, int count)` — at the
caches port.

### 5. `ItemTemplate.volume` recompute helper

`reduceVolume` in `mods/miscchanges/ItemTweaks` is one mod-specific
use case. No upstream duplication of *this exact pattern* outside the
log/kindling tweak. Skip promotion.

### 6. VehicleBehaviour permissions list extension

Upstream `ItemMod.registerPermissionsHook` proxies
`VehicleBehaviour.getVehicleBehaviours` and inserts MassStorageUnit
"Manage" / "History" entries when the player has permission. This is
a per-template permissions injection — belongs to whichever submod
ships MassStorageUnit (likely #46 Mastercraft).
**No framework gap** — `HookManager.registerHook` works, and
`com.wurmonline.server.behaviours.ActionEntry` is the right type to
import directly.

## Summary

The vanilla item-template tweaks (now in `mods/miscchanges/`) didn't
need any new framework primitives. But three small reflection helpers
(`Weapon` builder, `CreationEntry` skill setter,
`ItemTemplate.fragmentAmount` setter) will be duplicated across
downstream submods, and we should promote them as the third caller of
each appears — not pre-emptively.

There's deliberately no `mods/item/` jar and no `wyvern-shared` library
mod. Cross-submod ID lookups go through the framework's `Registries.ITEMS`
+ `ResourceLocation` — that's what it's there for.
