package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.modsupport.spawn.SpawnFilterConfig;

import java.util.Properties;
import java.util.logging.Logger;

final class TitanConfig {

    private static final Logger logger = Logger.getLogger(TitanConfig.class.getName());

    boolean enabled = true;
    long respawnTimeMs = 259_200_000L;
    int pollIntervalSeconds = 5;
    boolean disableNaturalRegeneration = true;
    int artifactCacheTemplateId = -1;
    int treasureMapCacheTemplateId = -1;

    final SpawnFilterConfig spawn = new SpawnFilterConfig();

    void load(Properties p) {
        enabled = bool(p, "enabled", enabled);
        respawnTimeMs = longVal(p, "respawnTimeMs", respawnTimeMs);
        pollIntervalSeconds = intVal(p, "pollIntervalSeconds", pollIntervalSeconds);
        disableNaturalRegeneration = bool(p, "disableNaturalRegeneration", disableNaturalRegeneration);
        artifactCacheTemplateId = intVal(p, "artifactCacheTemplateId", artifactCacheTemplateId);
        treasureMapCacheTemplateId = intVal(p, "treasureMapCacheTemplateId", treasureMapCacheTemplateId);
        spawn.load(p, "titan");
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
            logger.warning("[titan] invalid int for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }

    private static long longVal(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            logger.warning("[titan] invalid long for " + key + "=" + v + " (using " + def + ")");
            return def;
        }
    }
}
