# Armoury API Documentation

Armoury exposes a **Forge-style event API** that allows other mods to query armor stats, material bonuses, and integrate with the combat balance system.

---

## Available Events

### 1. `armoury:armor_damage_reduction` (ModQueryEvent)

Query armor damage reduction (DR) values configured in Armoury.

**When to Use**: Get accurate DR values for tooltips, damage calculations, or UI displays.

**Input Data**:
- `armorType` (byte) - **Required**. Armor type ID:
  - `1` = Leather
  - `2` = Studded
  - `3` = Chain
  - `4` = Plate
  - `6` = Cloth
  - `9` = Drake
  - `10` = Dragonscale
- `woundType` (byte) - **Optional**. Wound/damage type (0-10):
  - `0` = Crush, `1` = Slash, `2` = Pierce, `3` = Bite
  - `4` = Burn, `5` = Poison, `6` = Infection, `7` = Water
  - `8` = Cold, `9` = Internal, `10` = Acid
- `material` (byte) - **Optional**. Material of armor for additional modifier

**Output Data**:
- `baseDR` (float) - Base damage reduction at 100QL (e.g., 0.65 = 65%)
- `effectiveness` (float) - Effectiveness vs wound type (e.g., 1.05 = +5% more effective)
- `finalDR` (float) - Final calculated DR including all modifiers

**Example Usage**:
```java
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long defenderId = event.getLong("defenderId");
    String damageType = event.getString("damageType");

    // Query Armoury for defender's armor DR
    ModQueryEvent armorQuery = new ModQueryEvent("armoury:armor_damage_reduction");
    armorQuery.set("armorType", (byte) 4); // Plate armor
    armorQuery.set("woundType", getWoundTypeByte(damageType)); // Convert "SLASH" -> 1
    armorQuery.set("material", (byte) 11); // Steel
    EventBus.getInstance().post(armorQuery);

    if (armorQuery.isHandled()) {
        float dr = armorQuery.getFloat("finalDR");

        // Reduce damage by DR
        double baseDamage = event.getInt("baseDamage");
        double reducedDamage = baseDamage * (1.0 - dr);

        event.set("finalDamage", (int) reducedDamage);
        event.setHandled(true);
    }
}
```

---

### 2. `armoury:armor_glance_rate` (ModQueryEvent)

Query armor glance rates (chance to completely avoid damage).

**Input Data**:
- `armorType` (byte) - **Required**. Armor type ID
- `woundType` (byte) - **Optional**. Wound type

**Output Data**:
- `baseGlance` (float) - Base glance rate (0.40 = 40% chance)
- `finalGlance` (float) - Final glance rate

**Example Usage**:
```java
ModQueryEvent glanceQuery = new ModQueryEvent("armoury:armor_glance_rate");
glanceQuery.set("armorType", (byte) 4); // Plate
glanceQuery.set("woundType", (byte) 1); // Slash
EventBus.getInstance().post(glanceQuery);

if (glanceQuery.isHandled()) {
    float glanceRate = glanceQuery.getFloat("finalGlance");

    // Roll for glance
    if (Math.random() < glanceRate) {
        // Attack glanced off armor!
        event.set("finalDamage", 0);
    }
}
```

---

### 3. `armoury:material_weapon_bonus` (ModQueryEvent)

Query weapon material bonuses (damage, speed, parry, armor damage).

**When to Use**: Display material bonuses in tooltips, calculate accurate weapon damage/speed.

**Input Data**:
- `material` (byte) - **Required**. Material type:
  - `7` = Steel, `8` = Brass, `9` = Bronze
  - `10` = Copper, `11` = Gold, `12` = Silver
  - `13` = Iron, `30` = Tin, `31` = Lead
  - `34` = Zinc, `38` = Adamantine, `39` = Glimmersteel
  - `40` = Electrum, `41` = Seryll
  - See `com.wurmonline.server.items.Materials` for full list
- `weaponId` (long) - **Optional**. Weapon item ID for context

**Output Data**:
- `damageMultiplier` (double) - Damage multiplier (1.10 = +10% damage)
- `speedMultiplier` (float) - Speed multiplier (0.90 = 10% faster)
- `parryBonus` (float) - Parry bonus (1.05 = +5% parry)
- `armorDamage` (double) - Armor damage multiplier (how well it damages enemy armor)

