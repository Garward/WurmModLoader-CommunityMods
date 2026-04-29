package com.garward.wurmmodloader.mods.arena;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Sindusk's WyvernMods {@code Arena} grab-bag of PvP-only patches. Every
 * inserted code path is gated on
 * {@code com.wurmonline.server.Servers.localServer.PVPSERVER}, so on a
 * single PvE server this mod loads but installs no behavior changes.
 *
 * <p>The bytecode patches live in {@link ArenaPatches}; the runtime
 * helpers live in {@link ArenaAttitude} / {@link ArenaSpawn} /
 * {@link ArenaCrossMod}. The PvP {@code SpawnQuestion} replacement lives
 * at {@code com.wurmonline.server.questions.NewSpawnQuestion} so it can
 * extend the package-private vanilla {@code Question} machinery.
 */
public class ArenaMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(ArenaMod.class.getName());

    private final ArenaConfig cfg = new ArenaConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        logger.info("[arena] enabled=" + cfg.enabled);
    }

    @Override
    public void preInit() {
        if (!cfg.enabled) {
            logger.info("[arena] disabled via config; skipping bytecode patches");
            return;
        }
        ArenaPatches.install(cfg);
    }
}
