package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.creatures.ModCreatures;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Lilith and Ifrit raid bosses + their wraith / zombie / fiend / spider
 * minions, scripted boss fight, server-wide percentage announcements, and
 * auto-spawn after a configurable cooldown. Ported from Sindusk's
 * WyvernMods {@code Titans}.
 */
public class TitanMod implements WurmServerMod, Configurable, PreInitable, Initable {

    private static final Logger logger = Logger.getLogger(TitanMod.class.getName());

    private final TitanConfig cfg = new TitanConfig();
    private long pollIntervalMillis = 5_000L;
    private long lastPolled = 0L;
    private volatile boolean started = false;

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        if (cfg.pollIntervalSeconds > 0) {
            pollIntervalMillis = cfg.pollIntervalSeconds * 1000L;
        }
        logger.info("[titan] enabled=" + cfg.enabled
                + " respawnTimeMs=" + cfg.respawnTimeMs
                + " pollIntervalSeconds=" + cfg.pollIntervalSeconds
                + " disableNaturalRegeneration=" + cfg.disableNaturalRegeneration
                + " artifactCacheTemplateId=" + cfg.artifactCacheTemplateId
                + " treasureMapCacheTemplateId=" + cfg.treasureMapCacheTemplateId);
    }

    @Override
    public void preInit() {
        if (!cfg.enabled) {
            logger.info("[titan] disabled via config; skipping bytecode patches");
            return;
        }
        if (cfg.disableNaturalRegeneration) {
            TitanPatches.install();
        }
    }

    @Override
    public void init() {
        if (!cfg.enabled) return;
        ModCreatures.init();
        ModCreatures.addCreature(new Lilith());
        ModCreatures.addCreature(new Ifrit());
        ModCreatures.addCreature(new LilithWraith());
        ModCreatures.addCreature(new LilithZombie());
        ModCreatures.addCreature(new IfritFiend());
        ModCreatures.addCreature(new IfritSpider());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        TitanDb.initialize();
        started = true;
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!cfg.enabled || !started) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + pollIntervalMillis) return;
        lastPolled = now;
        TitanPoll.tick(cfg);
    }
}
