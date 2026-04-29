package com.garward.wurmmodloader.mods.supplydepot;

import com.garward.wurmmodloader.api.events.action.ItemMenuBuildEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.item.ItemTemplatesCreatedEvent;
import com.garward.wurmmodloader.api.events.server.ServerPollEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;
import com.garward.wurmmodloader.modsupport.actions.ActionEntryBuilder;
import com.garward.wurmmodloader.modsupport.actions.ModActions;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Supply depot world boss object: random surface tile spawn (filterable),
 * timed capture action, light effect, scaled reward bundle. Ported from
 * Sindusk's WyvernMods {@code SupplyDepots}, with the upstream "20-80% of
 * world size" ring replaced by a configurable {@code SpawnFilter}
 * (region disc + height + slope + tile-type allowlist).
 *
 * <p>Three custom item templates ship in this jar: the depot itself
 * ({@code mod.item.arena.depot}), the per-capture cache reward
 * ({@code mod.item.arenacache}), and a sorcery fragment
 * ({@code mod.fragment.sorcery}). Rewards may pull additional caches from
 * the {@code caches} submod when present (resolved reflectively at boot).
 */
public class SupplyDepotMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(SupplyDepotMod.class.getName());

    private final SupplyDepotConfig cfg = new SupplyDepotConfig();
    private long pollIntervalMillis = 30_000L;
    private long lastPolled = 0L;
    private volatile boolean started = false;
    static int captureActionId = -1;

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        if (cfg.pollIntervalSeconds > 0) {
            pollIntervalMillis = cfg.pollIntervalSeconds * 1000L;
        }
        DepotEffects.useDepotLights = cfg.useDepotLights;
        logger.info("[supplydepot] enabled=" + cfg.enabled
                + " requirePvpServer=" + cfg.requirePvpServer
                + " respawnTimeMs=" + cfg.respawnTimeMs
                + " pollIntervalSeconds=" + cfg.pollIntervalSeconds
                + " enchantOrbTemplateId=" + cfg.enchantOrbTemplateId
                + " spawnArea=(" + cfg.spawn.areaCenterX + "," + cfg.spawn.areaCenterY
                + ",r=" + cfg.spawn.areaRadius + ")"
                + " heightRange=[" + cfg.spawn.minHeight + "," + cfg.spawn.maxHeight + "]"
                + " maxSlope=" + cfg.spawn.maxSlope
                + " tileTypes=" + (cfg.spawn.allowedTileTypes == null
                        ? "any" : cfg.spawn.allowedTileTypes.length + " entries"));
    }

    @Override
    public void preInit() {
        if (!cfg.enabled) {
            logger.info("[supplydepot] disabled via config; skipping templates and patches");
            return;
        }
        if (cfg.useDepotLights) {
            DepotPatches.install();
        }
    }

    @SubscribeEvent
    public void onItemTemplatesCreated(ItemTemplatesCreatedEvent event) {
        if (!cfg.enabled) return;
        DepotTemplates.register();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!cfg.enabled) return;
        DepotDb.initialize();
        ModActions.init();
        captureActionId = ModActions.getNextActionId();
        ModActions.registerAction(new ActionEntryBuilder(
                (short) captureActionId,
                "Capture depot",
                "capturing",
                new int[] { 6 /* ACTION_TYPE_NOMOVE */ }
        ).build());
        ModActions.registerActionPerformer(new CaptureDepotPerformer(cfg));
        logger.info("[supplydepot] registered Capture depot action (id=" + captureActionId + ")");
        started = true;
    }

    @SubscribeEvent
    public void onServerPoll(ServerPollEvent event) {
        if (!cfg.enabled || !started) return;
        long now = System.currentTimeMillis();
        if (now < lastPolled + pollIntervalMillis) return;
        lastPolled = now;
        DepotPoll.tick(cfg);
    }

    @SubscribeEvent
    public void onItemMenuBuild(ItemMenuBuildEvent event) {
        if (!cfg.enabled || captureActionId <= 0) return;
        Item target;
        try {
            target = com.wurmonline.server.Items.getItem(event.getTargetId());
        } catch (NoSuchItemException e) {
            return;
        }
        if (target == null || !DepotState.isSupplyDepot(target)) return;
        event.getAvailableActions().add(ModActions.getAction((short) captureActionId));
    }
}
