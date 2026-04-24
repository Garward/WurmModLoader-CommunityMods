# Armoury Mod - Analysis & API Opportunities

## What Armoury Does

Armoury is a **comprehensive combat balance mod** that tweaks armor, weapons, shields, and materials. It's already modernized and uses WurmModLoader's event system internally.

### Core Features

#### 1. **Armor System Modifications**
- **Damage Reduction (DR)**: Configurable base DR for each armor type (cloth, leather, studded, chain, plate, drake, dragonscale)
- **Effectiveness vs Wound Types**: Armor can be more/less effective against specific damage (slash, pierce, crush, burn, cold, acid, etc.)
- **Glance Rates**: Per-armor-type glance chance against different wound types
- **Movement Penalties**: Configurable movement speed reduction per armor piece
- **Limit Factors**: Controls how armor scales with quality

**Example Config:**
```properties
armourDamageReduction-4:plate,0.65  # Plate armor has 65% DR at 100QL
armourEffectiveness-1:drake;cold,1.05  # Drake armor +5% more DR vs cold
armourGlanceRate-1:plate;slash,0.40  # 40% glance rate against slash
```

#### 2. **Material System Modifications**
Affects ALL items made from materials (not just combat gear):

**Armor Materials:**
- Damage reduction modifiers
- Movement speed modifiers

**Weapon Materials:**
- Damage multipliers (e.g., glimmersteel does more damage)
- Speed multipliers (e.g., seryll swings faster)
- Parry bonuses
- Armor damage (how well it damages enemy armor)

**General Item Materials:**
- Damage modifier (durability)
- Decay rate
- Creation/improve difficulty bonuses
- Shatter resistance
- Lockpick effectiveness
- Anchor effectiveness
- Pendulum power
- Repair speed
- Bash damage (for breaking locks/fences)
- Spell effect power
- Action speed (affects all actions with the tool)

**Example Configs:**
```properties
materialWeaponDamage-1:glimmersteel,1.10  # +10% damage
materialWeaponSpeed-1:seryll,0.90  # 10% faster swing
materialMovementModifier-1:steel,1.05  # +5% heavier
materialRepairSpeed-1:moonmetal,0.75  # 25% faster repairs
```

#### 3. **Weapon Tweaks**
Per-weapon template modifications:
- Base damage
- Swing speed
- Critical hit chance
- Reach
- Weight group (dual wield compatibility)
- Parry percentage
- Skill penalty

#### 4. **Combat System Tweaks**
- **Minimum swing time**: Prevents extremely fast weapon speeds
- **Rarity bonuses**: Rare+ items reduce swing time
- **Fixed weapon timer reset**: Prevents swing timer exploits
- **Better dual wield**: Experimental dual wielding improvements (HIGHLY EXPERIMENTAL)

#### 5. **Shield Enhancements**
- Shield damage enchants (e.g., CoC on shields increases bash damage)
- Shield speed enchants (affects shield bash speed)

---

## Current Integration

Armoury **already uses framework events** internally:
- `MaterialBonusEvent`
- `MaterialDamageModifierEvent`
- `ShieldCheckEvent`, `ShieldDamageEvent`
- `WeaponStatQueryEvent`
- `CombatSwingSpeedEvent`

But it **doesn't expose any public API** for other mods to:
1. Query armor stats (DR, glance rates, effectiveness)
2. Query material bonuses
3. Modify these values dynamically

---

## Recommended API Hooks (Forge-style Events)

### 1. `armoury:armor_damage_reduction` (ModQueryEvent)

**Purpose**: Let mods query or modify armor DR calculations

**Input:**
- `playerId` (long) - Player being hit
- `armorType` (byte) - Armor type (1=leather, 3=chain, 4=plate, etc.)
- `woundType` (byte) - Damage type (0=crush, 1=slash, 2=pierce, etc.)
- `quality` (float) - Armor quality level
- `material` (byte) - Armor material

**Output:**
- `baseDR` (float) - Base damage reduction (0.65 = 65%)
- `effectiveness` (float) - Effectiveness multiplier (1.05 = +5% more DR)
- `finalDR` (float) - Calculated final DR

**Use Case:**
- PowerScaling could increase DR based on player level
- Other mods could add set bonuses (wearing full plate = +10% DR)
- Dynamic armor effectiveness based on conditions

**Example:**
```java
@SubscribeEvent
public void onArmorDR(ModQueryEvent event) {
    if (!event.getEventType().equals("armoury:armor_damage_reduction")) return;

    long playerId = event.getLong("playerId");
    int powerLevel = getPowerLevel(playerId);

    // +5% DR per power level
    float currentDR = event.getFloat("finalDR");
    float bonus = powerLevel * 0.05f;
    event.set("finalDR", Math.min(0.90f, currentDR + bonus)); // Cap at 90%
    event.setHandled(true);
}
```

---

### 2. `armoury:armor_glance_rate` (ModQueryEvent)

**Purpose**: Let mods query or modify glance rate calculations

**Input:**
- `playerId` (long)
- `armorType` (byte)
- `woundType` (byte)
- `quality` (float)
- `material` (byte)

**Output:**
- `baseGlance` (float) - Base glance rate (0.40 = 40%)
- `finalGlance` (float) - Final glance rate

**Use Case:**
- Buff/debuff mods can modify glance rates
- Stance system: defensive stance = +10% glance rate

