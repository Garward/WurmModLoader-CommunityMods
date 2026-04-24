package com.garward.wurmmodloader.mods.spellcraft;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.ServerStartedListener;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.ModQueryEvent;
import com.garward.wurmmodloader.api.events.ModActionEvent;
import com.garward.wurmmodloader.core.event.EventBus;
import com.garward.wurmmodloader.api.events.item.ItemDamageEvent;
import com.garward.wurmmodloader.api.events.item.ContainerVolumeEvent;
import com.garward.wurmmodloader.api.events.skill.SkillDifficultyEvent;
import com.garward.wurmmodloader.api.events.creature.StaminaCostEvent;
import com.garward.wurmmodloader.api.events.spell.SpellFavorCostEvent;
import com.garward.wurmmodloader.api.events.combat.CombatRatingEvent;
import com.garward.wurmmodloader.api.events.combat.shield.ShieldCheckEvent;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.spells.*;

/**
 * Spellcraft Mod - Comprehensive spell system overhaul
 *
 * Features:
 * - 12 custom enchantable spells
 * - Faith system overhaul (max faith 200, improved prayer/favor)
 * - Spell modifications and deity customization
 * - Enchant grouping and decay systems
 * - Damage modifier improvements
 * - Statuette quality/rarity bonuses
 *
 * @author Sindusk (original), Garward (port to WurmModLoader)
 * @version 2.0.0
 */
public class SpellcraftMod implements WurmServerMod, Configurable, ServerStartedListener, Initable, PreInitable {

    private static final Logger logger = Logger.getLogger(SpellcraftMod.class.getName());

    // Faith & Prayer System
    private int maximumPlayerFaith = 200;
    private int priestFaithRequirement = 10;
    private boolean hourlyPrayer = true;
    private boolean scalePrayerGains = true;
    private boolean newFavorRegen = true;

    // Enchant & Spell Systems
    private boolean useNewDamageModifier = true;
    private boolean improvedEnchantGrouping = true;
    private boolean statuetteTweaks = true;
    private boolean onlyShowValidSpells = true;
    private boolean allSpellsGamemasters = true;
    private boolean crossFaithLinking = true;
    private boolean fixHighPowerEnchants = true;
    private boolean enableEnchantDecay = false;

    // Enchant Decay
    private float enchantDecayRate = 1.0f;
    private float enchantDecayMinimum = 10.0f;

    @Override
    public void configure(Properties properties) {
        // Load spell configuration first
        SpellcraftSpell.loadConfiguration(properties);

        // Faith & Prayer
        maximumPlayerFaith = getInt(properties, "maximumPlayerFaith", 200);
        priestFaithRequirement = getInt(properties, "priestFaithRequirement", 10);
        hourlyPrayer = getBoolean(properties, "hourlyPrayer", true);
        scalePrayerGains = getBoolean(properties, "scalePrayerGains", true);
        newFavorRegen = getBoolean(properties, "newFavorRegen", true);

        // Enchant & Spell Systems
        useNewDamageModifier = getBoolean(properties, "useNewDamageModifier", true);
        improvedEnchantGrouping = getBoolean(properties, "improvedEnchantGrouping", true);
        statuetteTweaks = getBoolean(properties, "statuetteTweaks", true);
        onlyShowValidSpells = getBoolean(properties, "onlyShowValidSpells", true);
        allSpellsGamemasters = getBoolean(properties, "allSpellsGamemasters", true);
        crossFaithLinking = getBoolean(properties, "crossFaithLinking", true);
        fixHighPowerEnchants = getBoolean(properties, "fixHighPowerEnchants", true);
        enableEnchantDecay = getBoolean(properties, "enableEnchantDecay", false);

        // Enchant Decay
        enchantDecayRate = getFloat(properties, "enchantDecayRate", 1.0f);
        enchantDecayMinimum = getFloat(properties, "enchantDecayMinimum", 10.0f);

        logger.log(Level.INFO, "Spellcraft Configuration:");
        logger.log(Level.INFO, "  Max Faith: " + maximumPlayerFaith);
        logger.log(Level.INFO, "  Priest Requirement: " + priestFaithRequirement);
        logger.log(Level.INFO, "  Hourly Prayer: " + hourlyPrayer);
        logger.log(Level.INFO, "  New Favor Regen: " + newFavorRegen);
        logger.log(Level.INFO, "  New Damage Modifier: " + useNewDamageModifier);
        logger.log(Level.INFO, "  Enchant Decay: " + enableEnchantDecay);
    }

