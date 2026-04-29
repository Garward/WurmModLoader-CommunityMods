package com.garward.wurmmodloader.mods.bounty;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static dispatch surface invoked from the bytecode patch installed by
 * {@link BountyPatches}. Walking the {@link BountyRegistry} handler chain
 * happens here so the patched bytecode stays minimal.
 */
public final class BountyHooks {

    private static final Logger logger = Logger.getLogger(BountyHooks.class.getName());

    private BountyHooks() {}

    /** Called from {@code Creature.die} after {@code setRotation} on the
     *  freshly-created corpse. */
    public static void fireCorpseLoot(Creature victim, Item corpse) {
        if (victim == null || corpse == null) return;
        if (victim.isReborn() || victim.isBred()) return;
        double strength = BountyRegistry.getStrength(victim);
        for (BountyRegistry.CorpseLootHandler h : BountyRegistry.corpseLootHandlers()) {
            try {
                h.onCreatureDeath(victim, corpse, strength);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "[bounty] corpse-loot handler "
                        + h.getClass().getName() + " threw", t);
            }
        }
    }
}