---

### 3. `armoury:material_weapon_bonus` (ModQueryEvent)

**Purpose**: Let mods query weapon material bonuses

**Input:**
- `weaponId` (long) - Weapon item ID
- `material` (byte) - Weapon material

**Output:**
- `damageMultiplier` (double) - Damage multiplier from material
- `speedMultiplier` (float) - Speed multiplier from material
- `parryBonus` (float) - Parry bonus from material
- `armorDamage` (double) - Armor damage multiplier

**Use Case:**
- DUSKombat could use this to show accurate material bonuses in tooltips
- Enchantment mods could stack with material bonuses

---

### 4. `armoury:armor_set_check` (ModQueryEvent)

**Purpose**: Query what armor set a player is wearing (for set bonuses)

**Input:**
- `playerId` (long)

**Output:**
- `fullSetType` (byte) - Armor type if wearing full set, -1 if not
- `pieceCount` (Map<Byte, Integer>) - Count of pieces per armor type
- `armorPieces` (List<Long>) - List of equipped armor item IDs

**Use Case:**
- Set bonus mods: "Wearing full plate gives +15% damage"
- Visual effects: glow effect when wearing full set
- Achievements: "Wear full dragonscale armor"

---

### 5. `armoury:material_tool_bonus` (ModQueryEvent)

**Purpose**: Query general material bonuses for tools/items

**Input:**
- `itemId` (long)
- `material` (byte)
- `action` (int) - Action type being performed

**Output:**
- `actionSpeedModifier` (double) - How fast actions are with this material
- `durabilityModifier` (float) - How much damage item takes
- `difficultyModifier` (double) - Creation/improve difficulty

**Use Case:**
- Tooltips showing "This pickaxe is 15% faster due to glimmersteel"
- Crafting mods that scale with material quality

---

## Integration Examples

### PowerScaling + Armoury: Scaling Armor

```java
@SubscribeEvent
public void onArmorDR(ModQueryEvent event) {
    if (!event.getEventType().equals("armoury:armor_damage_reduction")) return;

    long playerId = event.getLong("playerId");
    int powerLevel = getPowerLevel(playerId);

    if (powerLevel == 0) return;

    // Scale armor with power: +2% DR per level, cap at +50% total
    float currentDR = event.getFloat("finalDR");
    float powerBonus = Math.min(0.50f, powerLevel * 0.02f);
    event.set("finalDR", Math.min(0.95f, currentDR * (1.0f + powerBonus)));
    event.setHandled(true);
}
```

### DUSKombat + Armoury: Material Damage Tooltips

```java
@SubscribeEvent
public void onDUSKombatTooltip(ModQueryEvent event) {
    if (!event.getEventType().equals("duskombat:item_tooltip")) return;
    if (!event.getBoolean("isWeapon")) return;

    long itemId = event.getLong("itemId");

    // Query Armoury for material bonuses
    ModQueryEvent materialQuery = new ModQueryEvent("armoury:material_weapon_bonus");
    materialQuery.set("weaponId", itemId);
    EventBus.getInstance().post(materialQuery);

    if (materialQuery.isHandled()) {
        double damageMult = materialQuery.getDouble("damageMultiplier");
        float speedMult = materialQuery.getFloat("speedMultiplier");

        List<String> lines = (List<String>) event.get("tooltipLines");
        if (lines == null) lines = new ArrayList<>();

        if (damageMult != 1.0) {
            lines.add(String.format("Material Bonus: %.0f%% damage", (damageMult - 1.0) * 100));
        }
        if (speedMult != 1.0) {
            lines.add(String.format("Material Speed: %.0f%%", (speedMult - 1.0) * 100));
        }

        event.set("tooltipLines", lines);
    }
}
```

### Set Bonus Mod

```java
@SubscribeEvent
public void onDamageCalc(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long attackerId = event.getLong("attackerId");

    // Check if wearing full armor set
    ModQueryEvent setCheck = new ModQueryEvent("armoury:armor_set_check");
    setCheck.set("playerId", attackerId);
    EventBus.getInstance().post(setCheck);

    if (setCheck.isHandled()) {
        byte setType = setCheck.getByte("fullSetType");

        if (setType == 4) { // Full plate
            double mult = (Double) event.get("damageMultiplier");
            event.set("damageMultiplier", mult * 1.15); // +15% damage
            event.setHandled(true);
        }
    }
}
```

---

## Priority for Implementation

**High Priority:**
1. `armoury:armor_damage_reduction` - Most commonly needed
2. `armoury:material_weapon_bonus` - Enables tooltip integration

**Medium Priority:**
3. `armoury:armor_glance_rate` - For advanced combat mods
4. `armoury:armor_set_check` - Enables set bonus mods

**Low Priority:**
5. `armoury:material_tool_bonus` - Nice to have for crafting mods

---

## Why This Matters

**Current State:**
- Armoury modifies armor/weapons internally
- Other mods can't see these modifications
- DUSKombat shows base damage, not material-modified damage
- No way to create set bonuses or dynamic armor scaling

**With API Events:**
- DUSKombat tooltips show accurate material bonuses
- PowerScaling can scale armor with power level
- Community can create set bonus mods
- Material effectiveness is transparent to all mods
- Crafting mods can show accurate material benefits

This would be the **second reference implementation** (after DUSKombat) of the Forge-style API pattern!
