package com.garward.wurmmodloader.mods.soulstealing;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Soul-fed Eternal Reservoirs. Players harvest souls from corpses with a
 * sacrificial knife, feed them into deployed reservoirs, and the reservoirs
 * passively tend nearby branded carnivores + lit forges within a tile radius
 * scaled by their quality.
 *
 * Ported from Sindusk's WyvernMods Soulstealing module. Three actions
 * (soulsteal, check fuel, feed soul) and two custom item templates
 * (mod.item.soul, mod.item.eternal.reservoir). The periodic poll is wired
 * into {@link ServerPollEvent} with a configurable interval; upstream ran on
 * a manual cron in WyvernMods.poll().
 */
public class SoulstealingMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(SoulstealingMod.class.getName());

    private final SoulstealingConfig cfg = new SoulstealingConfig();
    private long lastPolled = 0L;

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        if (!cfg.enabled) return;
        try {
            SoulstealingTemplates.register();
            if (cfg.registerCreationEntry) {
                SoulstealingTemplates.registerCreationEntry(cfg.chaosCrystalTemplateId);
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[soulstealing] failed to register templates", t);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        try {
            ModActions.init();
            ModActions.registerAction(new SoulstealAction());
            ModActions.registerAction(new EternalReservoirCheckFuelAction());
            ModActions.registerAction(new EternalReservoirRefuelAction());
            logger.info("[soulstealing] actions registered");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[soulstealing] failed to register actions", t);
        }
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!cfg.enabled) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + cfg.pollIntervalMillis) return;
        lastPolled = now;
        SoulstealingPoll.pollAll();
    }
}
