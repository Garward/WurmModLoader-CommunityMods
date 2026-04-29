package com.garward.wurmmodloader.mods.wyverncombat;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Combat-side residuals from Sindusk's WyvernMods {@code CombatChanges}.
 * Ships four bytecode patches against vanilla {@code CombatHandler} +
 * {@code Wound} and one server-poll handler:
 *
 * <ul>
 *   <li>combat-rating additive/multiplicative hooks: royal-executioner
 *       bonus, pet-soul-depth scaling, vehicle CR penalty.</li>
 *   <li>halve CR contribution from TrueHit/Excel-style spell effects.</li>
 *   <li>block natural Wound.poll healing on uniques.</li>
 *   <li>periodic 75 HP heal of one random wound on each living unique.</li>
 * </ul>
 *
 * <p>Magranon-stacking and life-transfer patches from upstream are dropped:
 * the former is dead in DUSKombat, the latter was already commented out
 * upstream.
 */
public class WyvernCombatMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(WyvernCombatMod.class.getName());

    boolean enabled = true;
    boolean combatRatingAdjustments = true;
    boolean royalExecutionerBonus = true;
    boolean petSoulDepthScaling = true;
    boolean vehicleCombatRatingPenalty = true;
    boolean adjustCombatRatingSpellPower = true;
    boolean disableLegendaryRegeneration = true;
    int uniqueRegenerationIntervalSeconds = 10;

    private long lastUniquePoll = 0L;

    @Override
    public void configure(Properties properties) {
        enabled = bool(properties, "enabled", enabled);
        combatRatingAdjustments = bool(properties, "combatRatingAdjustments", combatRatingAdjustments);
        royalExecutionerBonus = bool(properties, "royalExecutionerBonus", royalExecutionerBonus);
        petSoulDepthScaling = bool(properties, "petSoulDepthScaling", petSoulDepthScaling);
        vehicleCombatRatingPenalty = bool(properties, "vehicleCombatRatingPenalty", vehicleCombatRatingPenalty);
        adjustCombatRatingSpellPower = bool(properties, "adjustCombatRatingSpellPower", adjustCombatRatingSpellPower);
        disableLegendaryRegeneration = bool(properties, "disableLegendaryRegeneration", disableLegendaryRegeneration);
        uniqueRegenerationIntervalSeconds = integer(properties, "uniqueRegenerationIntervalSeconds",
                uniqueRegenerationIntervalSeconds);
        logger.info("[wyverncombat] enabled=" + enabled
                + " CR=" + combatRatingAdjustments
                + " spellNerf=" + adjustCombatRatingSpellPower
                + " regenDisable=" + disableLegendaryRegeneration
                + " uniqueRegenSec=" + uniqueRegenerationIntervalSeconds);
        WyvernCombatHooks.config = this;
    }

    @Override
    public void preInit() {
        if (!enabled) return;
        WyvernCombatPatches.install(this);
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!enabled) return;
        if (uniqueRegenerationIntervalSeconds <= 0) return;
        long now = System.currentTimeMillis();
        if (now < lastUniquePoll + (long) uniqueRegenerationIntervalSeconds * 1000L) return;
        lastUniquePoll = now;
        UniqueRegenerationPoll.tick();
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int integer(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
