# DUSKombat API Documentation

DUSKombat exposes a **Forge-style event API** that allows other mods to integrate with combat mechanics without hardcoded dependencies.

## Available Events

### 1. `duskombat:calculate_damage` (ModActionEvent)

Fired during damage calculation, allowing mods to modify final damage output.

**When**: Called in `DamageMethods.getDamage()` after base damage and all DUSKombat multipliers are applied.

**Input Data**:
- `attackerId` (long) - Attacker's wurm ID
- `defenderId` (long) - Defender's wurm ID
- `baseDamage` (int) - Base damage before mod modifications
- `damageType` (String) - "SLASH", "PIERCE", "CRUSH", "UNARMED", or "OTHER"
- `weaponId` (long) - Weapon item wurm ID
- `isBackstab` (boolean) - Whether this is a backstab attack

**Modifiable Values** (set these to modify damage):
- `damageMultiplier` (double) - Default: 1.0. Final damage multiplied by this
- `bonusDamage` (int) - Default: 0. Flat damage added after multiplier

**Example Usage**:
```java
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (event.getEventType().equals("duskombat:calculate_damage")) {
        long attackerId = event.getLong("attackerId");

        // Example: Add 10% damage per power level
        int powerLevel = getPowerLevel(attackerId);
        double multiplier = 1.0 + (powerLevel * 0.10);

        event.set("damageMultiplier", multiplier);
        event.setHandled(true);
    }
}
```

**Formula**:
```
finalDamage = (baseDamage * damageMultiplier) + bonusDamage
```

---

### 2. `duskombat:item_tooltip` (ModQueryEvent)

Fired when examining weapons/armor, allowing mods to add custom tooltip lines.

**When**: Called in `ItemInfo.handleExamine()` after DUSKombat's damage/armor stats are displayed.

**Input Data**:
- `itemId` (long) - Item's wurm ID
- `playerId` (long) - Player examining the item
- `isWeapon` (boolean) - Whether item is a weapon
- `isArmour` (boolean) - Whether item is armor
- `baseDamage` (int) - (Weapons only) Base damage value
- `damageType` (String) - (Weapons only) Damage type

**Output** (add custom lines):
- `tooltipLines` (List\<String\>) - List of strings to display

**Example Usage**:
```java
@SubscribeEvent
public void onTooltip(ModQueryEvent event) {
    if (event.getEventType().equals("duskombat:item_tooltip")) {
        long playerId = event.getLong("playerId");

        if (event.getBoolean("isWeapon")) {
            int baseDamage = event.getInt("baseDamage");
            int powerLevel = getPowerLevel(playerId);

            // Calculate bonus damage from power
            int bonusDamage = (int) (baseDamage * powerLevel * 0.10);

            // Add custom tooltip line
            List<String> lines = new ArrayList<>();
            lines.add(String.format("Power Bonus: +%d damage", bonusDamage));

            event.set("tooltipLines", lines);
            event.setHandled(true);
        }
    }
}
```

**Display**: Custom lines are shown in **yellow** below DUSKombat's stats.

---

## Integration Examples

### PowerScaling Integration

Shows damage scaling with power level:

```java
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long attackerId = event.getLong("attackerId");
    PowerLevel powerData = powerManager.getPlayerPowerLevel(attackerId);

    if (powerData == null) return;

    // 5% damage increase per power level
    double multiplier = 1.0 + (powerData.getLevel() * 0.05);
    event.set("damageMultiplier", multiplier);
    event.setHandled(true);
}

@SubscribeEvent
public void onTooltip(ModQueryEvent event) {
    if (!event.getEventType().equals("duskombat:item_tooltip")) return;
    if (!event.getBoolean("isWeapon")) return;

    long playerId = event.getLong("playerId");
    int baseDamage = event.getInt("baseDamage");
    PowerLevel powerData = powerManager.getPlayerPowerLevel(playerId);

    if (powerData == null) return;

    int bonusDamage = (int) (baseDamage * powerData.getLevel() * 0.05);

    List<String> lines = new ArrayList<>();
    lines.add(String.format("Power Bonus (Lvl %d): +%d damage",
        powerData.getLevel(), bonusDamage));

    event.set("tooltipLines", lines);
    event.setHandled(true);
}
```

### Armoury Integration

Applies armor reduction to damage:

```java
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    long defenderId = event.getLong("defenderId");
    String damageType = event.getString("damageType");

    // Get defender's armor value
    int armorValue = getPlayerArmorValue(defenderId, damageType);

    // Reduce damage by armor (capped at 75% reduction)
    double reduction = Math.min(0.75, armorValue / 100.0);
    double multiplier = 1.0 - reduction;

    event.set("damageMultiplier", multiplier);
    event.setHandled(true);
}
```

### Conditional Damage Mod

Only modifies specific damage types:

```java
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (!event.getEventType().equals("duskombat:calculate_damage")) return;

    String damageType = event.getString("damageType");

    // Bonus damage only for slashing weapons
    if (damageType.equals("SLASH")) {
        event.set("damageMultiplier", 1.25); // +25% slash damage
        event.setHandled(true);
    }
}
```

---

## Best Practices

1. **Always check event type first**:
   ```java
   if (!event.getEventType().equals("duskombat:calculate_damage")) return;
   ```

2. **Mark events as handled**:
   ```java
   event.setHandled(true);
   ```

3. **Don't override other mods' values** - Read current values and modify:
   ```java
   double current = (Double) event.get("damageMultiplier");
   event.set("damageMultiplier", current * 1.10); // Add 10% on top
   ```

4. **Handle missing data gracefully**:
   ```java
   if (!event.has("baseDamage")) return;
   ```

5. **Use cancellation for blocking damage** (if needed):
   ```java
   event.setCancelled(true);
   event.setCancelReason("Player is invulnerable");
   ```

---

## Migration from Hardcoded Dependencies

**Old way** (brittle, breaks with classloaders):
```java
// This fails at runtime due to classloader isolation!
import mod.piddagoras.duskombat.DamageMethods;
double damage = DamageMethods.getDamage(...);
```

**New way** (stable, works across mods):
```java
// Listen for DUSKombat's event and modify damage
@SubscribeEvent
public void onDamageCalculate(ModActionEvent event) {
    if (event.getEventType().equals("duskombat:calculate_damage")) {
        // Modify damage here
    }
}
```

---

## Questions?

Check the **WurmModLoader generic event documentation** for more on `ModQueryEvent` and `ModActionEvent`.

See `mods/powerscaling` for a complete working example of the event system.
