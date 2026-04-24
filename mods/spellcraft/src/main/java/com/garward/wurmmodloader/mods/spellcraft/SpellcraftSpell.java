package com.garward.wurmmodloader.mods.spellcraft;

import com.wurmonline.server.spells.Spell;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Enum-based spell registry for all 12 Spellcraft custom spells.
 * Each spell has configuration loaded from properties file.
 */
public enum SpellcraftSpell {
    HARDEN("Harden", "harden", (byte) 110,
           "will take reduced damage when used.", "reduces damage taken from use"),
    PHASING("Phasing", "phasing", (byte) 111,
            "will phase through shields.", "phases through shields"),
    REPLENISH("Replenish", "replenish", (byte) 112,
               "will automatically refill when used.", "automatically refills containers"),
    EXPAND("Expand", "expand", (byte) 113,
           "has a larger capacity.", "increases capacity"),
    EFFICIENCY("Efficiency", "efficiency", (byte) 114,
               "will make actions easier.", "reduces difficulty"),
    QUARRY("Quarry", "quarry", (byte) 115,
           "will improve mining.", "improves mining"),
    PROWESS("Prowess", "prowess", (byte) 116,
            "improves combat ability.", "increases combat rating"),
    INDUSTRY("Industry", "industry", (byte) 117,
             "makes work easier.", "reduces difficulty"),
    ENDURANCE("Endurance", "endurance", (byte) 118,
              "reduces stamina drain.", "reduces stamina cost"),
    ACUITY("Acuity", "acuity", (byte) 119,
           "reduces favor cost.", "reduces spell favor cost"),
    TITANFORGED("Titanforged", "titanforged", (byte) 120,
                "is titanforged.", "multi-buff enchantment"),
    LABOURING_SPIRIT("Labouring Spirit", "labouringSpirit", (byte) 121,
                     "has labouring spirit.", "placeholder enchantment");

    private static final Logger logger = Logger.getLogger(SpellcraftSpell.class.getName());

    // Spell metadata
    private final String displayName;
    private final String propertyKey;
    private final byte enchantId;
    private final String effectDesc;
    private final String description;

    // Runtime spell instance
    private Spell spell;

    // Configuration (loaded from properties)
    private boolean enabled = true;
    private int castTime = 5;
    private int cost = 30;
    private int difficulty = 30;
    private int faith = 20;
    private int cooldown = 180;
    private List<String> gods = Arrays.asList("-1"); // -1 = all gods

    SpellcraftSpell(String displayName, String propertyKey, byte enchantId, String effectDesc, String description) {
        this.displayName = displayName;
        this.propertyKey = propertyKey;
        this.enchantId = enchantId;
        this.effectDesc = effectDesc;
        this.description = description;
    }

    /**
     * Load configuration for all spells from properties file.
     */
    public static void loadConfiguration(Properties properties) {
        for (SpellcraftSpell spellEnum : values()) {
            spellEnum.loadConfig(properties);
        }
    }

    /**
     * Get spell enum by display name.
     *
     * @param name The spell display name (case-insensitive)
     * @return The spell enum, or null if not found
     */
    public static SpellcraftSpell getByName(String name) {
        if (name == null) return null;
        for (SpellcraftSpell spell : values()) {
            if (spell.displayName.equalsIgnoreCase(name)) {
                return spell;
            }
        }
        return null;
    }

    /**
     * Load individual spell configuration from properties.
     */
    private void loadConfig(Properties properties) {
        String prefix = "spell." + propertyKey + ".";

        // Parse configuration with defaults
        enabled = getBoolean(properties, "spellEnable" + capitalize(propertyKey), true);

        if (enabled) {
            castTime = getInt(properties, prefix + "casttime", castTime);
            cost = getInt(properties, prefix + "cost", cost);
            difficulty = getInt(properties, prefix + "difficulty", difficulty);
            faith = getInt(properties, prefix + "faith", faith);
            cooldown = getInt(properties, prefix + "cooldown", cooldown);

            String godsStr = properties.getProperty(prefix + "gods", "-1");
            gods = Arrays.stream(godsStr.split(","))
                         .map(String::trim)
                         .collect(Collectors.toList());
        }

        logger.log(Level.FINE, "Loaded config for " + displayName + ": enabled=" + enabled +
                              ", cost=" + cost + ", faith=" + faith);
    }

    /**
     * Helper: Get integer from properties with default.
     */
    private int getInt(Properties props, String key, int defaultValue) {
        try {
            String value = props.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid integer for " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Helper: Get boolean from properties with default.
     */
    private boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Helper: Capitalize first letter.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    // Getters
    public String getName() { return displayName; }
    public String getPropertyKey() { return propertyKey; }
    public byte getEnchant() { return enchantId; }
    public String getEffectDesc() { return effectDesc; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public int getCastTime() { return castTime; }
    public int getCost() { return cost; }
    public int getDifficulty() { return difficulty; }
    public int getFaith() { return faith; }
    public int getCooldown() { return cooldown; }
    public List<String> getGods() { return gods; }

    // Spell instance management
    public Spell getSpell() { return spell; }
    public void setSpell(Spell spell) { this.spell = spell; }
}