**Example Usage**:
```java
@SubscribeEvent
public void onDUSKombatTooltip(ModQueryEvent event) {
    if (!event.getEventType().equals("duskombat:item_tooltip")) return;
    if (!event.getBoolean("isWeapon")) return;

    // Get weapon material
    Item weapon = getWeaponFromEvent(event);
    byte material = weapon.getMaterial();

    // Query Armoury for material bonuses
    ModQueryEvent materialQuery = new ModQueryEvent("armoury:material_weapon_bonus");
    materialQuery.set("material", material);
    EventBus.getInstance().post(materialQuery);

    if (materialQuery.isHandled()) {
        double damageMult = materialQuery.getDouble("damageMultiplier");
        float speedMult = materialQuery.getFloat("speedMultiplier");
        float parryBonus = materialQuery.getFloat("parryBonus");

        List<String> lines = (List<String>) event.get("tooltipLines");
        if (lines == null) lines = new ArrayList<>();

        // Add material bonus tooltips
        if (damageMult != 1.0) {
            lines.add(String.format("Material: +%.0f%% damage", (damageMult - 1.0) * 100));
        }
        if (speedMult != 1.0) {
            lines.add(String.format("Material: %.0f%% swing speed", (1.0 - speedMult) * 100));
        }
        if (parryBonus != 1.0) {
            lines.add(String.format("Material: +%.0f%% parry", (parryBonus - 1.0) * 100));
        }

        event.set("tooltipLines", lines);
        event.setHandled(true);
    }
}
```

---

### 4. `armoury:armor_set_check` (ModQueryEvent)

Query what armor pieces a player is wearing (for set bonuses).

**⚠️ Note**: Currently returns placeholder data. Full implementation requires equipment slot API.

**Input Data**:
- `playerId` (long) - **Required**. Player wurm ID

**Output Data**:
- `fullSetType` (byte) - Armor type if wearing full set, `-1` if not
- `pieceCount` (Map<Byte, Integer>) - Count of pieces per armor type
- `armorPieces` (List<Long>) - List of equipped armor item IDs

**Example Usage** (when fully implemented):
```java
ModQueryEvent setQuery = new ModQueryEvent("armoury:armor_set_check");
setQuery.set("playerId", player.getWurmId());
EventBus.getInstance().post(setQuery);

if (setQuery.isHandled()) {
    byte fullSetType = (Byte) setQuery.get("fullSetType");

    if (fullSetType == 4) {
        // Player is wearing full plate armor!
        // Apply set bonus: +15% damage, +10% DR
        applySetBonus(player, "full_plate");
    }
}
```

---

### 5. `armoury:material_tool_bonus` (ModQueryEvent)

Query material bonuses for tools and non-weapon items.

**When to Use**: Display action speed bonuses, durability info, crafting difficulty.

**Input Data**:
- `material` (byte) - **Required**. Material type
- `itemId` (long) - **Optional**. Item ID for context
- `action` (int) - **Optional**. Action type being performed

**Output Data**:
- `actionSpeedModifier` (double) - Action speed (0.85 = 15% faster)
- `durabilityModifier` (float) - Durability/damage taken (0.90 = 10% more durable)
- `difficultyModifier` (double) - Crafting difficulty (1.05 = +5% harder to create)

**Example Usage**:
```java
// Query tool material bonuses
ModQueryEvent toolQuery = new ModQueryEvent("armoury:material_tool_bonus");
toolQuery.set("material", pickaxe.getMaterial());
toolQuery.set("itemId", pickaxe.getWurmId());
EventBus.getInstance().post(toolQuery);

if (toolQuery.isHandled()) {
    double speedMod = toolQuery.getDouble("actionSpeedModifier");

    // Display in tooltip
    if (speedMod != 1.0) {
        player.sendMessage(String.format("This pickaxe is %.0f%% faster due to %s",
            (1.0 - speedMod) * 100,
            getMaterialName(pickaxe.getMaterial())));
    }
}
```

---

## Material IDs Reference

Common material IDs from `com.wurmonline.server.items.Materials`:

| ID | Material | Notes |
|----|----------|-------|
| 7 | Steel | Baseline material |
| 8 | Brass | |
| 9 | Bronze | |
| 10 | Copper | |
| 11 | Gold | Soft, poor for weapons |
| 12 | Silver | |
| 13 | Iron | Weaker than steel |
| 30 | Tin | |
| 31 | Lead | Heavy |
| 34 | Zinc | |
| 38 | Adamantine | Rare, powerful |
| 39 | Glimmersteel | Moon metal, high damage |
| 40 | Electrum | |
| 41 | Seryll | Moon metal, fast speed |

