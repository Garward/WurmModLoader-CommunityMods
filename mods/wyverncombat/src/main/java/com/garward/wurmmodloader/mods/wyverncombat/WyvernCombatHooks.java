package com.garward.wurmmodloader.mods.wyverncombat;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

/**
 * Static hooks called from patched {@code CombatHandler.getCombatRating}
 * bytecode. Kept apart from the patch installer so the entry-point
 * configure() can stamp the active config before preInit() runs the
 * patches.
 */
public final class WyvernCombatHooks {

    static volatile WyvernCombatMod config;

    private WyvernCombatHooks() {}

    public static float combatRatingAdditive(Creature self, Creature opponent) {
        WyvernCombatMod cfg = config;
        if (cfg == null || !cfg.combatRatingAdjustments) return 0.0f;
        float add = 0.0f;
        if (self != null && self.isPlayer() && opponent != null && !opponent.isPlayer()
                && cfg.royalExecutionerBonus && self.isRoyalExecutioner()) {
            add += 2.0f;
        }
        return add;
    }

    public static float combatRatingMultiplicative(Creature self) {
        WyvernCombatMod cfg = config;
        if (cfg == null || !cfg.combatRatingAdjustments || self == null) return 1.0f;
        float mult = 1.0f;
        if (cfg.petSoulDepthScaling && self.isDominated() && self.getDominator() instanceof Player) {
            Player owner = (Player) self.getDominator();
            double depth = owner.getSoulDepth().getKnowledge();
            mult *= depth * 0.02d;
        }
        if (cfg.vehicleCombatRatingPenalty && getVehicleSafe(self) != null) {
            mult *= 0.75f;
        }
        return mult;
    }

    private static Item getVehicleSafe(Creature pilot) {
        try {
            if (pilot.getVehicle() != -10L) {
                return Items.getItem(pilot.getVehicle());
            }
        } catch (NoSuchItemException ignored) {
            // pilot reports a vehicle id that no longer resolves — treat as dismounted
        }
        return null;
    }
}
