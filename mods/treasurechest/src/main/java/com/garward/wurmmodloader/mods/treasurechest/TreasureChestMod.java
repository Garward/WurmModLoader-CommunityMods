package com.garward.wurmmodloader.mods.treasurechest;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Replaces the vanilla treasure-chest fill with a tiered loot table and
 * widens the chest auxData roll to 0-99 (vanilla 0-9). Ported from
 * Sindusk's WyvernMods {@code TreasureChests}.
 */
public class TreasureChestMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(TreasureChestMod.class.getName());

    private final TreasureChestConfig cfg = new TreasureChestConfig();

    @Override
    public void configure(Properties properties) {
        cfg.load(properties);
        TreasureChestLoot.affinityOrbTemplateId = cfg.affinityOrbTemplateId;
    }

    @Override
    public void preInit() {
        if (!cfg.enabled) {
            logger.info("[treasurechest] disabled via config; skipping bytecode patches");
            return;
        }
        TreasureChestPatches.install(cfg);
    }
}