---

## Armor Type IDs Reference

From `ArmourTypeRegistry`:

| ID | Type | DR Range | Notes |
|----|------|----------|-------|
| 1 | Leather | ~60% | Light, common |
| 2 | Studded | ~62.5% | Reinforced leather |
| 3 | Chain | ~62.5% | Metal links |
| 4 | Plate | ~65% | Heavy, best physical DR |
| 6 | Cloth | ~40% | Lightest, poorest DR |
| 9 | Drake | ~65% | Rare, dragon hide |
| 10 | Dragonscale | ~70% | Rarest, best overall |

---

## Wound Type IDs Reference

From `WoundTypeRegistry`:

| ID | Type | Category | Common Sources |
|----|------|----------|----------------|
| 0 | Crush | Physical | Mauls, hammers |
| 1 | Slash | Physical | Swords, axes |
| 2 | Pierce | Physical | Spears, arrows |
| 3 | Bite | Physical | Animals, creatures |
| 4 | Burn | Elemental | Fire, flaming aura |
| 5 | Poison | Other | Venom, poison |
| 6 | Infection | Other | Disease |
| 7 | Water | Other | Drowning |
| 8 | Cold | Elemental | Ice, frostbrand |
| 9 | Internal | Other | Internal damage |
| 10 | Acid | Elemental | Acid flask |

---

## Integration Examples

### PowerScaling + Armoury: Scaling Armor with Power Level

```java
@SubscribeEvent
public void onDUSKombatDamage(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long defenderId = event.getLong("defenderId");
    int powerLevel = getPowerLevel(defenderId);

    if (powerLevel == 0) return;

    // Query defender's armor DR from Armoury
    ModQueryEvent armorQuery = new ModQueryEvent("armoury:armor_damage_reduction");
    armorQuery.set("armorType", getPlayerArmorType(defenderId));
    armorQuery.set("woundType", getDamageTypeFromEvent(event));
    EventBus.getInstance().post(armorQuery);

    if (armorQuery.isHandled()) {
        float baseDR = armorQuery.getFloat("finalDR");

        // Scale DR with power: +2% per level, cap at 95% total
        float powerBonus = Math.min(0.50f, powerLevel * 0.02f);
        float scaledDR = Math.min(0.95f, baseDR * (1.0f + powerBonus));

        // Apply DR reduction to damage
        double baseDamage = event.getInt("baseDamage");
        double reducedDamage = baseDamage * (1.0 - scaledDR);

        event.set("bonusDamage", (int) (baseDamage - reducedDamage));
        event.setHandled(true);
    }
}
```

### DUSKombat + Armoury: Material Bonus Tooltips

```java
@SubscribeEvent
public void onDUSKombatTooltip(ModQueryEvent event) {
    if (!event.getEventType().equals("duskombat:item_tooltip")) return;
    if (!event.getBoolean("isWeapon")) return;

    Item weapon = getWeaponFromId(event.getLong("itemId"));

    // Query material bonuses from Armoury
    ModQueryEvent matQuery = new ModQueryEvent("armoury:material_weapon_bonus");
    matQuery.set("material", weapon.getMaterial());
    EventBus.getInstance().post(matQuery);

    if (matQuery.isHandled()) {
        double damageMult = matQuery.getDouble("damageMultiplier");
        float speedMult = matQuery.getFloat("speedMultiplier");

        List<String> lines = (List<String>) event.get("tooltipLines");
        if (lines == null) lines = new ArrayList<>();

        String materialName = Materials.convertMaterialByteIntoString(weapon.getMaterial());

        if (damageMult != 1.0) {
            lines.add(String.format("%s Material: +%.0f%% damage",
                materialName, (damageMult - 1.0) * 100));
        }
        if (speedMult != 1.0) {
            lines.add(String.format("%s Material: %.0f%% faster",
                materialName, (1.0 - speedMult) * 100));
        }

        event.set("tooltipLines", lines);
    }
}
```

### Set Bonus Mod Example

