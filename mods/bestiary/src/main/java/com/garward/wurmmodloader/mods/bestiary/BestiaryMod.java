package com.garward.wurmmodloader.mods.bestiary;

import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.api.events.server.ServerStartedEvent;
import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

public class BestiaryMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(BestiaryMod.class.getName());

    boolean disableAfkTraining = true;
    boolean fixSacrificingStrongCreatures = true;
    boolean conditionWildCreatures = true;
    boolean useCustomCreatureSizes = true;
    boolean genesisEnchantedGrassNewborns = true;
    boolean preventLegendaryHitching = true;
    boolean allowGhostArchery = true;
    boolean logCreatureSpawns = false;
    boolean applyTemplateAdjustments = true;

    @Override
    public void configure(Properties properties) {
        disableAfkTraining = bool(properties, "disableAfkTraining", disableAfkTraining);
        fixSacrificingStrongCreatures = bool(properties, "fixSacrificingStrongCreatures", fixSacrificingStrongCreatures);
        conditionWildCreatures = bool(properties, "conditionWildCreatures", conditionWildCreatures);
        useCustomCreatureSizes = bool(properties, "useCustomCreatureSizes", useCustomCreatureSizes);
        genesisEnchantedGrassNewborns = bool(properties, "genesisEnchantedGrassNewborns", genesisEnchantedGrassNewborns);
        preventLegendaryHitching = bool(properties, "preventLegendaryHitching", preventLegendaryHitching);
        allowGhostArchery = bool(properties, "allowGhostArchery", allowGhostArchery);
        logCreatureSpawns = bool(properties, "logCreatureSpawns", logCreatureSpawns);
        applyTemplateAdjustments = bool(properties, "applyTemplateAdjustments", applyTemplateAdjustments);
    }

    @Override
    public void preInit() {
        BestiaryPatches.install(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (applyTemplateAdjustments) {
            BestiaryTemplates.applyAll();
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
