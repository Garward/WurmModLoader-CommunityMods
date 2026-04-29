package com.garward.wurmmodloader.mods.rarespawn;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilterConfig;

import java.util.Properties;
import java.util.logging.Logger;

final class RareSpawnConfig {

    private static final Logger logger = Logger.getLogger(RareSpawnConfig.class.getName());

    boolean enabled = true;
    int pollIntervalSeconds = 300;
    boolean includeWyvernBlue = true;

    final SpawnFilterConfig spawn = new SpawnFilterConfig();

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);
        pollIntervalSeconds = intVal(p, "pollIntervalSeconds", pollIntervalSeconds);
        includeWyvernBlue = bool(p, "includeWyvernBlue", includeWyvernBlue);
        spawn.load(p, "rarespawn");
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int intVal(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[rarespawn] invalid int for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }
}
