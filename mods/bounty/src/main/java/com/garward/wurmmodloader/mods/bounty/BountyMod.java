package com.garward.wurmmodloader.mods.bounty;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.creature.CreatureDeathEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.players.Player;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coin reward + corpse-loot mod ported from Sindusk's WyvernMods Bounty /
 * PlayerBounty / LootBounty. Vanilla-applicable behaviour only — custom
 * creature drops (Titan plate, Wyvern unique masks, AffinityOrb, the cache
 * items, etc.) are deferred to their owning submods, which plug in via
 * {@link BountyRegistry}.
 *
 * <h2>Two reward paths</h2>
 * <ul>
 *   <li><b>Player bounty</b> — driven by {@link CreatureDeathEvent}. The
 *       framework already names a single canonical killer (the highest-
 *       damage attacker captured at death), so we don't replicate the
 *       upstream "combatant in last 2 minutes" map-walk. Servers wanting
 *       group-play rewards register a
 *       {@link BountyRegistry.KillerRewardHandler}.</li>
 *   <li><b>Corpse loot</b> — driven by {@link BountyPatches}, which
 *       patches {@code Creature.die} so we can reach the local-variable
 *       corpse after it's placed, then walks
 *       {@link BountyRegistry#corpseLootHandlers()}.</li>
 * </ul>
 */
public class BountyMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(BountyMod.class.getName());

    private final BountyConfig cfg = new BountyConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        BountyStrength.cfg = cfg;
        logger.info("[bounty] configure: playerBounty=" + cfg.enablePlayerBounty
                + " corpseLoot=" + cfg.enableCorpseLoot
                + " floor=" + cfg.bountyFloorIron
                + " ceil=" + cfg.bountyCeilingIron);
    }

    @Override
    public void preInit() {
        if (cfg.enableCorpseLoot) {
            BountyPatches.install();
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (cfg.enableCorpseLoot) {
            BuiltInLootHandlers.registerEnabledHandlers(cfg);
        }
    }

    @SubscribeEvent
    public void onCreatureKilled(CreatureDeathEvent event) {
        if (!cfg.enablePlayerBounty) return;
        Creature victim = event.getVictim();
        Creature killer = event.getKiller();
        if (victim == null || killer == null) return;
        if (!killer.isPlayer() || victim.isPlayer()) return;
        if (victim.isReborn() || victim.isBred()) return;
        if (!(killer instanceof Player)) return;
        Player player = (Player) killer;

        try {
            double strength = BountyRegistry.getStrength(victim);

            long iron;
            long override = BountyRegistry.getRewardOverride(victim);
            if (override >= 0L) {
                iron = override;
            } else {
                iron = Math.max(0L, Math.round(strength));
            }

            double prefixMult = BountyRegistry.getTypePrefixMultiplier(victim);
            iron = Math.round(iron * prefixMult);

            if (Servers.localServer.PVPSERVER && !victim.isUnique()) {
                iron = Math.round(iron * cfg.pvpBountyMultiplier);
            }

            if (cfg.bountyFloorIron > 0L && iron < cfg.bountyFloorIron) {
                iron = cfg.bountyFloorIron;
            }
            if (cfg.bountyCeilingIron > 0L && iron > cfg.bountyCeilingIron) {
                iron = cfg.bountyCeilingIron;
            }

            if (iron > 0L) {
                try {
                    if (player.addMoney(iron)) {
                        String human = Economy.getEconomy().getChangeFor(iron).getChangeString();
                        player.getCommunicator().sendSafeServerMessage(
                                "You receive a bounty of " + human
                                        + " for slaying " + victim.getName() + ".");
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING,
                            "[bounty] addMoney failed for " + player.getName(), e);
                }
            }

            for (BountyRegistry.KillerRewardHandler h : BountyRegistry.killerRewardHandlers()) {
                try {
                    h.onCreatureKilled(player, victim, strength);
                } catch (Throwable t) {
                    logger.log(Level.WARNING,
                            "[bounty] killer-reward handler "
                                    + h.getClass().getName() + " threw", t);
                }
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "[bounty] reward path failed", t);
        }
    }
}
