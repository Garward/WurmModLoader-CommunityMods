package com.garward.wurmmodloader.mods.gemaugmentation;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Improvement-skill quality boost via material impalement bonus, ported
 * from Sindusk's WyvernMods {@code GemAugmentation}. Each successful
 * improve/polish/temper action multiplies the QL gain by the item's
 * {@code getMaterialImpBonus()}, and the QL ceiling is raised to 9999.9
 * to make room for the boost on already-near-max items.
 */
public class GemAugmentationMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(GemAugmentationMod.class.getName());

    boolean enabled = true;

    @Override
    public void configure(Properties properties) {
        String e = properties.getProperty("enabled");
        if (e != null) enabled = Boolean.parseBoolean(e.trim());
        logger.info("[gemaugmentation] enabled=" + enabled);
    }

    @Override
    public void preInit() {
        if (!enabled) return;
        GemAugmentationPatches.install();
    }
}
