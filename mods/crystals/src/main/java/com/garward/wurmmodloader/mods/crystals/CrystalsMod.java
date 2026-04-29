package com.garward.wurmmodloader.mods.crystals;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chaos crystals + enchanters crystals + the three crystal-infuse / combine
 * actions. Ported from Sindusk's WyvernMods Crystals helper +
 * ChaosCrystalInfuseAction / EnchantersCrystalInfuseAction /
 * CrystalCombineAction.
 *
 * Other submods (caches, soulstealing, bounty) pick up these template ids at
 * runtime through their own config — no compile-time coupling.
 */
public class CrystalsMod implements WurmServerMod, Configurable {

    private static final Logger logger = Logger.getLogger(CrystalsMod.class.getName());

    private final CrystalsConfig cfg = new CrystalsConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        if (!cfg.enabled) return;
        try {
            CrystalsTemplates.register();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[crystals] failed to register crystal templates", t);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        try {
            ModActions.init();
            ModActions.registerAction(new CrystalCombineAction());
            ModActions.registerAction(new ChaosCrystalInfuseAction());
            ModActions.registerAction(new EnchantersCrystalInfuseAction());
            logger.info("[crystals] actions registered");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[crystals] failed to register crystal actions", t);
        }
    }
}