```java
@SubscribeEvent
public void onDamageCalc(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long attackerId = event.getLong("attackerId");

    // Check for full armor set
    ModQueryEvent setQuery = new ModQueryEvent("armoury:armor_set_check");
    setQuery.set("playerId", attackerId);
    EventBus.getInstance().post(setQuery);

    if (setQuery.isHandled()) {
        byte fullSet = (Byte) setQuery.get("fullSetType");

        switch (fullSet) {
            case 4: // Full Plate
                // Set bonus: +15% damage, +5% DR
                double mult = (Double) event.get("damageMultiplier");
                event.set("damageMultiplier", mult * 1.15);
                break;
            case 1: // Full Leather
                // Set bonus: +10% speed, +3% dodge
                // (would need additional event hooks)
                break;
            case 10: // Full Dragonscale
                // Set bonus: +20% all stats
                mult = (Double) event.get("damageMultiplier");
                event.set("damageMultiplier", mult * 1.20);
                break;
        }

        event.setHandled(true);
    }
}
```

---

## Best Practices

### 1. Always Check if Event is Handled

```java
ModQueryEvent query = new ModQueryEvent("armoury:material_weapon_bonus");
query.set("material", material);
EventBus.getInstance().post(query);

if (query.isHandled()) {
    // Armoury responded, safe to use values
    double damageMult = query.getDouble("damageMultiplier");
} else {
    // Armoury not loaded or material not configured
    // Use fallback values
    double damageMult = 1.0;
}
```

### 2. Provide Required Parameters

```java
// ✅ Good - provides required armorType
ModQueryEvent query = new ModQueryEvent("armoury:armor_damage_reduction");
query.set("armorType", (byte) 4);

// ❌ Bad - missing armorType
ModQueryEvent query = new ModQueryEvent("armoury:armor_damage_reduction");
query.set("woundType", (byte) 1); // No armorType!
```

### 3. Handle Optional vs Required Parameters

```java
// Material is required
if (!event.has("material")) {
    logger.warning("material parameter is required!");
    return;
}

byte material = ((Number) event.get("material")).byteValue();
```

### 4. Use Type-Safe Getters with Fallbacks

```java
// Safe casting with defaults
byte armorType = event.get("armorType") != null
    ? ((Number) event.get("armorType")).byteValue()
    : -1;

if (armorType == -1) {
    // Invalid armor type
    return;
}
```

### 5. Document Material/Armor/Wound IDs

Always reference this API guide or the registry classes:
- `com.wurmonline.server.items.Materials`
- `com.garward.wurmmodloader.api.support.ArmourTypeRegistry`
- `com.garward.wurmmodloader.api.support.WoundTypeRegistry`

---

## Configuration Reference

Armoury's behavior is configured via `armoury.properties`. Here are some examples:

```properties
# Armor damage reduction (at 100QL)
armourDamageReduction-4:plate,0.65  # Plate has 65% DR

# Armor effectiveness vs wound types
armourEffectiveness-1:drake;cold,1.05  # Drake +5% vs cold

# Material weapon damage bonuses
materialWeaponDamage-1:glimmersteel,1.10  # Glimmersteel +10% damage
materialWeaponSpeed-1:seryll,0.90  # Seryll 10% faster

# Material tool bonuses
materialActionSpeedModifier-1:glimmersteel,0.85  # 15% faster actions
```

The API exposes these configured values to other mods!

---

## FAQ

**Q: How do I get a player's current armor type?**
A: You'll need to access equipped armor items and check their armor type. This requires item/equipment API (not yet available in generic events).

**Q: Can I modify Armoury's values dynamically?**
A: Not through these events. These are **query events** (read-only). To modify values, you'd need to adjust Armoury's config or implement your own combat calculation override.

**Q: Why does `armoury:armor_set_check` return placeholder data?**
A: It requires equipment slot access which isn't exposed through generic events yet. It's included for future extensibility.

**Q: How do I convert damage types between DUSKombat and Armoury?**
A: Use a mapping function:
```java
private byte getWoundTypeByte(String damageType) {
    switch (damageType) {
        case "SLASH": return 1;
        case "PIERCE": return 2;
        case "CRUSH": return 0;
        case "UNARMED": return 3; // Bite
        default: return 0; // Crush as fallback
    }
}
```

---

## See Also

- **DUSKombat API** - Damage calculation and tooltip events
- **PowerScaling API** - Power level queries and spending
- **WurmModLoader Event System** - Base ModQueryEvent/ModActionEvent documentation

---

For questions or suggestions, check the WurmModLoader documentation or community forums!