    @Override
    public void preInit() {
        logger.log(Level.INFO, "Spellcraft: PreInit - registering bytecode patches");
        // TODO: Register bytecode patches via framework
    }

    @Override
    public void init() {
        logger.log(Level.INFO, "Spellcraft: Init - event handlers will be automatically registered");
        // Event handlers are automatically registered by the framework via @SubscribeEvent annotations
    }

    @Override
    public void onServerStarted() {
        logger.log(Level.INFO, "Spellcraft: Server started, registering custom spells");

        // Register all 12 custom spells using reflection factory
        for (SpellcraftSpell spellEnum : SpellcraftSpell.values()) {
            if (!spellEnum.isEnabled()) {
                logger.log(Level.INFO, "Skipping disabled spell: " + spellEnum.getName());
                continue;
            }

            Spell spell = SpellFactory.createItemEnchantment(
                spellEnum.getName(),
                spellEnum.getCastTime(),
                spellEnum.getCost(),
                spellEnum.getDifficulty(),
                spellEnum.getFaith(),
                spellEnum.getCooldown(),
                spellEnum.getEnchant(),
                spellEnum.getEffectDesc(),
                spellEnum.getDescription(),
                true // targetItem
            );

            registerSpell(spellEnum, spell);
        }

        logger.log(Level.INFO, "Spellcraft: All custom spells registered successfully");
    }

    /**
     * Register a single spell and add it to appropriate deities.
     */
    private void registerSpell(SpellcraftSpell spellEnum, Spell spell) {
        if (!spellEnum.isEnabled()) {
            logger.log(Level.INFO, "Skipping disabled spell: " + spellEnum.getName());
            return;
        }

        // Store spell instance
        spellEnum.setSpell(spell);

        // Register with game's spell system
        try {
            Method addSpellMethod = Spells.class.getDeclaredMethod("addSpell", Spell.class);
            addSpellMethod.setAccessible(true);
            addSpellMethod.invoke(null, spell);
            logger.log(Level.INFO, "Registered spell: " + spellEnum.getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to register spell: " + spellEnum.getName(), e);
            return;
        }

        // Add to deities based on configuration
        for (Deity deity : Deities.getDeities()) {
            if (shouldAddSpellToDeity(spellEnum, deity)) {
                deity.addSpell(spell);
                logger.log(Level.FINE, "Added " + spellEnum.getName() + " to deity: " + deity.getName());
            }
        }
    }

    /**
     * Check if spell should be added to deity based on configuration.
     */
    private boolean shouldAddSpellToDeity(SpellcraftSpell spellEnum, Deity deity) {
        // -1 means all deities
        if (spellEnum.getGods().contains("-1")) {
            return true;
        }
        // Check if deity number is in the list
        return spellEnum.getGods().contains(String.valueOf(deity.getNumber()));
    }

    /**
     * Get integer from properties with default value.
     */
    private int getInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid integer for " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Get boolean from properties with default value.
     */
    private boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    /**
     * Get float from properties with default value.
     */
    private float getFloat(Properties properties, String key, float defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid float for " + key + ": " + value);
            return defaultValue;
        }
    }

    // Getters for other classes to access configuration
    public int getMaximumPlayerFaith() { return maximumPlayerFaith; }
    public int getPriestFaithRequirement() { return priestFaithRequirement; }
    public boolean isHourlyPrayer() { return hourlyPrayer; }
    public boolean isScalePrayerGains() { return scalePrayerGains; }
    public boolean isNewFavorRegen() { return newFavorRegen; }
    public boolean isUseNewDamageModifier() { return useNewDamageModifier; }
    public boolean isImprovedEnchantGrouping() { return improvedEnchantGrouping; }
    public boolean isStatuetteTweaks() { return statuetteTweaks; }
    public boolean isOnlyShowValidSpells() { return onlyShowValidSpells; }
    public boolean isAllSpellsGamemasters() { return allSpellsGamemasters; }
    public boolean isCrossFaithLinking() { return crossFaithLinking; }
    public boolean isFixHighPowerEnchants() { return fixHighPowerEnchants; }
    public boolean isEnchantDecayEnabled() { return enableEnchantDecay; }
    public float getEnchantDecayRate() { return enchantDecayRate; }
    public float getEnchantDecayMinimum() { return enchantDecayMinimum; }

