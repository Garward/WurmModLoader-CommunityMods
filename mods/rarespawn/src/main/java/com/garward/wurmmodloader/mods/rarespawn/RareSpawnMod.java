package com.garward.wurmmodloader.mods.rarespawn;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.creatures.ModCreatures;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Reaper, Spectral drake, and Blue wyvern world bosses ported from
 * Sindusk's WyvernMods {@code RareSpawns}. WyvernBlue is also a rideable
 * mount; it is registered as a creature template regardless of the
 * {@code includeWyvernBlue} flag, which only controls world-spawn cycles.
 */
public class RareSpawnMod implements WurmServerMod, Configurable, Initable {

    private static final Logger logger = Logger.getLogger(RareSpawnMod.class.getName());

    private final RareSpawnConfig cfg = new RareSpawnConfig();
    private long pollIntervalMillis = 300_000L;
    private long lastPolled = 0L;

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        if (cfg.pollIntervalSeconds > 0) {
            pollIntervalMillis = cfg.pollIntervalSeconds * 1000L;
        }
        logger.info("[rarespawn] enabled=" + cfg.enabled
                + " pollIntervalSeconds=" + cfg.pollIntervalSeconds
                + " includeWyvernBlue=" + cfg.includeWyvernBlue);
    }

    @Override
    public void init() {
        if (!cfg.enabled) return;
        ModCreatures.init();
        ModCreatures.addCreature(new Reaper());
        ModCreatures.addCreature(new SpectralDrake());
        ModCreatures.addCreature(new WyvernBlue());
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!cfg.enabled) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + pollIntervalMillis) return;
        lastPolled = now;
        RareSpawnPoll.tick(cfg);
    }
}
