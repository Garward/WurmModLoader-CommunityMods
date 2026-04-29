package com.garward.wurmmodloader.mods.caches;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiered loot caches — armour, artifact, crystal, dragon, gem, moon, potion,
 * rift, titan, tool, treasure-map. Caches themselves are containers minted
 * elsewhere (rare-spawn loot, supply depots, GM creation); this submod
 * registers their templates, the right-click open action, and the loot
 * tables that fire on open.
 */
public class CachesMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(CachesMod.class.getName());

    private final CachesConfig cfg = new CachesConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        CacheLoot.artifactExtraTemplateIds = cfg.artifactCacheCustomTemplateIds;
        CacheLoot.crystalCacheTemplateIds = cfg.crystalCacheTemplateIds;
    }

    @Override
    public void preInit() {
        if (!cfg.enabled) {
            logger.info("[caches] disabled via config");
        }
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        if (!cfg.enabled) return;
        try {
            CacheTemplates.register();
            CacheLoot.recordCacheIds();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[caches] failed to register cache templates", t);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        try {
            ModActions.init();
            ModActions.registerAction(new TreasureCacheOpenAction());
            logger.info("[caches] open-cache action registered");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "[caches] failed to register open-cache action", t);
        }
    }
}