    // ========== MOD API EVENT HANDLERS ==========

    /**
     * Handle queries from other mods about spellcraft configuration and state.
     *
     * Supported query types:
     * - spellcraft:is_enabled (spellName) -> boolean enabled
     * - spellcraft:get_spell_power (itemId, spellName) -> float power
     * - spellcraft:get_config (configKey) -> Object value
     * - spellcraft:list_spells -> String[] spellNames
     */
    @SubscribeEvent
    public void onModQuery(ModQueryEvent event) {
        try {
            if (event.getEventType().equals("spellcraft:is_enabled")) {
                String spellName = event.getString("spellName");
                SpellcraftSpell spell = SpellcraftSpell.getByName(spellName);
                if (spell != null) {
                    event.set("enabled", spell.isEnabled());
                    event.setHandled(true);
                }
            }
            else if (event.getEventType().equals("spellcraft:get_spell_power")) {
                long itemId = event.getLong("itemId");
                String spellName = event.getString("spellName");
                SpellcraftSpell spell = SpellcraftSpell.getByName(spellName);
                if (spell != null) {
                    try {
                        Item item = Items.getItem(itemId);
                        float power = item.getBonusForSpellEffect(spell.getEnchant());
                        event.set("power", power);
                        event.setHandled(true);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error getting spell power for item " + itemId, e);
                    }
                }
            }
            else if (event.getEventType().equals("spellcraft:get_config")) {
                String configKey = event.getString("configKey");
                Object value = getConfigValue(configKey);
                if (value != null) {
                    event.set("value", value);
                    event.setHandled(true);
                }
            }
            else if (event.getEventType().equals("spellcraft:list_spells")) {
                String[] spellNames = new String[SpellcraftSpell.values().length];
                for (int i = 0; i < SpellcraftSpell.values().length; i++) {
                    spellNames[i] = SpellcraftSpell.values()[i].getName();
                }
                event.set("spellNames", spellNames);
                event.setHandled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling ModQueryEvent: " + event.getEventType(), e);
        }
    }

    /**
     * Get configuration value by key.
     */
    private Object getConfigValue(String key) {
        switch (key) {
            case "maximumPlayerFaith": return maximumPlayerFaith;
            case "priestFaithRequirement": return priestFaithRequirement;
            case "hourlyPrayer": return hourlyPrayer;
            case "scalePrayerGains": return scalePrayerGains;
            case "newFavorRegen": return newFavorRegen;
            case "useNewDamageModifier": return useNewDamageModifier;
            case "improvedEnchantGrouping": return improvedEnchantGrouping;
            case "statuetteTweaks": return statuetteTweaks;
            case "onlyShowValidSpells": return onlyShowValidSpells;
            case "allSpellsGamemasters": return allSpellsGamemasters;
            case "crossFaithLinking": return crossFaithLinking;
            case "fixHighPowerEnchants": return fixHighPowerEnchants;
            case "enableEnchantDecay": return enableEnchantDecay;
            case "enchantDecayRate": return enchantDecayRate;
            case "enchantDecayMinimum": return enchantDecayMinimum;
            default: return null;
        }
    }

    // ========== EVENT HANDLERS FOR SPELL EFFECTS ==========

    /**
     * Harden spell - Reduces item damage
     *
     * Fires ModActionEvent "spellcraft:harden_effect" to allow other mods to modify the effect.
     * Event data:
     *   IN: itemId (long), spellPower (float), originalDamage (float)
     *   OUT: effectMultiplier (float, default 1.0) - multiplies the damage reduction
     */
    @SubscribeEvent
    public void onItemDamage(ItemDamageEvent event) {
        try {
            if (!SpellcraftSpell.HARDEN.isEnabled()) return;

            Item item = Items.getItem(event.getItemId());
            float hardenPower = item.getBonusForSpellEffect(SpellcraftSpell.HARDEN.getEnchant());

            if (hardenPower > 0) {
                // Fire mod action event to allow other mods to modify the effect
                ModActionEvent modAction = new ModActionEvent("spellcraft:harden_effect");
                modAction.set("itemId", event.getItemId());
                modAction.set("spellPower", hardenPower);
                modAction.set("originalDamage", event.getOriginalDamage());
                modAction.set("effectMultiplier", 1.0f);
                EventBus.getInstance().post(modAction);

                // Apply damage reduction (potentially modified by other mods)
                float reduction = hardenPower / 100.0f;  // 100 power = 100% reduction
                float effectMultiplier = modAction.has("effectMultiplier") ?
                    (Float) modAction.get("effectMultiplier") : 1.0f;
                event.multiplyDamage(1.0f - (reduction * effectMultiplier));

                logger.log(Level.FINE, "Harden spell reduced damage on " + event.getItemName() +
                    " by " + (reduction * effectMultiplier * 100) + "% (power: " + hardenPower + ", multiplier: " + effectMultiplier + ")");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Harden spell effect", e);
        }
    }

    /**
     * Expand spell - Increases container capacity
     *
     * Fires ModActionEvent "spellcraft:expand_effect" to allow other mods to modify the effect.
     * Event data:
     *   IN: itemId (long), spellPower (float), volumeType (String)
     *   OUT: effectMultiplier (float, default 1.0) - multiplies the capacity increase
     */
    @SubscribeEvent
    public void onContainerVolume(ContainerVolumeEvent event) {
        try {
            if (!SpellcraftSpell.EXPAND.isEnabled()) return;

            Item item = Items.getItem(event.getItemId());
            float expandPower = item.getBonusForSpellEffect(SpellcraftSpell.EXPAND.getEnchant());

            if (expandPower > 0) {
                // Fire mod action event to allow other mods to modify the effect
                ModActionEvent modAction = new ModActionEvent("spellcraft:expand_effect");
                modAction.set("itemId", event.getItemId());
                modAction.set("spellPower", expandPower);
                modAction.set("volumeType", event.getType().name());
                modAction.set("effectMultiplier", 1.0f);
                EventBus.getInstance().post(modAction);

                // Apply capacity increase (potentially modified by other mods)
                float effectMultiplier = modAction.has("effectMultiplier") ?
                    (Float) modAction.get("effectMultiplier") : 1.0f;
                double multiplier = 1.0 + ((expandPower / 100.0) * effectMultiplier); // 100 power = 2x capacity
                event.multiplyValue(multiplier);

                logger.log(Level.FINE, "Expand spell increased " + event.getType() +
                    " for " + event.getItemName() + " by " + ((multiplier - 1.0) * 100) + "% (multiplier: " + effectMultiplier + ")");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Expand spell effect", e);
        }
    }

    /**
     * Efficiency/Industry spells - Reduce skill difficulty
     */
    @SubscribeEvent
    public void onSkillDifficulty(SkillDifficultyEvent event) {
        try {
            float reduction = 0.0f;

            // Check tool for Efficiency enchant
            if (SpellcraftSpell.EFFICIENCY.isEnabled() && event.getToolId() != -1) {
                try {
                    Item tool = Items.getItem(event.getToolId());
                    float efficiencyPower = tool.getBonusForSpellEffect(SpellcraftSpell.EFFICIENCY.getEnchant());
                    if (efficiencyPower > 0) {
                        reduction += efficiencyPower / 200.0; // 100 power = 50% reduction
                    }
                } catch (Exception ignored) {}
            }

            // Check worn jewelry for Industry enchant
            if (SpellcraftSpell.INDUSTRY.isEnabled()) {
                try {
                    Creature performer = Server.getInstance().getCreature(event.getPerformerId());
                    Item[] bodyItems = performer.getBody().getAllItems();
                    for (Item item : bodyItems) {
                        if (item.isEnchantableJewelry()) {
                            float industryPower = item.getBonusForSpellEffect(SpellcraftSpell.INDUSTRY.getEnchant());
                            if (industryPower > 0) {
                                reduction += industryPower / 200.0; // 100 power = 50% reduction
                                break; // Only one jewelry bonus
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (reduction > 0) {
                event.multiplyDifficulty(1.0 - reduction);
                logger.log(Level.FINE, "Reduced skill difficulty by " + (reduction * 100) + "%");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Efficiency/Industry spell effect", e);
        }
    }

    /**
     * Endurance spell - Reduces stamina cost
     */
    @SubscribeEvent
    public void onStaminaCost(StaminaCostEvent event) {
        try {
            if (!SpellcraftSpell.ENDURANCE.isEnabled()) return;

            Creature creature = Server.getInstance().getCreature(event.getCreatureId());
            Item[] bodyItems = creature.getBody().getAllItems();

            float endurancePower = 0.0f;
            for (Item item : bodyItems) {
                float power = item.getBonusForSpellEffect(SpellcraftSpell.ENDURANCE.getEnchant());
                if (power > endurancePower) {
                    endurancePower = power; // Use highest power
                }
            }

            if (endurancePower > 0) {
                // Reduce stamina cost
                double reduction = endurancePower / 100.0; // 100 power = 100% reduction
                event.multiplyCost(1.0 - reduction);

                logger.log(Level.FINE, "Endurance spell reduced stamina cost by " +
                    (reduction * 100) + "%");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Endurance spell effect", e);
        }
    }

    /**
     * Acuity spell - Reduces spell favor cost
     */
    @SubscribeEvent
    public void onSpellFavorCost(SpellFavorCostEvent event) {
        try {
            if (!SpellcraftSpell.ACUITY.isEnabled()) return;

            Creature caster = Server.getInstance().getCreature(event.getCasterId());
            Item[] bodyItems = caster.getBody().getAllItems();

            float acuityPower = 0.0f;
            for (Item item : bodyItems) {
                if (item.isEnchantableJewelry()) {
                    float power = item.getBonusForSpellEffect(SpellcraftSpell.ACUITY.getEnchant());
                    if (power > acuityPower) {
                        acuityPower = power; // Use highest power
                    }
                }
            }

            if (acuityPower > 0) {
                // Reduce favor cost
                double reduction = acuityPower / 200.0; // 100 power = 50% reduction
                event.multiplyCost(1.0 - reduction);

                logger.log(Level.FINE, "Acuity spell reduced favor cost by " +
                    (reduction * 100) + "%");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Acuity spell effect", e);
        }
    }

    /**
     * Prowess spell - Increases combat rating
     *
     * Fires ModActionEvent "spellcraft:prowess_effect" to allow other mods to modify the effect.
     * Event data:
     *   IN: creatureId (long), spellPower (float)
     *   OUT: effectMultiplier (float, default 1.0) - multiplies the combat rating bonus
     */
    @SubscribeEvent
    public void onCombatRating(CombatRatingEvent event) {
        try {
            if (!SpellcraftSpell.PROWESS.isEnabled()) return;

            Creature creature = Server.getInstance().getCreature(event.getCreatureId());
            Item[] bodyItems = creature.getBody().getAllItems();

            float prowessPower = 0.0f;
            for (Item item : bodyItems) {
                if (item.isEnchantableJewelry()) {
                    float power = item.getBonusForSpellEffect(SpellcraftSpell.PROWESS.getEnchant());
                    if (power > prowessPower) {
                        prowessPower = power; // Use highest power
                    }
                }
            }

            if (prowessPower > 0) {
                // Fire mod action event to allow other mods to modify the effect
                ModActionEvent modAction = new ModActionEvent("spellcraft:prowess_effect");
                modAction.set("creatureId", event.getCreatureId());
                modAction.set("spellPower", prowessPower);
                modAction.set("effectMultiplier", 1.0f);
                EventBus.getInstance().post(modAction);

                // Apply combat rating bonus (potentially modified by other mods)
                float effectMultiplier = modAction.has("effectMultiplier") ?
                    (Float) modAction.get("effectMultiplier") : 1.0f;
                float ratingBonus = (prowessPower / 10.0f) * effectMultiplier; // 100 power = +10 CR
                event.addRating(ratingBonus);

                logger.log(Level.FINE, "Prowess spell added " + ratingBonus +
                    " to combat rating (power: " + prowessPower + ", multiplier: " + effectMultiplier + ")");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Prowess spell effect", e);
        }
    }

    /**
     * Phasing spell - Makes shields less effective against enchanted weapons
     */
    @SubscribeEvent
    public void onShieldCheck(ShieldCheckEvent event) {
        try {
            if (!SpellcraftSpell.PHASING.isEnabled()) return;

            Item weapon = event.getIncomingWeapon();
            if (weapon != null) {
                float phasingPower = weapon.getBonusForSpellEffect(SpellcraftSpell.PHASING.getEnchant());

                if (phasingPower > 0) {
                    // Reduce shield effectiveness
                    float currentBlock = event.getBlockPercent();
                    if (!Float.isNaN(currentBlock)) {
                        float reduction = phasingPower / 200.0f; // 100 power = 50% reduction
                        event.setBlockPercent(currentBlock * (1.0f - reduction));

                        logger.log(Level.FINE, "Phasing spell reduced shield block by " +
                            (reduction * 100) + "%");
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Phasing spell effect", e);
        }
    }
}
