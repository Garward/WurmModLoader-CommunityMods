package com.garward.wurmmodloader.mods.anticheat;

import com.garward.wurmmodloader.modloader.interfaces.Configurable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import java.util.Properties;
import java.util.logging.Logger;

public class AntiCheatMod implements WurmServerMod, Configurable, PreInitable {

    private static final Logger logger = Logger.getLogger(AntiCheatMod.class.getName());

    boolean enableSpoofHiddenOre = true;
    boolean prospectingVision = true;
    boolean mapSteamIds = true;

    @Override
    public void configure(Properties properties) {
        enableSpoofHiddenOre = bool(properties, "enableSpoofHiddenOre", enableSpoofHiddenOre);
        prospectingVision = bool(properties, "prospectingVision", prospectingVision);
        mapSteamIds = bool(properties, "mapSteamIds", mapSteamIds);
    }

    @Override
    public void preInit() {
        logger.info("[anticheat] skeleton loaded — patches not yet wired up "
                + "(enableSpoofHiddenOre=" + enableSpoofHiddenOre
                + ", prospectingVision=" + prospectingVision
                + ", mapSteamIds=" + mapSteamIds + ")");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
